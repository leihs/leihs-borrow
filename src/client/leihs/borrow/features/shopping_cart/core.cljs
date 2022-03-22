(ns leihs.borrow.features.shopping-cart.core
  (:require
   ["date-fns" :as datefn]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [clojure.string :as string]
   [reagent.core :as reagent]
   [reagent.impl.template :as rtpl]
   [re-graph.core :as re-graph]
   [re-frame.std-interceptors :refer [path]]
   [shadow.resource :as rc]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.form-helpers :refer [UiTextarea]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [t dict set-default-translate-path]]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.models.filter-modal :as filter-modal]
   [leihs.core.core :refer [dissoc-in presence]]
   [leihs.borrow.features.shopping-cart.timeout :as timeout]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.model-show.availability :as availability]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.shopping-cart)

(def max-date availability/max-date)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/shopping-cart
 (fn-traced [{:keys [db]} [_ _]]
   {:db (assoc db ::edit-mode nil)
    :dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/shopping_cart/getShoppingCart.gql")
               {:userId (current-user/get-current-profile-id db)}
               [::on-fetched-data]]}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (assoc-in [:ls ::data]
                 (get-in data [:current-user :user :unsubmitted-order]))
       (cond-> errors (assoc ::errors errors)))))

(defn pool-ids-with-borrowable-quantity [db]
  (let [quants (get-in db [::edit-mode
                           :model
                           :total-borrowable-quantities])]
    (->> quants
         (filter #(-> % :quantity (> 0)))
         (map #(-> % :inventory-pool :id)))))

(reg-event-fx
 ::fetch-availability
 (fn-traced [{:keys [db]} [_ start-date end-date]]
   (let [edit-mode (get-in db [::edit-mode])
         user-id (-> edit-mode :user-id)
         model (:model edit-mode)
         exclude-reservation-ids (map :id (:res-lines edit-mode))
         pool-ids (pool-ids-with-borrowable-quantity db)
         start-date-exceeds-max? (> (js/Date. start-date) max-date)
         end-or-max-date (if (> (js/Date. end-date) max-date)
                           (h/date-format-day max-date)
                           end-date)]
     (cond
       (empty? pool-ids)
       {:db (assoc-in db [::edit-mode :availability] [])}
       start-date-exceeds-max?
       {:db (update-in db [::edit-mode]
                       #(availability/set-loading-as-ended % end-date))}
       :else
       {:db (assoc-in db [::edit-mode :fetching-until-date] end-date)
        :dispatch [::re-graph/query
                   (rc/inline "leihs/borrow/features/model_show/getAvailability.gql")
                   {:modelId (:id model)
                    :userId user-id
                    :poolIds pool-ids
                    :startDate start-date
                    :endDate end-or-max-date
                    :excludeReservationIds exclude-reservation-ids}
                   [::on-fetched-availability end-date]]}))))

(reg-event-db
 ::on-fetched-availability
 (fn-traced [db
             [_ end-date {{{new-availability :availability} :model} :data
                          errors :errors}]]
   (-> db
       (cond-> errors (assoc-in [::errors] errors))
       (update-in [::edit-mode]
                  #(when %
                     (-> %
                         (availability/update-availability new-availability)
                         (availability/set-loading-as-ended end-date)))))))

(reg-event-fx
 ::ensure-availability-fetched-until
 (fn-traced [{:keys [db]} [_ requested-date]]
   (let [edit-mode (get-in db [::edit-mode])
         max-fetched-or-fetching (js/Date. (or (:fetching-until-date edit-mode) (:fetched-until-date edit-mode)))
         range-start (datefn/addDays max-fetched-or-fetching 1)
         range-end (availability/with-future-buffer requested-date)]
     (when (datefn/isAfter requested-date max-fetched-or-fetching)
       {:dispatch [::fetch-availability
                   (-> range-start h/date-format-day)
                   (-> range-end h/date-format-day)]}))))
(reg-event-db
 ::open-delete-dialog
 (fn-traced [db]
   (assoc-in db [::data :delete-dialog] {})))

(reg-event-db
 ::close-delete-dialog
 (fn-traced [db]
   (dissoc-in db [::data :delete-dialog])))

(reg-event-fx
 ::delete-reservations
 (fn-traced [{:keys [db]} [_ ids]]
   {:db (-> db
            (assoc-in [:ls ::data :pending-count] (- 0 (count ids)))
            (assoc-in [::edit-mode] nil))
    :dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/features/shopping_cart/deleteReservationLines.gql")
               {:ids ids}
               [::on-delete-reservations]]}))

(reg-event-fx
 ::delete-all-reservations
 (fn-traced [{:keys [db]} [_ ids]]
   {:db (-> db
            (assoc-in [:ls ::data :pending-count] (- 0 (count ids)))
            (assoc-in [::data :delete-dialog :is-saving?] true))
    :dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/features/shopping_cart/deleteReservationLines.gql")
               {:ids ids}
               [::on-delete-reservations]]}))

(reg-event-fx
 ::on-delete-reservations
 (fn-traced [{:keys [db]} [_ {{ids :delete-reservation-lines} :data errors :errors}]]
   (if errors
     {:db (dissoc-in db [:ls ::data :pending-count])
      :alert (str "FAIL! " (pr-str errors))}
     {:db (-> db
              (update-in [:ls ::data :reservations]
                         (partial filter #(->> % :id ((set ids)) not)))
              (dissoc-in [:ls ::data :pending-count])
              (dissoc-in [::data :delete-dialog]))
      :dispatch [::timeout/refresh]})))

(reg-event-fx
 ::edit-reservation
 (fn-traced [{:keys [db]} [_ res-lines]]
   (when-not (get-in db [::edit-mode]) ; do nothing if already editing (double event dispatch)
     (let [now (js/Date.)
           res-line (first res-lines)
           user-id (-> res-line :user :id)
           model (:model res-line)
           inventory-pool (:inventory-pool res-line)
           start-date (:start-date res-line)
           end-date (:end-date res-line)
           quantity (count res-lines)
           start-of-current-month (datefn/startOfMonth now)
           fetch-until-date (-> (datefn/parseISO end-date)
                                availability/with-future-buffer)]
       {:db (assoc-in db [::edit-mode]
                      {:res-lines res-lines
                       :model model
                       :inventory-pool inventory-pool
                       :start-date start-date
                       :end-date end-date
                       :quantity quantity
                       :user-id user-id})
        :dispatch [::fetch-availability
                   (h/date-format-day start-of-current-month)
                   (h/date-format-day fetch-until-date)]}))))

(reg-event-db
 ::open-order-dialog
 (fn-traced [db]
   (assoc-in db [::data :order-dialog] {})))

(reg-event-db
 ::close-order-dialog
 (fn-traced [db]
   (dissoc-in db [::data :order-dialog])))

(reg-event-fx
 ::submit-order
 (fn-traced [{:keys [db]} [_ args]]
   {:db (-> db
            (assoc-in [::data :order-dialog :is-saving?] true))
    :dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/features/shopping_cart/submitOrderMutation.gql")
               (merge args {:userId (current-user/get-current-profile-id db)})
               [::on-submit-order-result]]}))

(reg-event-fx
 ::on-submit-order-result
 (fn-traced [{:keys [db]}
             [_ {{rental :submit-order} :data
                 errors :errors}]]
   (if errors
     {:alert (str "FAIL! " (pr-str errors))}
     {:db (-> db
              (assoc-in [::data :order-success] {:rental rental})
              (dissoc-in [::data :order-dialog]))})))

(reg-event-db
 ::close-order-success-notification
 (fn-traced [db]
   (dissoc-in db [::data :order-success])))

(reg-event-fx
 ::order-success-notification-confirm
 (fn-traced [_ _]
   {:fx [[:dispatch [::close-order-success-notification]]
         [:dispatch [:routing/navigate [::routes/rentals-index]]]]}))

(reg-event-fx
 ::update-reservations
 (fn-traced [{:keys [db]} [_ args]]
   (let [new-quantity (:quantity args)
         original-quantity (-> db ::edit-mode :original-quantity)]
     {:db (-> db
              (assoc-in [:ls ::data :pending-count] (- new-quantity original-quantity))
              (assoc-in [::edit-mode :is-saving?] true))
      :dispatch [::re-graph/mutate
                 (rc/inline "leihs/borrow/features/shopping_cart/updateReservations.gql")
                 args
                 [::on-update-reservations-result]]})))

(reg-event-fx
 ::on-update-reservations-result
 (fn-traced [{:keys [db]}
             [_ {:keys [errors] {del-ids :delete-reservation-lines
                                 new-res-lines :create-reservation} :data}]]
   (if errors
     {:alert (str "FAIL! " (pr-str errors))}
     {:db (-> db
              (dissoc-in [:ls ::data :pending-count])
              (assoc ::edit-mode nil)
              (update-in [:ls ::data :reservations]
                         (fn [rs]
                           (as-> rs <>
                             (filter #(->> % :id ((set del-ids)) not) <>)
                             (into <> new-res-lines)))))
      :dispatch [::timeout/refresh]})))

(reg-event-db ::cancel-edit
              (fn-traced [co _] (assoc co ::edit-mode nil)))

(reg-sub ::data
         (fn [db _] (-> db :ls ::data)))

(reg-sub ::empty?
         :<- [::data]
         (fn [d _] (-> d :reservations empty?)))

(reg-sub ::errors
         (fn [db _] (::errors db)))

(reg-sub ::edit-mode-data
         (fn [data _] (::edit-mode data)))

(reg-sub ::reservations
         :<- [::data]
         (fn [co _] (:reservations co)))

(reg-sub ::reservations-grouped
         :<- [::reservations]
         (fn [lines _]
           (->> lines
                (group-by
                 (fn [line]
                   [(get-in line [:start-date])
                    (get-in line [:inventory-pool :id])
                    (get-in line [:model :id])
                    (get-in line [:end-date])]))
                (sort-by
                 (fn [[_ [line0]]]
                   [(get-in line0 [:start-date])
                    (get-in line0 [:inventory-pool :name])
                    (get-in line0 [:model :name])
                    (get-in line0 [:end-date])])))))

(reg-sub ::current-profile
         :<- [::current-user/current-profile]
         (fn [current-profile _] current-profile))

(reg-sub ::can-change-profile?
         :<- [::current-user/can-change-profile?]
         (fn [current-profile _] current-profile))

(reg-sub ::user-id
         :<- [::data]
         (fn [d _] (:user-id d)))

(reg-sub ::user-locale
         :<- [::current-user/locale]
         (fn [l _] l))

(reg-sub ::delete-dialog-data
         (fn [db _] (-> db (get-in [::data :delete-dialog]))))

(reg-sub ::order-dialog-data
         (fn [db _] (-> db (get-in [::data :order-dialog]))))

(reg-sub ::order-success-data
         (fn [db _] (-> db (get-in [::data :order-success]))))

(defn order-panel-texts []
  ;; NOTE: maybe add a helper function for this in lib.translate?
  {:label (clj->js (get-in dict [:borrow :order-panel :label]))
   :validate (clj->js (get-in dict [:borrow :order-panel :validate]))})

(defn edit-dialog []
  (let [form-valid? (reagent/atom false)]

    (fn []
      (let [now (js/Date.)
            edit-mode-data @(subscribe [::edit-mode-data])
            current-profile @(subscribe [::current-profile])
            can-change-profile? @(subscribe [::can-change-profile?])
            profile-name (when can-change-profile? (:name current-profile))
            res-lines (:res-lines edit-mode-data)
            user-locale @(subscribe [::user-locale])
            user-id (:user-id edit-mode-data)
            model (:model edit-mode-data)
            loading? (nil? (:availability edit-mode-data))
            availability (or (:availability edit-mode-data) [])
            start-date (datefn/parseISO (:start-date edit-mode-data))
            end-date (datefn/parseISO (:end-date edit-mode-data))
            quantity (:quantity edit-mode-data)
            suspensions (:suspensions current-profile)

            ; Transformators for the pools list
            flatten-pool (fn [{{id :id name :name} :inventory-pool quantity :quantity}]
                           {:id id
                            :name name
                            :total-borrowable-quantity quantity})
            has-items-or-is-selected (fn [selected-pool-id pool]
                                       (or (> (:total-borrowable-quantity pool) 0)
                                           (= (:id pool) selected-pool-id)))
            assoc-suspension (fn [pool suspensions]
                               (let [is-suspended? (some #(= (-> % :inventory-pool :id) (-> pool :id)) suspensions)]
                                 (merge pool
                                        (when is-suspended? {:user-is-suspended true}))))
            add-inaccessible-pool (fn [selected-pool pools]
                                    (concat pools
                                            (when
                                             (and selected-pool (not-any? #(= (:id %) (:id selected-pool)) pools))
                                              [(merge selected-pool {:user-has-no-access true})])))

            pool-id-and-name (:inventory-pool edit-mode-data)
            pools (->> model :total-borrowable-quantities
                       (map flatten-pool)
                       (filter #(has-items-or-is-selected (:id pool-id-and-name) %))
                       (map #(-> % (assoc-suspension suspensions)))
                       (add-inaccessible-pool pool-id-and-name))

            fetched-until-date (-> edit-mode-data
                                   :fetched-until-date
                                   js/Date.
                                   datefn/endOfDay)
            is-saving? (:is-saving? edit-mode-data)]

        [:> UI/Components.Design.ModalDialog {:shown true
                                              :dismissible true
                                              :on-dismiss #(dispatch [::cancel-edit])
                                              :title (t :edit-dialog/dialog-title)
                                              :class "ui-booking-calendar"}
         [:> UI/Components.Design.ModalDialog.Body
          [:> UI/Components.Design.Stack {:space 4}
           [:> UI/Components.OrderPanel
            {:initialQuantity quantity
             :initialStartDate start-date
             :initialEndDate end-date
             :initialInventoryPoolId (:id pool-id-and-name)
             :inventoryPools (map h/camel-case-keys pools)
             :maxDateLoaded fetched-until-date
             :maxDateTotal max-date
             :onCalendarNavigate (fn [date-object]
                                   (let [until-date (get (js->clj date-object) "date")]
                                     (dispatch [::ensure-availability-fetched-until until-date])))
             :onDatesChange (fn [formValues]
                              (let [end-date (get (js->clj formValues) "endDate")]
                                (dispatch [::ensure-availability-fetched-until end-date])))
             :onSubmit (fn [jsargs]
                         (let [args (js->clj jsargs :keywordize-keys true)]
                           (dispatch [::update-reservations
                                      {:ids (map :id res-lines)
                                       :modelId (:id model)
                                       :startDate (h/date-format-day (:startDate args))
                                       :endDate (h/date-format-day (:endDate args))
                                       :quantity (int (:quantity args))
                                       :poolIds [(:poolId args)]
                                       :userId user-id}])))
             :onValidate (fn [v] (reset! form-valid? v))
             :modelData (h/camel-case-keys (merge model {:availability availability}))
             :profileName profile-name
             :locale user-locale
             :txt (order-panel-texts)}]
           [:> UI/Components.Design.ActionButtonGroup
            [:button.btn.btn-secondary
             {:on-click #(dispatch [::delete-reservations (map :id res-lines)])}
             (t :edit-dialog/delete-reservation)]]]]
         [:> UI/Components.Design.ModalDialog.Footer
          [:button.btn.btn-primary
           {:form "order-dialog-form" :type :submit :disabled (or loading? is-saving?) :class (when (not @form-valid?) "disabled pe-auto")}
           (when is-saving? [:> UI/Components.Design.Spinner]) " "
           (t :edit-dialog/confirm)]
          [:button.btn.btn-secondary {:on-click #(dispatch [::cancel-edit])}
           (t :edit-dialog/cancel)]]]))))

(defn reservation [res-lines invalid-res-ids]
  (let [exemplar (first res-lines)
        model (:model exemplar)
        quantity (count res-lines)
        pool-names (->> res-lines
                        (map (comp :name :inventory-pool))
                        distinct
                        (clojure.string/join ", "))
        invalid? (every? invalid-res-ids (map :id res-lines))
        start-date (js/Date. (:start-date exemplar))
        end-date (js/Date. (:end-date exemplar))
        ;; NOTE: should be in API
        total-days (+ 1 (datefn/differenceInCalendarDays end-date start-date))
        duration (t :line.duration {:totalDays total-days :fromDate start-date})
        action-props {:on-click #(dispatch [::edit-reservation res-lines])}]
    [:div
     [:> UI/Components.Design.ListCard action-props

      [:> UI/Components.Design.ListCard.Title
       (str quantity "× ")
       (:name model)]

      [:> UI/Components.Design.ListCard.Body
       pool-names]

      [:> UI/Components.Design.ListCard.Foot
       [:> UI/Components.Design.Badge
        (merge action-props {:as "button" :class "stretched-link" :colorClassName (when invalid? " bg-danger")})
        duration]]]]))

(defn delegation-section []
  (let [user-data @(subscribe [::current-user/user-data])
        can-change-profile? @(subscribe [::can-change-profile?])
        cart-user-id @(subscribe [::user-id])]
    [:> UI/Components.Design.Section
     {:title (t :delegation/section-title) :collapsible true}
     (if (or (nil? cart-user-id) (= cart-user-id (:id user-data)))
       [:<> (:name user-data) (when can-change-profile? (t :!borrow.phrases.user-or-delegation-personal-postfix))]
       [:<> (->> (:delegations user-data)
                 (filter #(= cart-user-id (:id %)))
                 first
                 :name)])]))


(defn countdown []
  (reagent/with-let [now (reagent/atom (js/Date.))
                     timer-fn  (js/setInterval #(reset! now (js/Date.)) 1000)]
    (let [data @(subscribe [::data])
          valid-until (-> data :valid-until datefn/parseISO)
          total-minutes 30
          remaining-seconds  (max 0 (datefn/differenceInSeconds valid-until @now))
          remaining-minutes (int (/ remaining-seconds 60))]
      [:> UI/Components.Design.Section {:title (t :countdown/section-title) :collapsible true}
       [:> UI/Components.Design.Stack {:space 3}
        [:> UI/Components.Design.ProgressInfo {:title (t :countdown/time-limit)
                                               :info (cond (<= remaining-seconds 0)
                                                           (reagent/as-element [:> UI/Components.Design.Warning (t :countdown/expired)])
                                                           (<= remaining-seconds 60)
                                                           (t :countdown/time-left-last-minute)
                                                           :else
                                                           (t :countdown/time-left {:minutesLeft remaining-minutes}))
                                               :totalCount (* 60 total-minutes)
                                               :doneCount (- (* 60 total-minutes) remaining-seconds)}]
        [:> UI/Components.Design.ActionButtonGroup
         [:button.btn.btn-secondary {:type "button" :on-click #(dispatch [::timeout/refresh])} (t :countdown/reset)]]]])
    (finally (js/clearInterval timer-fn))))

(defn order-dialog []
  (let [purpose (reagent/atom {:value ""})
        title (reagent/atom {:value ""})
        linked? (reagent/atom true)
        form-validated? (reagent/atom false)]
    (fn []
      (let [dialog-data @(subscribe [::order-dialog-data])
            is-saving? (:is-saving? dialog-data)]
        [:> UI/Components.Design.ModalDialog {:id :confirm-order
                                              :shown (some? dialog-data)
                                              :dismissible true
                                              :on-dismiss #(dispatch [::close-order-dialog])
                                              :title (t :confirm-dialog/dialog-title)
                                              :class "ui-confirm-order-dialog"}
         [:> UI/Components.Design.ModalDialog.Body
          [:form
           {:on-submit (fn [e]
                         (-> e .preventDefault)
                         (reset! form-validated? true)
                         (when (-> e .-target .checkValidity)
                           (dispatch [::submit-order
                                      {:purpose (:value @purpose)
                                       :title (:value @title)}])))
            :no-validate true
            :auto-complete :off
            :id :the-form
            :class (when @form-validated? "was-validated")}
           [:> UI/Components.Design.Stack {:space "4"}

            [:> UI/Components.Design.Section
             {:title (t :confirm-dialog/title)
              :collapsible true
              :class (when (:was-validated @title) "was-validated")}
             [:label {:htmlFor :title, :class "visually-hidden"} (t :confirm-dialog/title)]
             [:input.form-control
              {:type :text
               :name :title
               :id :title
               :required true
               :value (:value @title)
               :on-change (fn [e]
                            (let [v (-> e .-target .-value)]
                              (swap! title assoc :value v)
                              (when @linked? (swap! purpose assoc :value v))))
               :on-blur #(swap! title assoc :was-validated true)}]]

            [:> UI/Components.Design.Section
             {:title (t :confirm-dialog/purpose)
              :collapsible true
              :class (when (:was-validated @purpose) "was-validated")}
             [:label {:htmlFor :purpose, :class "visually-hidden"} (t :confirm-dialog/purpose)]
             [UiTextarea
              {:maxRows 15
               :minRows 3
               :name :purpose
               :id :purpose
               :class "form-control"
               :required true
               :value (:value @purpose)
               :on-change (fn [e]
                            (swap! purpose assoc :value (-> e .-target .-value))
                            (reset! linked? false))
               :on-blur #(swap! purpose assoc :was-validated true)}]]]]]
         [:> UI/Components.Design.ModalDialog.Footer
          [:button.btn.btn-primary {:form :the-form :type :submit}
           (when is-saving? [:> UI/Components.Design.Spinner]) " "
           (t :confirm-dialog/confirm)]
          [:button.btn.btn-secondary {:on-click #(dispatch [::close-order-dialog])} (t :confirm-dialog/cancel)]]]))))

(defn order-success-notification []
  (fn []
    (let [notification-data @(subscribe [::order-success-data])
          {rental :rental} notification-data
          {:keys [title purpose]} rental
          on-confirm #(dispatch [::order-success-notification-confirm])]
      [:> UI/Components.Design.ConfirmDialog
       {:shown (some? notification-data)
        :title (t :order-success-notification/title)
        :onConfirm on-confirm}
       [:<>
        [:p (t :order-success-notification/order-submitted)]
        [:> UI/Components.Design.Section {:title title :collapsible true}
         [:p purpose]
         [:p.small (rentals/rental-summary-text rental)]]]])))

(defn delete-dialog []
  (fn [reservations]
    (let [dialog-data @(subscribe [::delete-dialog-data])]
      [:> UI/Components.Design.ConfirmDialog
       {:shown (some? dialog-data)
        :title (t :delete-dialog/dialog-title)
        :onConfirm #(dispatch [::delete-all-reservations (map :id reservations)])
        :confirmLabel (t :delete-dialog/confirm)
        :confirmIsLoading (:is-saving? dialog-data)
        :dismissible true
        :onDismiss #(dispatch [::close-delete-dialog])
        :onCancel #(dispatch [::close-delete-dialog])
        :cancelLabel (t :delete-dialog/cancel)}
       [:p (t :delete-dialog/really-delete-order)]])))

(defn view []
  (fn []
    (let [data @(subscribe [::data])
          invalid-res-ids (set (:invalid-reservation-ids data))
          errors @(subscribe [::errors])
          reservations @(subscribe [::reservations])
          grouped-reservations @(subscribe [::reservations-grouped])
          edit-mode-data @(subscribe [::edit-mode-data])
          is-loading? (not (or data errors))]

      [:<>
       (cond
         is-loading? [ui/loading]

         errors [ui/error-view errors]

         (empty? grouped-reservations)
         [:<>
          [:> UI/Components.Design.PageLayout.Header {:title  (t :order-overview)}]
          [:> UI/Components.Design.Stack {:space 4 :class "text-center"}
           (t :empty-order)
           [:a.text-decoration-underline {:href (routing/path-for ::routes/home)}
            (t :borrow-items)]]]

         :else
         [:<>
          [:> UI/Components.Design.PageLayout.Header {:title  (t :order-overview)}]

          [order-dialog]

          [order-success-notification]

          (when edit-mode-data [edit-dialog])

          [delete-dialog reservations]

          [:> UI/Components.Design.Stack {:space 5}

           [countdown]

           [delegation-section]

           [:> UI/Components.Design.Section {:title (t :line/section-title) :collapsible true}
            [:> UI/Components.Design.ListCard.Stack
             (doall
              (for [[grouped-key res-lines] grouped-reservations]
                [:<> {:key grouped-key}
                 [reservation res-lines invalid-res-ids]]))]]

           [:> UI/Components.Design.Section
            [:> UI/Components.Design.ActionButtonGroup
             (when (> (count invalid-res-ids) 0)
               [:> UI/Components.Design.Warning
                (t :line/invalid-items-warning {:invalidItemsCount (count invalid-res-ids)})])
             [:button.btn.btn-primary
              {:type "button"
               :disabled (seq invalid-res-ids)
               :on-click #(dispatch [::open-order-dialog])}
              (t :confirm-order)]
             [:button.btn.btn-secondary
              {:type "button"
               :on-click #(dispatch [::open-delete-dialog])}
              (t :delete-order)]]]]])])))
