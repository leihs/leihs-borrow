(ns leihs.borrow.features.customer-orders.repeat-order
  (:require
   ["/borrow-ui" :as UI]
   ["date-fns" :as date-fns]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.form-helpers :refer [UiDateRangePicker]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db reg-event-fx
                                      reg-sub subscribe]]
   [leihs.borrow.lib.translate :as translate :refer [set-default-translate-path
                                                     t]]
   [leihs.borrow.translations :as translations]
   [leihs.core.core :refer [dissoc-in]]
   [re-graph.core :as re-graph]
   [reagent.core :as reagent]
   [shadow.resource :as rc]))

(set-default-translate-path :borrow.rental-show.repeat-order)

;; TODO: error handling

(reg-event-fx
 ::open-repeat-dialog
 (fn-traced [{:keys [db]} [_ rental]]
   (let [order-id (-> rental :id)
         pool-ids (->> rental :reservations (map #(-> % :inventory-pool :id)) distinct)
         start-date (date-fns/startOfMonth (js/Date.))
         end-date (date-fns/addYears start-date 1)]
     (if (-> pool-ids count (= 1))
       {:dispatch [::re-graph/query
                   (str
                    (rc/inline "leihs/borrow/features/customer_orders/poolAvailability.gql"))
                   {:id (first pool-ids)
                    :startDate start-date
                    :endDate end-date}
                   [::on-open-repeat-dialog order-id]]
        :db (-> db (assoc-in [::data :repeat-dialog] {:loading? true}))}
       ; else
       {:db (-> db (assoc-in [::data :repeat-dialog] {:show true}))}))))

(reg-event-db
 ::on-open-repeat-dialog
 (fn-traced [db [_ _ {:keys [data errors]}]]
   (-> db
       #_(cond-> errors (assoc-in [::errors order-id] errors))
       (assoc-in [::data :repeat-dialog] {:show true
                                          :pool (->> data :inventory-pool)}))))

(reg-event-db
 ::close-repeat-dialog
 (fn-traced [db]
   (dissoc-in db [::data :repeat-dialog])))

(reg-event-fx
 ::mutate
 (fn-traced [{:keys [db]} [_ args]]
   {:db (-> db
            (assoc-in [::data :repeat-dialog :is-saving?] true))
    :dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/features/customer_orders/repeatOrder.gql")
               args
               [::on-mutate-result]]}))

(reg-event-fx
 ::on-mutate-result
 (fn-traced [{:keys [db]}
             [_ {{reservations :repeat-order} :data
                 errors :errors}]]
   (if errors
     {:db (-> db
              (dissoc-in [::data :repeat-dialog :is-saving?]))
      :dispatch [::errors/add-many errors]}
     {:db (-> db
              (assoc-in [::data :repeat-success] {:reservations reservations})
              (dissoc-in [::data :repeat-dialog]))})))

(reg-event-db
 ::close-repeat-success-notification
 (fn-traced [db]
   (dissoc-in db [::data :repeat-success])))

(reg-event-fx
 ::confirm-repeat-success-notification
 (fn-traced [_ _]
   {:fx [[:dispatch [::close-repeat-success-notification]]
         [:dispatch [:routing/navigate [::routes/shopping-cart]]]]}))

(reg-sub ::repeat-dialog-data
         (fn [db _] (-> db (get-in [::data :repeat-dialog]))))

(reg-sub ::loading?
         :<- [::repeat-dialog-data]
         (fn [data]
           (-> data :loading?)))

(reg-sub ::repeat-success-data
         (fn [db _] (-> db (get-in [::data :repeat-success]))))

(reg-sub ::current-profile-name
         :<- [::current-user/user-data]
         :<- [::current-user/current-delegation]
         (fn [[user-data current-delegation] _]
           (if current-delegation
             (:name current-delegation)
             (str (-> user-data :name)
                  (cond (-> user-data :delegations seq)
                        (t :!borrow.phrases.user-or-delegation-personal-postfix))))))

(defn- get-disabled-dates [pool f]
  (->> pool :availability :dates
       (filter #(-> % f seq))
       (map #(-> % :date date-fns/parseISO))))

(defn- get-pool-availability-js [pool]
  (clj->js
   (h/camel-case-keys
    {:inventoryPool (or pool {})
     :dates (if pool
              (->> pool :availability :dates
                   (map (fn [day-data]
                          (assoc day-data :parsedDate (-> day-data :date date-fns/parseISO)))))
              [])})))

(defn repeat-dialog [rental reservations user-id date-locale text-locale]
  (let [today (date-fns/startOfToday)
        max-date (date-fns/addYears today 1)
        selected-range (reagent/atom {:startDate today :endDate  (date-fns/addDays today 1)})
        validation-result (reagent/atom {:valid? true})
        validate! (fn [start-date end-date pool]
                    (reset!
                     validation-result
                     (cond
                       (not (and (date-fns/isValid start-date) (date-fns/isValid end-date)))
                       {:valid? false}
                       (> start-date end-date)
                       {:valid? false :date-messages [(t :dialog.validation.start-after-end)]}
                       :else
                       (let [availability-messages
                             (UI/validateDateRange
                              #js {:startDate start-date :endDate end-date}
                              today
                              max-date
                              (get-pool-availability-js pool)
                              1
                              text-locale
                              date-locale
                              (clj->js (get-in translations/dict [:borrow :order-panel :validate])))]
                         (if (seq availability-messages)
                           {:valid? false :date-messages availability-messages}
                           {:valid? true})))))
        change-selected-range (fn [r pool]
                                (let [start-date (-> r .-startDate)
                                      end-date (-> r .-endDate)]
                                  (reset! selected-range {:startDate start-date :endDate end-date})
                                  (validate! start-date end-date pool)))
        get-quantity (fn [reservations filter-pred]
                       (->> reservations (filter filter-pred) (map :quantity) (reduce +)))]
    (fn [rental reservations user-id date-locale]
      (let [dialog-data @(subscribe [::repeat-dialog-data])
            current-profile-name @(subscribe [::current-profile-name])
            is-saving? (:is-saving? dialog-data)
            rental-id (:id rental)
            options-quantity (get-quantity reservations #(-> % :option))
            models-quantity (get-quantity reservations #(-> % :option not))
            pool (-> dialog-data :pool)
            calendar-availability-props (if pool
                                          {:disabledStartDates (get-disabled-dates pool :start-date-restrictions)
                                           :disabledEndDates (get-disabled-dates pool :end-date-restrictions)
                                           :className (when (not (:valid? @validation-result)) "invalid-date-range")}
                                          {})]
        (if-not (-> dialog-data :show)
          nil
          [:> UI/Components.Design.ModalDialog
           {:id :repeat-order
            :shown true
            :dismissible true
            :on-dismiss #(dispatch [::close-repeat-dialog])
            :title (t :dialog.title)
            :class "ui-repeat-order-dialog"}
           [:> UI/Components.Design.ModalDialog.Body
            [:form {:on-submit (fn [e]
                                 (-> e .preventDefault)
                                 (validate! (:startDate @selected-range) (:endDate @selected-range) pool)
                                 (when (and (-> e .-target .checkValidity) (:valid? @validation-result))
                                   (dispatch [::mutate
                                              {:id rental-id
                                               :startDate (h/date-format-day (:startDate @selected-range))
                                               :endDate (h/date-format-day (:endDate @selected-range))
                                               :userId user-id}])))
                    :no-validate true
                    :auto-complete :off
                    :id :the-form}
             (if (= models-quantity 0)
               [:div.d-grid.gap-4
                [:> UI/Components.Design.Warning {:class "fs-2"}
                 (t :dialog.error-only-options)]]
               [:div.d-grid.gap-4
                [:> UI/Components.Design.Section
                 [:p.fw-bold (t :dialog.info {:count models-quantity})]
                 (when (> options-quantity 0)
                   [:> UI/Components.Design.Warning {:class "fs-2"}
                    (t :dialog.warning-some-options {:count options-quantity})])]
                [:> UI/Components.Design.Section {:title (t :dialog.order-for)}
                 [:div.fw-bold current-profile-name]]
                [:> UI/Components.Design.Section {:title (t :dialog.timespan)}
                 [:fieldset
                  [:legend.visually-hidden (t :dialog.timespan)]
                  [:div.d-flex.flex-column.gap-3
                   [UiDateRangePicker
                    (merge calendar-availability-props
                           {:locale date-locale
                            :txt {:from (t :dialog.from)
                                  :until (t :dialog.until)
                                  :placeholderFrom (t :dialog.undefined)
                                  :placeholderUntil (t :dialog.undefined)}
                            :selected-range @selected-range
                            :onChange #(change-selected-range % pool)
                            :min-date today
                            :max-date max-date})]
                   (when-let [messages (-> @validation-result :date-messages seq)]
                     (doall
                      (for [[idx message] (map-indexed vector messages)]
                        ^{:key idx} [:<> [:> UI/Components.Design.Warning message]])))]
                  (when (nil? pool)
                    [:> UI/Components.Design.InfoMessage {:class "mt-3"}
                     (t :dialog.info-multi-pool)])]]])]]
           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-primary {:form :the-form :type :submit :disabled (or (= 0 models-quantity) (not (:valid? @validation-result)))}
             (when is-saving? [:> UI/Components.Design.Spinner]) " "
             (t :dialog.submit)]
            [:button.btn.btn-secondary {:on-click #(dispatch [::close-repeat-dialog])} (t :dialog.cancel)]]])))))

(defn repeat-success-notification []
  (fn []
    (let [notification-data @(subscribe [::repeat-success-data])
          on-confirm #(dispatch [::confirm-repeat-success-notification])]
      (if-not (some? notification-data)
        nil
        [:> UI/Components.Design.ConfirmDialog
         {:shown true
          :title (t :success-notification.title)
          :onConfirm on-confirm
          :confirmLabel (t :success-notification.confirm)}
         [:<>
          [:p.fw-bold (t :success-notification.message {:count (-> notification-data :reservations count)})]]]))))

(defn repeat-button [rental]
  (let [loading? @(subscribe [::loading?])]
    [:button.btn.btn-secondary {:onClick #(dispatch [::open-repeat-dialog rental])}
     (when loading? [:> UI/Components.Design.Spinner]) " " (t :repeat-action-label)]))
