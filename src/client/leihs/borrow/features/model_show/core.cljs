(ns leihs.borrow.features.model-show.core
  (:refer-clojure :exclude [val])
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-frame.std-interceptors :refer [path]]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   #_[leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.ui.icons :as icons]
   ["/leihs-ui-client-side-external-react" :as UI]
   ["date-fns" :as datefn]
   [leihs.borrow.lib.helpers :as h :refer [spy log]]
   [leihs.borrow.features.favorite-models.events :as favs]
   [leihs.borrow.features.models.filter-modal :as filter-modal]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.shopping-cart.core :as cart]
   [leihs.borrow.features.shopping-cart.timeout :as timeout]
   [leihs.borrow.features.model-show.availability :as availability]
   [leihs.core.core :refer [dissoc-in flip]]))

; TODO: 
; * separate fetching of page & calendar data
; * use plain reg-event-fx for the calendar part (no kebab)

(set-default-translate-path :borrow.model-show)

(def model-id (atom nil))

(def max-date availability/max-date)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/models-show
 (fn-traced
   [{:keys [db]} [_ args]]
   (reset! model-id (get-in args [:route-params :model-id]))
   {:dispatch [::fetch]
    :db (assoc-in db [::data :order-panel] nil)}))

(reg-event-fx
 ::fetch
 (fn-traced [{:keys [db]} _]
   {:dispatch
    [::re-graph/query
     (rc/inline "leihs/borrow/features/model_show/getModelShow.gql")
     {:modelId @model-id, :userId (current-user/get-current-profile-id db)}
     [::on-fetched-data]]}))

(defn pool-ids-with-borrowable-quantity [db model-id]
  (let [quants (get-in db
                       [:ls
                        ::data
                        model-id
                        :total-borrowable-quantities])]
    (->> quants
         (filter #(-> % :quantity (> 0)))
         (map #(-> % :inventory-pool :id)))))

(reg-event-fx
 ::on-fetched-data
 (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
   (let [now (js/Date.)
         opts (get-in db [:ls ::filter-modal/options])
         start-date (:start-date opts)
         end-date (:end-date opts)
         user-id (current-user/get-current-profile-id db)
         filter-start-date (some-> start-date datefn/parseISO)
         filter-end-date (some-> end-date datefn/parseISO)
         initial-start-date (or filter-start-date now)
         initial-end-date (or filter-end-date
                              (datefn/addDays initial-start-date 1))
         start-of-current-month (datefn/startOfMonth now)
         fetch-until-date (-> initial-end-date
                              availability/with-future-buffer)]
     {:db (-> db
              (update-in [:ls ::data @model-id] (fnil identity {}))
              (cond-> errors (assoc-in [::errors @model-id] errors))
              (assoc-in [:ls ::data @model-id] (:model data)))
      :dispatch [::fetch-availability
                 user-id
                 (h/date-format-day start-of-current-month)
                 (h/date-format-day fetch-until-date)]})))

(reg-event-fx
 ::fetch-availability
 (fn-traced [{:keys [db]} [_ user-id start-date end-date]]
   (let [model-id @model-id
         pool-ids (pool-ids-with-borrowable-quantity db model-id)
         start-date-exceeds-max? (> (js/Date. start-date) max-date)
         end-or-max-date (if (> (js/Date. end-date) max-date)
                           (h/date-format-day max-date)
                           end-date)]
     (cond
       (empty? pool-ids)
       {:db (update-in db [:ls ::data model-id]
                       #(merge %
                               {:availability []
                                :availability-ready? true}))}
       start-date-exceeds-max?
       {:db (update-in db [:ls ::data model-id]
                       #(availability/set-loading-as-ended % end-date))}
       :else
       {:db (assoc-in db
                      [:ls ::data model-id :fetching-until-date]
                      end-date)
        :dispatch [::re-graph/query
                   (rc/inline "leihs/borrow/features/model_show/getAvailability.gql")
                   {:modelId model-id
                    :userId user-id
                    :poolIds pool-ids
                    :startDate start-date
                    :endDate end-or-max-date}
                   [::on-fetched-availability end-date]]}))))

(reg-event-db
 ::on-fetched-availability
 (fn-traced [db
             [_  end-date {{{new-availability :availability} :model} :data
                           errors :errors}]]
   (-> db
       (cond-> errors (assoc-in [::errors @model-id] errors))
       (update-in [:ls ::data @model-id]
                  #(-> %
                       (availability/update-availability new-availability)
                       (availability/set-loading-as-ended end-date)
                       (assoc :availability-ready? true))))))

(reg-event-fx
 ::ensure-availability-fetched-until
 (fn-traced [{:keys [db]} [_ user-id requested-date]]
   (let [model (get-in db [:ls ::data @model-id])
         max-fetched-or-fetching (js/Date. (or (:fetching-until-date model) (:fetched-until-date model)))
         range-start (datefn/addDays max-fetched-or-fetching 1)
         range-end (availability/with-future-buffer requested-date)]
     (when (datefn/isAfter requested-date max-fetched-or-fetching)
       {:dispatch [::fetch-availability
                   user-id
                   (-> range-start h/date-format-day)
                   (-> range-end h/date-format-day)]}))))

(reg-event-db
 ::clear-availability
 (fn-traced [db _]
   (update-in db [:ls ::data @model-id]
              #(merge % {:availability []
                         :availability-ready? false}))))

(reg-event-fx
 ::favorite-model
 (fn-traced
   [{:keys [db]} [_ model-id]]
   {:db (assoc-in db [:ls ::data model-id :is-favorited] true),
    :dispatch-n (list [::favs/favorite-model model-id]
                      [::favs/invalidate-cache])}))

(reg-event-fx
 ::unfavorite-model
 (fn-traced
   [{:keys [db]} [_ model-id]]
   {:db (assoc-in db [:ls ::data model-id :is-favorited] false),
    :dispatch-n (list [::favs/unfavorite-model model-id]
                      [::favs/invalidate-cache])}))

(reg-event-db
 ::open-order-panel
 (fn-traced [db]
   (assoc-in db [::data :order-panel] {:is-open? true})))

(reg-event-db
 ::close-order-panel
 (fn-traced [db]
   (assoc-in db [::data :order-panel] nil)))

(reg-event-db
 ::order-success
 (fn-traced [db]
   (-> db
       (assoc-in [::data :order-panel] {:success? true}))))

(reg-event-fx
 ::dismiss-order-success
 (fn-traced [{:keys [db]} _]
   {:db (-> db
            (assoc-in [::data :order-panel] nil))}))

(reg-sub
 ::order-panel-data
 (fn [db]
   (get-in db [::data :order-panel])))

(reg-sub ::model-data
         (fn [db [_ id]]
           (get-in db [:ls ::data id])))

(reg-sub
 ::errors
 (fn [db [_ id]]
   (get-in db [::errors id])))

(reg-sub ::current-profile
         :<- [::current-user/current-profile]
         (fn [x _] x))

(reg-sub ::can-change-profile?
         :<- [::current-user/can-change-profile?]
         (fn [x _] x))

(reg-sub ::user-locale
         :<- [::current-user/locale]
         (fn [l _] l))

(reg-sub
 ::inventory-pools
 (fn [[_ id] _]
   [(rf/subscribe [::model-data id])
    (rf/subscribe [::current-profile])])
 (fn [[model current-profile] _]
   (letfn [(assoc-borrowable-quantity [pool]
             (assoc pool
                    :total-borrowable-quantity
                    (->> model
                         :total-borrowable-quantities
                         (filter #(-> % :inventory-pool :id (= (:id pool))))
                         first
                         :quantity)))
           (assoc-suspension [pool]
             (let [is-suspended? (some #(= (-> % :inventory-pool :id) (-> pool :id)) (:suspensions current-profile))]
               (merge pool
                      (when is-suspended? {:user-is-suspended true}))))]
     (->> model
          :availability
          (map :inventory-pool)
          (map assoc-borrowable-quantity)
          (map assoc-suspension)))))

(reg-event-fx
 ::model-create-reservation
 (fn-traced [{:keys [db]} [_ args]]
   {:db
    (-> db
        (assoc-in [:ls ::cart/data :pending-count] (:quantity args))
        (assoc-in [::data :order-panel :is-saving?] true))
    :dispatch
    [::re-graph/mutate
     (rc/inline
      "leihs/borrow/features/model_show/createReservationMutation.gql") args
     [::on-mutation-result args]]}))

(reg-event-fx
 ::on-mutation-result
 (fn-traced [{:keys [db]} [_ {user-id :userId :as args} {:keys [data errors]}]]
   (if errors
     {:db (-> db
              (assoc-in [:meta :app :fatal-errors] errors)
              (dissoc-in [:ls ::cart/data :pending-count])
              (assoc-in [::data :order-panel] nil))
      :alert (str "FAIL! " (pr-str errors))}
     {:dispatch-n (list [::clear-availability]
                        [::fetch-availability
                         user-id
                         (-> (js/Date.)
                             datefn/startOfMonth
                             h/date-format-day)
                         (-> args
                             :endDate
                             datefn/parseISO
                             availability/with-future-buffer
                             h/date-format-day)]
                        [::timeout/refresh]
                        [::order-success data])})))

(defn order-panel
  [model filters shown?]
  (let [form-valid? (reagent/atom false)]
    (fn [model filters shown?]
      (let [now (js/Date.)
            current-profile @(subscribe [::current-profile])
            can-change-profile? @(subscribe [::can-change-profile?])
            profile-name (when can-change-profile? (:name current-profile))
            user-locale @(subscribe [::user-locale])
            filter-start-date (some-> filters :start-date datefn/parseISO)
            filter-end-date (some-> filters :end-date datefn/parseISO)
            initial-start-date (or filter-start-date now)
            initial-end-date (or filter-end-date
                                 (datefn/addDays initial-start-date 1))
            fetched-until-date (-> model
                                   :fetched-until-date
                                   js/Date.
                                   datefn/endOfDay)
            user-id (:id current-profile)
            pools @(subscribe [::inventory-pools (:id model)])
            availability-ready? (:availability-ready? model)
            on-cancel #(dispatch [::close-order-panel])
            on-submit (fn [jsargs]
                        (let [args (js->clj jsargs :keywordize-keys true)]
                          (dispatch [::model-create-reservation
                                     {:modelId (:id model)
                                      :startDate (h/date-format-day (:startDate args))
                                      :endDate (h/date-format-day (:endDate args))
                                      :quantity (int (:quantity args))
                                      :poolIds [(:poolId args)]
                                      :userId user-id}])))
            on-validate (fn [v] (reset! form-valid? v))
            order-panel-data @(subscribe [::order-panel-data])
            is-saving? (:is-saving? order-panel-data)]

        (when availability-ready?
          [:> UI/Components.Design.ModalDialog {:shown shown?
                                                :dismissible true
                                                :on-dismiss on-cancel
                                                :title (t :order-dialog/title)
                                                :class "ui-booking-calendar"}
           [:> UI/Components.Design.ModalDialog.Body
            [:> UI/Components.OrderPanel
             {:initialQuantity (or (:quantity filters) 1),
              :initialStartDate initial-start-date,
              :initialEndDate initial-end-date,
              :maxDateLoaded fetched-until-date,
              :profileName profile-name
              :maxDateTotal max-date
              :onCalendarNavigate (fn [date-object]
                                    (let [until-date (get (js->clj date-object) "date")]
                                      (dispatch [::ensure-availability-fetched-until user-id until-date])))
              :onDatesChange (fn [formValues]
                               (let [end-date (get (js->clj formValues) "endDate")]
                                 (dispatch [::ensure-availability-fetched-until user-id end-date])))
              :initialInventoryPoolId (:pool-id filters)
              :inventoryPools (map h/camel-case-keys pools)
              :onSubmit on-submit
              :onValidate on-validate
              :modelData (h/camel-case-keys model)
              :locale user-locale
              :txt (cart/order-panel-texts)}]]
           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-primary {:form :order-dialog-form :type :submit :disabled is-saving? :class (when (not @form-valid?) "disabled pe-auto")}
             (when is-saving? [:> UI/Components.Design.Spinner]) " "
             (t :order-dialog/add)]
            [:button.btn.btn-secondary {:on-click on-cancel} (t :order-dialog/cancel)]]])))))

(defn enrich-recommends-with-href [m filters]
  (update-in m
             [:recommends :edges]
             (flip map)
             #(assoc-in %
                        [:node :href]
                        (routing/path-for ::routes/models-show
                                          :model-id
                                          (-> % :node :id)
                                          :query-params filters))))

(defn order-success-notification [order-panel-data]
  [:> UI/Components.Design.ConfirmDialog
   {:shown (:success? order-panel-data)
    :title (t :order-success-notification/title)
    :onConfirm #(dispatch [::dismiss-order-success])
    :dismissible true
    :onDismiss #(dispatch [::dismiss-order-success])}
   [:<>
    [:p
     (t :order-success-notification/item-was-added)]]])

(defn view
  []
  (let [routing @(subscribe [:routing/routing])
        model-id (get-in routing [:bidi-match :route-params :model-id])
        filters @(subscribe [::filter-modal/options])
        model @(subscribe [::model-data model-id])
        errors @(subscribe [::errors model-id])
        order-panel-data @(subscribe [::order-panel-data])
        is-loading? (not (or model errors))
        availability-ready? (:availability-ready? model)
        any-availability? (boolean (seq (:availability model)))
        current-profile @(subscribe [::current-profile])
        user-id (:id current-profile)]
    [:section
     (cond
       is-loading? [ui/loading (t :loading)]
       errors [ui/error-view errors]
       :else
       [:<>
        [:> UI/Components.ModelShow {:model (-> model
                                                h/camel-case-keys
                                                (enrich-recommends-with-href filters))
                                     :t {:description (t :description)
                                         :properties (t :properties)
                                         :documents (t :documents)
                                         :compatibles (t :compatibles)
                                         :addItemToCart (t :add-item-to-cart)
                                         :addToFavorites (t :add-to-favorites)
                                         :removeFromFavorites (t :remove-from-favorites)}
                                     :currentFilters (h/camel-case-keys filters)
                                     :onClickFavorite #(dispatch
                                                        [(if (:is-favorited model)
                                                           ::unfavorite-model
                                                           ::favorite-model) (:id model)])
                                     :onOrderClick #(dispatch [::open-order-panel user-id filters])
                                     :isAddButtonEnabled (and availability-ready? any-availability?)
                                     :isFavoriteButtonEnabled (and availability-ready? any-availability?)
                                     :buttonInfo (when (and availability-ready? (not any-availability?)) (t :not-available-for-current-profile))}]

        ; NOTE: order panel is inside a modal, so we dont need to pass it through as a child to `ModelShow` 
        [order-panel model filters (:is-open? order-panel-data)]

        [order-success-notification order-panel-data]])]))
