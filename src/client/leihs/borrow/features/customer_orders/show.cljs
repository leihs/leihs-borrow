(ns leihs.borrow.features.customer-orders.show
  (:require
   ["/borrow-ui" :as UI]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.customer-orders.repeat-order :as repeat-order]
   [leihs.borrow.features.customer-orders.reservation-card :refer [reservation-card]]
   [leihs.borrow.features.customer-orders.status-summary :refer [status-summary]]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db reg-event-fx
                                      reg-sub subscribe]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :as translate :refer [set-default-translate-path
                                                     t]]
   [leihs.core.core :refer [dissoc-in]]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]))

(set-default-translate-path :borrow.rental-show)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/rentals-show
 (fn-traced [{:keys [db]} [_ args]]
   (let [order-id (get-in args [:route-params :rental-id])]
     {:dispatch [::re-graph/query
                 (str
                  (rc/inline "leihs/borrow/features/customer_orders/customerOrderShow.gql") "\n"
                  (rc/inline "leihs/borrow/features/customer_orders/fragment_rentalProps.gql"))
                 {:id order-id
                  :userId (current-user/get-current-profile-id db)}
                 [::on-fetched-data order-id]]
      :db (-> db (assoc-in [::errors order-id] nil))})))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ order-id {:keys [data errors]}]]
   (-> db
       (update-in , [::data order-id] (fnil identity {}))
       (cond-> errors (assoc-in , [::errors order-id] errors))
       (assoc-in , [::data order-id] (into (sorted-map) (:rental data))))))

(reg-event-db
 ::open-cancellation-dialog
 (fn-traced [db]
   (assoc-in db [::data :cancellation-dialog] {:show true})))

(reg-event-db
 ::close-cancellation-dialog
 (fn-traced [db]
   (dissoc-in db [::data :cancellation-dialog])))

(reg-event-fx
 ::cancel-order
 (fn-traced [{:keys [db]} [_ id]]
   {:db (-> db
            (assoc-in [::data :cancellation-dialog :is-saving?] true))
    :dispatch [::re-graph/mutate
               (str
                (rc/inline "leihs/borrow/features/customer_orders/cancelOrder.gql") "\n"
                (rc/inline "leihs/borrow/features/customer_orders/fragment_rentalProps.gql"))
               {:id id
                :userId (current-user/get-current-profile-id db)}
               [::on-cancel-order]]}))

(reg-event-fx
 ::on-cancel-order
 (fn-traced [{:keys [db]} [_ {{rental :cancel-order} :data errors :errors}]]
   (if errors
     {:dispatch [::errors/add-many errors]
      :db (-> db
              (dissoc-in [::data :cancellation-dialog :is-saving?]))}

     {:db (-> db
              (assoc-in [::data (:id rental)] (into (sorted-map) rental))
              (dissoc-in [::data :cancellation-dialog]))})))

(reg-sub ::data
         (fn [db [_ id]] (get-in db [::data id])))

(reg-sub ::errors
         (fn [db [_ id]] (get-in db [::errors id])))

(reg-sub ::reservations
         (fn [[_ id]] (subscribe [::data id]))
         (fn [co _] #_(h/log co) (:reservations co)))

(reg-sub ::reservations-sorted
         (fn [[_ id]] (subscribe [::reservations id]))
         (fn [lines _]
           (->> lines
                (sort-by
                 (fn [line]
                   [(get-in line [:start-date])
                    (get-in line [:inventory-pool :name])
                    (get-in line [:model :name])
                    (get-in line [:actual-end-date])])))))

(reg-sub ::cancellation-dialog-data
         (fn [db _] (get-in db [::data :cancellation-dialog])))

(reg-sub ::current-profile-id
         :<- [::current-user/current-profile-id]
         (fn [current-profile-id _] current-profile-id))

(reg-sub ::can-change-profile?
         :<- [::current-user/can-change-profile?]
         (fn [can-change-profile? _] can-change-profile?))

(defn cancellation-dialog []
  (fn [rental]
    (let [dialog-data @(subscribe [::cancellation-dialog-data])
          title (:title rental)
          purpose (:purpose rental)
          id (:id rental)]
      [:> UI/Components.Design.ConfirmDialog
       {:shown (some? dialog-data)
        :title (t :cancellation-dialog/title)
        :onConfirm #(dispatch [::cancel-order id])
        :confirmLabel (t :cancellation-dialog/confirm)
        :confirmIsLoading (:is-saving? dialog-data)
        :dismissible true
        :onDismiss #(dispatch [::close-cancellation-dialog])
        :onCancel #(dispatch [::close-cancellation-dialog])
        :cancelLabel (t :cancellation-dialog/cancel)}
       [:<>
        [:p.fw-bold title]
        [:p (when (not= title purpose) [:p.preserve-linebreaks purpose])]
        [:p.small (rentals/rental-summary-text rental)]]])))

(defn view []
  (let [now (js/Date.)
        routing @(subscribe [:routing/routing])
        rental-id (get-in routing [:bidi-match :route-params :rental-id])
        rental @(subscribe [::data rental-id])
        reservations @(subscribe [::reservations rental-id])
        reservations-sorted @(subscribe [::reservations-sorted rental-id])
        errors @(subscribe [::errors rental-id])
        is-loading? (not (or rental errors))
        error403? (and (not is-loading?) (some #(= 403 (-> % :extensions :code)) errors))

        rental-title  (or (:title rental) (:purpose rental))

        is-cancelable? (= ["IN_APPROVAL"] (:fulfillment-states rental))
        is-repeatable? (seq reservations)
        contracts (map :node (get-in rental [:contracts :edges]))
        user-data @(subscribe [::current-user/user-data])
        current-profile-id @(subscribe [::current-profile-id])
        can-change-profile? @(subscribe [::can-change-profile?])
        rental-user-id (-> rental :user :id)
        date-locale @(subscribe [::translate/date-locale])]

    [:> UI/Components.Design.PageLayout.ContentContainer
     (cond

       is-loading?
       [:<>
        [:> UI/Components.Design.PageLayout.Header
         {:title (t :page-title)}]
        [ui/loading]]

       errors (if error403?
                [:> UI/Components.Design.PageLayout.Header
                 {:title (t :page-title)}
                 [:> UI/Components.Design.InfoMessage {:class "mt-2"} (t :message-403)]]
                [ui/error-view errors])

       :else
       [:<>
        [:> UI/Components.Design.PageLayout.Header
         {:title rental-title}

         [:h2 (rentals/rental-summary-text rental)]]

        [cancellation-dialog rental]

        [repeat-order/repeat-dialog rental reservations current-profile-id date-locale]
        [repeat-order/repeat-success-notification]

        [:div.d-grid.gap-5

         [:> UI/Components.Design.Section
          {:title (t :state) :collapsible false}

          [:div.d-grid.gap-3

           [status-summary rental false]

           (when (or is-cancelable? is-repeatable?)
             [:> UI/Components.Design.ActionButtonGroup
              (when is-cancelable?
                [:button.btn.btn-secondary {:onClick #(dispatch [::open-cancellation-dialog rental-id])} (t :cancel-action-label)])
              (when is-repeatable?
                [:button.btn.btn-secondary {:onClick #(dispatch [::repeat-order/open-repeat-dialog])} (t :repeat-action-label)])])]]

         (when (not-empty (:delegations user-data))
           [:> UI/Components.Design.Section
            {:title (t :user-or-delegation-section-title) :collapsible false}
            (if (or (nil? rental-user-id) (= rental-user-id (:id user-data)))
              [:div.fw-bold (:name user-data) (when can-change-profile? (t :!borrow.phrases.user-or-delegation-personal-postfix))]
              [:div.fw-bold (->> (:delegations user-data)
                                 (filter #(= rental-user-id (:id %)))
                                 first
                                 :name)])])

         (when-let [contact-details (-> rental :contact-details not-empty)]
           [:> UI/Components.Design.Section
            {:title (t :contact-details) :collapsible false}
            [:div.fw-bold contact-details]])

         [:> UI/Components.Design.Section
          {:title (t :purpose) :collapsible false}
          [:div.fw-bold.preserve-linebreaks (:purpose rental)]]

         [:> UI/Components.Design.Section
          {:title (t :items-section-title) :collapsible true}
          [:> UI/Components.Design.ListCard.Stack
           (doall
            (for [reservation reservations-sorted]
              [:<> {:key (:id reservation)}
               [reservation-card reservation
                now
                (some->> (:model reservation) :id
                         (routing/path-for ::routes/models-show :model-id))
                date-locale]]))]]

         (when (seq contracts)
           [:> UI/Components.Design.Section
            {:title (t :documents-section-title) :collapsible true}

            [:div.d-grid.gap-3
             (doall
              (for [contract contracts]
                ^{:key (:id contract)}
                [:<>
                 [:> UI/Components.Design.DownloadLink
                  {:href (:print-url contract)}
                  (t :!borrow.terms/contract) " " (:compact-id contract) " "
                  [:span (str "(" (ui/format-date :short (:created-at contract)) ")")]]]))]])]])]))
