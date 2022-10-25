(ns leihs.borrow.features.templates.apply-template
  (:require
   [shadow.resource :as rc]
   ["date-fns" :as date-fns]
   [reagent.core :as reagent]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx reg-event-db reg-sub subscribe dispatch]]
   [re-graph.core :as re-graph]
   [leihs.core.core :refer [dissoc-in]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.translate :as translate :refer [t set-default-translate-path]]
   [leihs.borrow.lib.form-helpers :refer [UiDateRangePicker]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.templates.apply)

(reg-event-db
 ::open-dialog
 (fn-traced [db]
   (assoc-in db [::data :dialog] {:show true})))

(reg-event-db
 ::close-dialog
 (fn-traced [db]
   (dissoc-in db [::data :dialog])))

(reg-event-fx
 ::mutate
 (fn-traced [{:keys [db]} [_ args]]
   {:db (-> db
            (assoc-in [::data :dialog :is-saving?] true))
    :dispatch [::re-graph/mutate
               (str
                (rc/inline "leihs/borrow/features/templates/apply.gql") "\n"
                (rc/inline "leihs/borrow/features/shopping_cart/fragment_unsubmittedOrderProps.gql"))
               args
               [::on-mutate-result]]}))

(reg-event-fx
 ::on-mutate-result
 (fn-traced [{:keys [db]}
             [_ {{reservations :apply-template refresh-timeout-data :refresh-timeout} :data
                 errors :errors}]]
   (if errors
     {:db (-> db
              (dissoc-in [::data :dialog :is-saving?]))
      :dispatch [::errors/add-many errors]}
     {:db (-> db
              (assoc-in [:ls :leihs.borrow.features.shopping-cart.core/data]
                        (:unsubmitted-order refresh-timeout-data))
              (assoc-in [::data :success-notification] {:reservations reservations})
              (dissoc-in [::data :dialog]))})))

(reg-event-db
 ::close-success-notification
 (fn-traced [db]
   (dissoc-in db [::data :success-notification])))

(reg-event-fx
 ::confirm-success-notification
 (fn-traced [_ _]
   {:fx [[:dispatch [::close-success-notification]]
         [:dispatch [:routing/navigate [::routes/shopping-cart]]]]}))

(reg-sub ::dialog-data
         (fn [db _] (-> db (get-in [::data :dialog]))))

(reg-sub ::success-notification-data
         (fn [db _] (-> db (get-in [::data :success-notification]))))

(reg-sub ::current-profile-name
         :<- [::current-user/user-data]
         :<- [::current-user/current-delegation]
         (fn [[user-data current-delegation] _]
           (if current-delegation
             (:name current-delegation)
             (str (-> user-data :name)
                  (cond (-> user-data :delegations seq)
                        (t :!borrow.phrases.user-or-delegation-personal-postfix))))))

(defn dialog [template models user-id date-fns-locale]
  (let [today (date-fns/startOfToday)
        max-date (date-fns/addYears today 10)
        selected-range (reagent/atom {:startDate today :endDate  (date-fns/addDays today 1)})
        validation-result (reagent/atom {:valid? true})
        validate-dates (fn [start-date, end-date]
                         (cond
                           (not (and (date-fns/isValid start-date) (date-fns/isValid end-date)))
                           {:valid? false}
                           (> start-date end-date)
                           {:valid? false :date-message (t :dialog.validation.start-after-end)}
                           (< start-date today)
                           {:valid? false :date-message (t :dialog.validation.start-date-in-past)}
                           (> end-date max-date)
                           {:valid? false :date-message (t :dialog.validation.end-date-too-late {:maxDate max-date})}
                           :else
                           {:valid? true}))
        change-selected-range (fn [r]
                                (let [start-date (-> r .-startDate)
                                      end-date (-> r .-endDate)]
                                  (reset! selected-range {:startDate start-date :endDate end-date})
                                  (reset! validation-result (validate-dates start-date end-date))))
        get-quantity (fn [reservations filter-pred]
                       (->> reservations (filter filter-pred) (map :quantity) (reduce +)))]
    (fn [template models user-id date-fns-locale]
      (let [dialog-data @(subscribe [::dialog-data])
            current-profile-name @(subscribe [::current-profile-name])
            is-saving? (:is-saving? dialog-data)
            template-id (:id template)
            models-quantity (get-quantity models #(-> % :is-reservable))]
        (if-not dialog-data
          nil
          [:> UI/Components.Design.ModalDialog
           {:id :apply-template
            :shown true
            :dismissible true
            :on-dismiss #(dispatch [::close-dialog])
            :title (t :dialog.title)
            :class "ui-apply-template-dialog"}
           [:> UI/Components.Design.ModalDialog.Body
            [:form {:on-submit (fn [e]
                                 (-> e .preventDefault)
                                 (when (-> e .-target .checkValidity)
                                   (dispatch [::mutate
                                              {:id template-id
                                               :startDate (h/date-format-day (:startDate @selected-range))
                                               :endDate (h/date-format-day (:endDate @selected-range))
                                               :userId user-id}])))
                    :no-validate true
                    :auto-complete :off
                    :id :the-form}
             (if (= models-quantity 0)
               [:> UI/Components.Design.Stack {:space 4}
                [:> UI/Components.Design.Warning {:class "fs-2"}
                 (t :dialog.error-no-items)]]
               [:> UI/Components.Design.Stack {:space 4}
                [:> UI/Components.Design.Section
                 [:p (t :dialog.info {:count models-quantity})]]
                [:> UI/Components.Design.Section {:title (t :dialog.order-for)}
                 current-profile-name]
                [:> UI/Components.Design.Section {:title (t :dialog.time-span)}
                 [:fieldset
                  [:legend.visually-hidden (t :dialog.time-span)]
                  [:div.d-flex.flex-column.gap-3
                   [UiDateRangePicker
                    {:locale date-fns-locale
                     :txt {:from (t :dialog.from)
                           :until (t :dialog.until)
                           :placeholderFrom (t :dialog.undefined)
                           :placeholderUntil (t :dialog.undefined)}
                     :selected-range @selected-range
                     :onChange change-selected-range
                     :min-date today
                     :max-date max-date}]
                   (when (:date-message @validation-result)
                     [:> UI/Components.Design.Warning
                      (:date-message @validation-result)])]]]])]]
           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-primary {:form :the-form :type :submit :disabled (or (= 0 models-quantity) (not (:valid? @validation-result)))}
             (when is-saving? [:> UI/Components.Design.Spinner]) " "
             (t :dialog.submit)]
            [:button.btn.btn-secondary {:on-click #(dispatch [::close-dialog])} (t :dialog.cancel)]]])))))

(defn success-notification []
  (fn []
    (let [notification-data @(subscribe [::success-notification-data])
          on-confirm #(dispatch [::confirm-success-notification])]
      (if-not (some? notification-data)
        nil
        [:> UI/Components.Design.ConfirmDialog
         {:shown true
          :title (t :success-notification.title)
          :onConfirm on-confirm
          :confirmLabel (t :success-notification.confirm)}
         [:<>
          [:p (t :success-notification.message {:count (-> notification-data :reservations count)})]]]))))
