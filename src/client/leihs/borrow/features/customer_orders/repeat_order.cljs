(ns leihs.borrow.features.customer-orders.repeat-order
  (:require
   [shadow.resource :as rc]
   ["date-fns" :as date-fns]
   [reagent.core :as reagent]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx reg-event-db reg-sub subscribe dispatch]]
   [re-graph.core :as re-graph]
   [leihs.core.core :refer [dissoc-in]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.translate :as translate :refer [t set-default-translate-path]]
   [leihs.borrow.lib.form-helpers :refer [UiDatepicker]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.rental-show.repeat-order)

(reg-event-db
 ::open-repeat-dialog
 (fn-traced [db]
   (assoc-in db [::data :repeat-dialog] {})))

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
     {:alert (str "FAIL! " (pr-str errors))}
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

(defn repeat-dialog [rental reservations user-id date-fns-locale]
  (let [format-date #(date-fns/format % "P" #js {:locale date-fns-locale})
        parse-date #(date-fns/parse % "P" (js/Date.) #js {:locale date-fns-locale})
        total-quantity (:total-quantity rental)
        today (date-fns/startOfToday)
        max-date (date-fns/addYears today 10)
        start-date (reagent/atom (format-date today))
        end-date (reagent/atom (format-date (date-fns/addDays today 1)))
        validation-result (reagent/atom {:valid? true})
        validate-dates (fn [start-date-input, end-date-input]
                         (if (or (empty? start-date-input) (empty? end-date-input))
                           {:valid? false}
                           (let [tmp-start-date (parse-date start-date-input)
                                 tmp-end-date (parse-date end-date-input)]
                             (cond
                               (not (and (date-fns/isValid tmp-start-date) (date-fns/isValid tmp-end-date)))
                               {:valid? false}
                               (> (h/spy tmp-start-date) (h/spy tmp-end-date))
                               {:valid? false :date-message (t :dialog.validation.start-after-end)}
                               (< tmp-start-date today)
                               {:valid? false :date-message (t :dialog.validation.start-date-in-past)}
                               (> tmp-end-date max-date)
                               {:valid? false :date-message (t :dialog.validation.end-date-too-late {:maxDate max-date})}
                               :else
                               {:valid? true}))))
        change-dates (fn [start-date-input, end-date-input]
                       (reset! start-date start-date-input)
                       (reset! end-date end-date-input)
                       (reset! validation-result (validate-dates start-date-input end-date-input)))
        change-start-date (fn [e]
                            (change-dates (-> e .-target .-value) @end-date))
        change-end-date (fn [e]
                          (change-dates @start-date (-> e .-target .-value)))
        get-quantity (fn [reservations filter-pred]
                       (->> reservations (filter filter-pred) (map :quantity) (reduce +)))]
    (fn [rental reservations user-id date-fns-locale]
      (let [dialog-data @(subscribe [::repeat-dialog-data])
            current-profile-name @(subscribe [::current-profile-name])
            is-saving? (:is-saving? dialog-data)
            rental-id (:id rental)
            options-quantity (get-quantity reservations #(-> % :option))
            models-quantity (get-quantity reservations #(-> % :option not))]
        (if-not dialog-data
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
                                 (when (-> e .-target .checkValidity)
                                   (dispatch [::mutate
                                              {:id rental-id
                                               :startDate (h/date-format-day (parse-date @start-date))
                                               :endDate (h/date-format-day (parse-date @end-date))
                                               :userId user-id}])))
                    :no-validate true
                    :auto-complete :off
                    :id :the-form}
             (if (= models-quantity 0)
               [:> UI/Components.Design.Stack {:space 4}
                [:> UI/Components.Design.Warning {:class "fs-2"}
                 (t :dialog.error-only-options)]]
               [:> UI/Components.Design.Stack {:space 4}
                [:> UI/Components.Design.Section
                 [:p (t :dialog.info {:count models-quantity})]
                 (when (> options-quantity 0)
                   [:> UI/Components.Design.Warning {:class "fs-2"}
                    (t :dialog.warning-some-options {:count options-quantity})])]
                [:> UI/Components.Design.Section {:title (t :dialog.order-for) :collapsible true}
                 current-profile-name]
                [:> UI/Components.Design.Section {:title (t :dialog.time-span) :collapsible true}
                 [:fieldset
                  [:legend.visually-hidden (t :dialog.time-span)]
                  [:div.d-flex.flex-column.gap-3
                   [UiDatepicker
                    {:locale date-fns-locale
                     :name "start-date"
                     :id "start-date"
                     :value @start-date
                     :on-change change-start-date
                     :placeholder (t :dialog.undefined)
                     :min-date today
                     :max-date max-date
                     :label (reagent/as-element [:label {:html-for "start-date"} (t :dialog.from)])}]
                   [UiDatepicker
                    {:locale date-fns-locale
                     :name "end-date"
                     :id "end-date"
                     :value @end-date
                     :on-change change-end-date
                     :placeholder (t :dialog.undefined)
                     :min-date today
                     :max-date max-date
                     :label (reagent/as-element [:label {:html-for "end-date"} (t :dialog.until)])}]
                   (when (:date-message @validation-result)
                     [:> UI/Components.Design.Warning
                      (:date-message @validation-result)])]]]])]]
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
          [:p (t :success-notification.message {:count (-> notification-data :reservations count)})]]]))))
