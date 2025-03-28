(ns leihs.borrow.features.shopping-cart.core
  (:require
   ["/borrow-ui" :as UI]
   ["date-fns" :as date-fns]
   [clojure.string :as string]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.model-show.availability :as availability]
   [leihs.borrow.features.shopping-cart.timeout :as timeout]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.form-helpers :refer [UiTextarea]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.prefs :as prefs]
   [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db reg-event-fx
                                      reg-sub subscribe]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [set-default-translate-path t] :as translate]
   [leihs.borrow.translations :as translations]
   [leihs.core.core :refer [dissoc-in]]
   [re-graph.core :as re-graph]
   [reagent.core :as reagent]
   [shadow.resource :as rc]))

(set-default-translate-path :borrow.shopping-cart)

(def max-date availability/max-date)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/shopping-cart
 (fn-traced [{:keys [db]} [_ _]]
   {:db (-> db
            (assoc ::edit-mode nil)
            (assoc ::errors nil)
            (assoc ::loading true))
    :dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/shopping_cart/getShoppingCart.gql")
               {:userId (current-user/get-current-profile-id db)}
               [::on-fetched-data]]}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (if errors
     (-> db
         (assoc ::loading nil)
         (assoc ::errors errors))
     (-> db
         (assoc ::loading nil)
         (assoc-in [:ls ::data]
                   (get-in data [:current-user :user :unsubmitted-order]))
         (assoc-in [:ls ::settings]
                   (get-in data [:current-user :settings]))))))

(reg-event-db
 ::set
 (fn-traced [db [_ cart-data]]
   (assoc-in db [:ls ::data] cart-data)))

(defn pool-ids-with-reservable-quantity [db]
  (let [quants (get-in db [::edit-mode
                           :model
                           :total-reservable-quantities])]
    (->> quants
         (filter #(-> % :quantity (> 0)))
         (map #(-> % :inventory-pool :id)))))

(reg-event-fx
 ::fetch-total-reservable-quantities
 (fn-traced [{:keys [db]} [_ start-date end-date]]
   (let [edit-mode (get-in db [::edit-mode])
         user-id (-> edit-mode :user-id)
         model (:model edit-mode)]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/features/model_show/getTotalReservableQuantities.gql")
                 {:modelId (:id model), :userId user-id}
                 [::on-fetched-total-reservable-quantities start-date end-date]]})))

(reg-event-fx
 ::on-fetched-total-reservable-quantities
 (fn-traced [{:keys [db]}
             [_ start-date end-date {{{:keys [total-reservable-quantities]} :model} :data}]]
   {:db (assoc-in db [::edit-mode :model :total-reservable-quantities]
                  total-reservable-quantities)
    :dispatch [::fetch-availability start-date end-date]}))

(reg-event-fx
 ::fetch-availability
 (fn-traced [{:keys [db]} [_ start-date end-date]]
   (let [edit-mode (get-in db [::edit-mode])
         user-id (-> edit-mode :user-id)
         model (:model edit-mode)
         exclude-reservation-ids (map :id (:res-lines edit-mode))
         pool-ids (pool-ids-with-reservable-quantity db)
         start-date-exceeds-max? (> (js/Date. start-date) max-date)
         end-or-max-date (if (> (js/Date. end-date) max-date)
                           (h/date-format-day max-date)
                           end-date)]
     (cond
       (empty? pool-ids)
       {:db (assoc-in db [::edit-mode :availability] [])}
       start-date-exceeds-max?
       {:db (update-in db [::edit-mode]
                       #(availability/set-loading-as-ended % end-date false))}
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

(reg-event-fx
 ::on-fetched-availability
 (fn-traced [{:keys [db]}
             [_ end-date {{{new-availability :availability} :model} :data
                          errors :errors}]]
   (if errors
     {:db (update-in db [::edit-mode]
                     #(availability/set-loading-as-ended % end-date false))
      :dispatch [::errors/add-many errors]}
     {:db (-> db
              (update-in [::edit-mode]
                         #(when %
                            (-> %
                                (availability/update-availability new-availability)
                                (availability/set-loading-as-ended end-date true)))))})))

(reg-event-fx
 ::ensure-availability-fetched-until
 (fn-traced [{:keys [db]} [_ requested-date]]
   (let [edit-mode (get-in db [::edit-mode])
         max-fetched-or-fetching (js/Date. (or (:fetching-until-date edit-mode) (:fetched-until-date edit-mode)))
         range-start (date-fns/addDays max-fetched-or-fetching 1)
         range-end (availability/with-future-buffer requested-date)]
     (when (date-fns/isAfter requested-date max-fetched-or-fetching)
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
            (assoc-in [:ls ::data :pending-count] (- 0 (count ids))))
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
     {:db (-> db
              (dissoc-in [:ls ::data :pending-count])
              (dissoc-in [::data :delete-dialog :is-saving?]))
      :dispatch [::errors/add-many errors]}
     {:db (-> db
              (update-in [:ls ::data :reservations]
                         (partial filter #(->> % :id ((set ids)) not)))
              (dissoc-in [:ls ::data :pending-count])
              (dissoc-in [::data :delete-dialog])
              (dissoc-in [::edit-mode]))
      :dispatch [::timeout/refresh]})))

(reg-event-fx
 ::edit-reservation
 (fn-traced [{:keys [db]} [_ res-lines]]
    ; do nothing if already editing (double event dispatch)
   (when-not (get-in db [::edit-mode :model :total-reservable-quantities])
     (let [now (js/Date.)
           res-line (first res-lines)
           user-id (-> res-line :user :id)
           model (:model res-line)
           inventory-pool (:inventory-pool res-line)
           start-date (:start-date res-line)
           end-date (:end-date res-line)
           quantity (count res-lines)
           start-of-current-month (date-fns/startOfMonth now)
           fetch-until-date (-> (date-fns/parseISO end-date)
                                availability/with-future-buffer)]
       {:db (assoc-in db [::edit-mode]
                      {:res-lines res-lines
                       :model model
                       :inventory-pool inventory-pool
                       :start-date start-date
                       :end-date end-date
                       :quantity quantity
                       :user-id user-id})
        :dispatch [::fetch-total-reservable-quantities
                   (h/date-format-day start-of-current-month)
                   (h/date-format-day fetch-until-date)]}))))

(reg-event-db
 ::open-order-dialog
 (fn-traced [db]
   (let [settings-data (get-in db [:ls ::settings])
         settings {:show-contact-details? (:show-contact-details-on-customer-order settings-data)
                   :show-lending-terms-acceptance? (:lending-terms-acceptance-required-for-order settings-data)
                   :lending-terms-url (:lending-terms-url settings-data)}]
     (assoc-in db [::data :order-dialog] (merge {:show true} settings)))))

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
     {:db (-> db
              (dissoc-in [::data :order-dialog :is-saving?]))
      :dispatch [::errors/add-many errors]}
     {:db (-> db
              (assoc-in [::data :order-success] {:rental rental})
              (dissoc-in [::data :order-dialog]))})))

(reg-event-db
 ::close-order-success-notification
 (fn-traced [db]
   (-> db
       (dissoc-in [::data :order-success])
       (assoc-in [:ls ::data :reservations] []))))

(reg-event-fx
 ::order-success-notification-confirm
 (fn-traced [_ _]
   {:fx [[:dispatch [::close-order-success-notification]]
         [:dispatch [:routing/navigate [::routes/rentals-index {:query-params {:tab "open-orders"}}]]]]}))

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
     {:db (-> db (dissoc-in [::edit-mode :is-saving?]))
      :dispatch [::errors/add-many errors]}
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

(reg-sub ::delete-dialog-data
         (fn [db _] (-> db (get-in [::data :delete-dialog]))))

(reg-sub ::order-dialog-data
         (fn [db _] (-> db (get-in [::data :order-dialog]))))

(reg-sub ::order-success-data
         (fn [db _] (-> db (get-in [::data :order-success]))))

(reg-sub ::valid-until
         (fn [db _] (-> db (get-in [::data :valid-until]))))

(reg-sub ::loading
         (fn [db _] (-> db (get ::loading))))

(reg-sub ::settings
         (fn [db _] (-> db (get-in [:ls ::settings]))))

(defn order-panel-texts []
  ;; NOTE: maybe add a helper function for this in lib.translate?
  {:label (clj->js (get-in translations/dict [:borrow :order-panel :label]))
   :validate (clj->js (get-in translations/dict [:borrow :order-panel :validate]))})

(defn edit-dialog []
  (let [form-valid? (reagent/atom false)]

    (fn []
      (let [now (js/Date.)
            edit-mode-data @(subscribe [::edit-mode-data])
            current-profile @(subscribe [::current-profile])
            can-change-profile? @(subscribe [::can-change-profile?])
            profile-name (when can-change-profile? (:name current-profile))
            res-lines (:res-lines edit-mode-data)
            text-locale @(subscribe [::translate/text-locale])
            date-locale @(subscribe [::translate/date-locale])
            user-id (:user-id edit-mode-data)
            model (:model edit-mode-data)
            loading? (nil? (:availability edit-mode-data))
            availability (or (:availability edit-mode-data) [])
            start-date (date-fns/parseISO (:start-date edit-mode-data))
            end-date (date-fns/parseISO (:end-date edit-mode-data))
            quantity (:quantity edit-mode-data)
            suspensions (:suspensions current-profile)

            ; Transformators for the pools list
            flatten-pool (fn [{{id :id name :name} :inventory-pool quantity :quantity}]
                           {:id id
                            :name name
                            :total-reservable-quantity quantity})
            has-items-or-is-selected (fn [selected-pool-id pool]
                                       (or (> (:total-reservable-quantity pool) 0)
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
            pools (->> model :total-reservable-quantities
                       (map flatten-pool)
                       (filter #(has-items-or-is-selected (:id pool-id-and-name) %))
                       (map #(-> % (assoc-suspension suspensions)))
                       (add-inaccessible-pool pool-id-and-name))

            fetched-until-date (-> edit-mode-data
                                   :fetched-until-date
                                   js/Date.
                                   date-fns/endOfDay)
            is-saving? (:is-saving? edit-mode-data)
            show-day-quants @(subscribe [::prefs/show-day-quants])
            on-show-day-quants-change #(dispatch [::prefs/set-show-day-quants %])]

        [:> UI/Components.Design.ModalDialog {:shown true
                                              :dismissible true
                                              :on-dismiss #(dispatch [::cancel-edit])
                                              :title (t :edit-dialog/dialog-title)
                                              :class "ui-booking-calendar"}
         [:> UI/Components.Design.ModalDialog.Body
          [:div.d-grid.gap-4
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
             :initialShowDayQuants (or show-day-quants false)
             :onShowDayQuantsChange on-show-day-quants-change
             :profileName profile-name
             :locale text-locale
             :dateLocale date-locale
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

(defn reservation [res-lines invalid-res-ids date-locale]
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
        total-days (+ 1 (date-fns/differenceInCalendarDays end-date start-date))
        imgSrc (or (get-in model [:cover-image :image-url])
                   (get-in model [:images 0 :image-url]))]
    [:div
     [:> UI/Components.Design.ListCard
      {:on-click #(dispatch [::edit-reservation res-lines])
       :img (reagent/as-element [:> UI/Components.Design.SquareImage {:imgSrc imgSrc :paddingClassName "p-0"}])}
      [:> UI/Components.Design.ListCard.Title
       (str quantity "× ")
       (:name model)]

      [:> UI/Components.Design.ListCard.Body
       [:div pool-names]
       [:div {:class (when invalid? "text-danger")}
        (h/format-date-range start-date end-date date-locale)
        " (" (t :line.duration-days {:totalDays total-days}) ")"]]]]))

(defn delegation-section []
  (let [user-data @(subscribe [::current-user/user-data])
        can-change-profile? @(subscribe [::can-change-profile?])
        cart-user-id @(subscribe [::user-id])]
    [:> UI/Components.Design.Section
     {:title (t :delegation/section-title) :collapsible false}
     (if (or (nil? cart-user-id) (= cart-user-id (:id user-data)))
       [:div.fw-bold (:name user-data) (when can-change-profile? (t :!borrow.phrases.user-or-delegation-personal-postfix))]
       [:div.fw-bold (->> (:delegations user-data)
                          (filter #(= cart-user-id (:id %)))
                          first
                          :name)])]))

(defn countdown []
  (reagent/with-let [now (reagent/atom (js/Date.))
                     timer-fn  (js/setInterval #(reset! now (js/Date.)) 1000)]
    (let [data @(subscribe [::data])
          settings @(subscribe [::settings])
          valid-until (-> data :valid-until date-fns/parseISO)
          total-minutes (-> settings :timeout-minutes)
          remaining-seconds  (max 0 (date-fns/differenceInSeconds valid-until @now))
          remaining-minutes (-> remaining-seconds (/ 60) (js/Math.ceil))
          waiting? @(subscribe [::timeout/waiting])]
      [:> UI/Components.Design.Section {:title (t :countdown/section-title) :collapsible false}
       [:div.d-grid.gap-3
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
         [:button.btn.btn-secondary {:type "button" :on-click #(dispatch [::timeout/refresh])}
          (when waiting? [:<> [:> UI/Components.Design.Spinner] " "])
          (t :countdown/reset)]]]])
    (finally (js/clearInterval timer-fn))))

(defn no-valid-items []
  [:> UI/Components.Design.Section {:title (t :countdown/section-title) :collapsible false}
   [:> UI/Components.Design.ProgressInfo {:title (t :countdown/no-valid-items)}]])

(defn order-dialog []
  (let [purpose (reagent/atom {:value ""})
        title (reagent/atom {:value ""})
        title-purpose-linked? (reagent/atom true)
        contact-details (reagent/atom {:value ""})
        lending-terms-accepted (reagent/atom {:value false})
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
                                       :title (:value @title)
                                       :contactDetails (when (:show-contact-details? dialog-data) (:value @contact-details))
                                       :lendingTermsAccepted (when (:show-lending-terms-acceptance? dialog-data) (:value @lending-terms-accepted))}])))
            :no-validate true
            :auto-complete :off
            :id :the-form
            :class (when @form-validated? "was-validated")}
           [:div.d-grid.gap-4

            [:> UI/Components.Design.Section
             {:title (t :confirm-dialog/title)
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
                              (when @title-purpose-linked? (swap! purpose assoc :value v))))
               :on-blur #(swap! title assoc :was-validated true)}]
             [:> UI/Components.Design.InfoMessage {:class "mt-2"} (t :confirm-dialog/title-hint)]]

            [:> UI/Components.Design.Section
             {:title (t :confirm-dialog/purpose)
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
                            (reset! title-purpose-linked? false))
               :on-blur #(swap! purpose assoc :was-validated true)}]
             [:> UI/Components.Design.InfoMessage {:class "mt-2"} (t :confirm-dialog/purpose-hint)]]
            (when (:show-contact-details? dialog-data)
              [:> UI/Components.Design.Section
               {:title (t :confirm-dialog/contact-details)
                :class (when (:was-validated @contact-details) "was-validated")}
               [:label {:htmlFor :contact-details, :class "visually-hidden"} (t :confirm-dialog/contact-details)]
               [:input.form-control
                {:type :text
                 :name :contact-details
                 :id :contact-details
                 :value (:value @contact-details)
                 :max-length 1000
                 :on-change (fn [e]
                              (let [v (-> e .-target .-value)]
                                (swap! contact-details assoc :value v)))
                 :on-blur #(swap! contact-details assoc :was-validated true)}]
               [:> UI/Components.Design.InfoMessage {:class "mt-2"} (t :confirm-dialog/contact-details-hint)]])
            (when (:show-lending-terms-acceptance? dialog-data)
              [:> UI/Components.Design.Section
               {:title (t :confirm-dialog/lending-terms)}
               [:div.form-check
                [:input.form-check-input
                 {:type :checkbox
                  :name "lending-terms-accepted"
                  :id "lending-terms-accepted"
                  :checked (:value @lending-terms-accepted)
                  :required true
                  :on-change (fn [_] (swap! lending-terms-accepted (fn [x] {:value (-> x :value not) :was-validated true})))}]
                [:label.form-check-label {:html-for "lending-terms-accepted"} (t :confirm-dialog/i-accept)
                 (when (:lending-terms-url dialog-data) ":")]
                (when-let [url (:lending-terms-url dialog-data)]
                  [:div.mt-2 [:a.decorate-links {:href url :target "_blank"} url]])]])]]]
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
        [:p.fw-bold (t :order-success-notification/order-submitted)]
        [:> UI/Components.Design.Section {:title title}
         [:p (when (not= title purpose) [:p.preserve-linebreaks purpose])]
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
          user-data @(subscribe [::current-user/user-data])
          invalid-res-ids (set (:invalid-reservation-ids data))
          errors @(subscribe [::errors])
          reservations @(subscribe [::reservations])
          grouped-reservations @(subscribe [::reservations-grouped])
          edit-mode-data @(subscribe [::edit-mode-data])
          total-reservable-quantities (-> edit-mode-data :model :total-reservable-quantities)
          is-initial-loading? (not (or data errors))
          is-loading? @(subscribe [::loading])
          refreshing-timeout? @(subscribe [::timeout/waiting])
          date-locale @(subscribe [::translate/date-locale])]

      [:> UI/Components.Design.PageLayout.ContentContainer
       (cond
         is-initial-loading? [ui/loading]

         errors [ui/error-view errors]

         (empty? grouped-reservations)
         [:<>
          [:> UI/Components.Design.PageLayout.Header {:title  (t :order-overview)}]
          [:div.d-grid.gap-4.text-center.decorate-links
           (t :empty-order)
           [:a.fw-bold {:href (routing/path-for ::routes/home)}
            (t :borrow-items)]]]

         :else
         [:<>
          [:> UI/Components.Design.PageLayout.Header {:title  (t :order-overview)}]

          [order-dialog]

          [order-success-notification]

          (when total-reservable-quantities [edit-dialog])

          [delete-dialog reservations]

          [:div.d-grid.gap-5

           (if (:valid-until data)
             [countdown]
             [no-valid-items])

           (when (not-empty (:delegations user-data))
             [delegation-section])

           [:> UI/Components.Design.Section
            {:class :position-relative
             :title (reagent/as-element
                     ^{:key "title"}
                     [:<>
                      (t :line/section-title)
                      (when (or is-loading? refreshing-timeout?)
                        [:div.position-absolute {:style {:right "0" :top "3px"}} [:> UI/Components.Design.Spinner]])])
             :collapsible true}
            [:> UI/Components.Design.ListCard.Stack
             (doall
              (for [[grouped-key res-lines] grouped-reservations]
                ^{:key grouped-key}
                [:<>
                 [reservation res-lines invalid-res-ids date-locale]]))]]

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
