(ns leihs.borrow.features.customer-orders.show
  (:require
   ["date-fns" :as date-fns]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.translate :as translate :refer [t set-default-translate-path]]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.customer-orders.index :refer [status-summary]]
   [leihs.borrow.features.customer-orders.repeat-order :as repeat-order]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.core.core :refer [dissoc-in]]
   ["/leihs-ui-client-side-external-react" :as UI]))

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
                 {:id order-id}
                 [::on-fetched-data order-id]]})))

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
   (assoc-in db [::data :cancellation-dialog] {})))

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
               {:id id}
               [::on-cancel-order]]}))

(reg-event-fx
 ::on-cancel-order
 (fn-traced [{:keys [db]} [_ {{rental :cancel-order} :data errors :errors}]]
   (if errors
     {:alert (str "FAIL! " (pr-str errors))
      :db (-> db
              (dissoc-in [::data :cancellation-dialog]))}

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

(reg-sub ::reservations-grouped
         (fn [[_ id]] (subscribe [::reservations id]))
         (fn [lines _]
           (->> lines
                (sort-by
                 (fn [line]
                   [(get-in line [:start-date])
                    (get-in line [:inventory-pool :name])
                    (get-in line [:model :name])]))
                (group-by
                 (fn [line]
                   [(get-in line [:model :id])
                    (get-in line [:inventory-pool :id])
                    (get-in line [:start-date])
                    (get-in line [:end-date])])))))

(reg-sub ::cancellation-dialog-data
         (fn [db _] (get-in db [::data :cancellation-dialog])))

(reg-sub ::current-profile-id
         :<- [::current-user/current-profile-id]
         (fn [current-profile-id _] current-profile-id))

(reg-sub ::can-change-profile?
         :<- [::current-user/can-change-profile?]
         (fn [can-change-profile? _] can-change-profile?))


(defn ui-item-line [reservation]
  (let [model (:model reservation)
        option (:option reservation)
        name (:name (or model option))
        quantity (:quantity reservation)
        start-date (js/Date. (:start-date reservation))
        end-date (js/Date. (:end-date reservation))
        ;; NOTE: should be in API
        total-days (+ 1 (date-fns/differenceInCalendarDays end-date start-date))
        title (t :reservation-line.title {:itemCount quantity, :itemName name})
        inventory-code (-> reservation :item :inventory-code)
        duration (t :reservation-line.duration {:totalDays total-days, :fromDate start-date})
        sub-title (get-in reservation [:inventory-pool :name])
        href (when model (routing/path-for ::routes/models-show :model-id (:id (:model reservation))))
        status (:status reservation)
        is-over? (date-fns/isAfter (js/Date.) (date-fns/addDays end-date 1))
        overdue? (and (= status "SIGNED") is-over?)
        expired-unapproved? (and (= status "SUBMITTED") is-over?)
        expired? (and (= status "APPROVED") is-over?)
        refined-status (cond expired-unapproved? "EXPIRED-UNAPPROVED" expired? "EXPIRED" :else status)]
    [:<>
     [:> UI/Components.Design.ListCard {:href href}
      [:> UI/Components.Design.ListCard.Title
       title (when inventory-code [:span.fw-light " (" inventory-code ")"])
       (when option [:span.fw-light " (" (t :reservation-line.option) ")"])]

      [:> UI/Components.Design.ListCard.Body
       sub-title]

      [:> UI/Components.Design.ListCard.Foot
       [:> UI/Components.Design.Badge duration] " "
       [:> UI/Components.Design.Badge {:colorClassName (when overdue? " bg-danger")}
        (t (str :reservation-status-label "/" refined-status) {:endDate end-date})]]]]))

(defn ui-items-list [grouped-reservation]
  [:> UI/Components.Design.ListCard.Stack
   (doall
    (for [[_ items] grouped-reservation]
      (for [item items]
        (when item
          [:<> {:key (:id item)}
           [ui-item-line item]]))))])

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
        [:> UI/Components.Design.Section {:title title :collapsible true}
         [:p purpose]
         [:p.small (rentals/rental-summary-text rental)]]]])))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        rental-id (get-in routing [:bidi-match :route-params :rental-id])
        rental @(subscribe [::data rental-id])
        reservations @(subscribe [::reservations rental-id])
        grouped-reservations @(subscribe [::reservations-grouped rental-id])
        errors @(subscribe [::errors rental-id])
        is-loading? (not (or rental errors))

        rental-title  (or (:title rental) (:purpose rental))

        is-cancelable? (= ["IN_APPROVAL"] (:fulfillment-states rental))
        contracts (map :node (get-in rental [:contracts :edges]))
        user-data @(subscribe [::current-user/user-data])
        current-profile-id @(subscribe [::current-profile-id])
        can-change-profile? @(subscribe [::can-change-profile?])
        rental-user-id (-> rental :user :id)
        date-fns-locale @(subscribe [::translate/i18n-locale])]

    [:<>
     (cond

       is-loading?
       [:<>
        [:> UI/Components.Design.PageLayout.Header
         {:title (t :page-title)}]
        [ui/loading]]

       errors [ui/error-view errors]

       :else
       [:<>
        [:> UI/Components.Design.PageLayout.Header
         {:title rental-title}

         [:h2.fw-light (rentals/rental-summary-text rental)]]

        [cancellation-dialog rental]

        [repeat-order/repeat-dialog rental reservations current-profile-id date-fns-locale]
        [repeat-order/repeat-success-notification]

        [:> UI/Components.Design.Stack {:space 5}

         [:> UI/Components.Design.Section
          {:title (t :state) :collapsible true}

          [:> UI/Components.Design.Stack {:space 3}

           [status-summary rental false]

           [:> UI/Components.Design.ActionButtonGroup
            (when is-cancelable?
              [:button.btn.btn-secondary {:onClick #(dispatch [::open-cancellation-dialog rental-id])} (t :cancel-action-label)])
            [:button.btn.btn-secondary {:onClick #(dispatch [::repeat-order/open-repeat-dialog])} (t :repeat-action-label)]]]]

         [:> UI/Components.Design.Section
          {:title (t :user-or-delegation-section-title) :collapsible true}
          (if (or (nil? rental-user-id) (= rental-user-id (:id user-data)))
            [:<> (:name user-data) (when can-change-profile? (t :!borrow.phrases.user-or-delegation-personal-postfix))]
            [:<> (->> (:delegations user-data)
                      (filter #(= rental-user-id (:id %)))
                      first
                      :name)])]

         [:> UI/Components.Design.Section
          {:title (t :purpose) :collapsible true}
          (:purpose rental)]

         [:> UI/Components.Design.Section
          {:title (t :items-section-title) :collapsible true}
          (ui-items-list grouped-reservations)]

         (when (seq contracts)
           [:> UI/Components.Design.Section
            {:title (t :documents-section-title) :collapsible true}

            [:> UI/Components.Design.Stack {:space 3}
             (doall
              (for [contract contracts]
                ^{:key (:id contract)}
                [:<>
                 [:> UI/Components.Design.DownloadLink
                  {:href (:print-url contract)}
                  (t :!borrow.terms/contract) " " (:compact-id contract) " "
                  [:span.fw-light (str "(" (ui/format-date :short (:created-at contract)) ")")]]]))]])]

        [:> UI/Components.Design.PageLayout.MetadataWithDetails
         {:summary (t :metadata-summary {:rentalId (:id rental)})
          :details (clj->js (h/camel-case-keys rental))}]

        ;
        ])]))
