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
   [leihs.borrow.lib.translate :refer [t set-default-translate-path] :as translate]
   [leihs.borrow.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.errors :as errors]
   ["/borrow-ui" :as UI]
   ["date-fns" :as datefn]
   [leihs.borrow.lib.helpers :as h :refer [spy log]]
   [leihs.borrow.features.favorite-models.events :as favs]
   [leihs.borrow.features.models.model-filter :as filter-modal]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.shopping-cart.core :as cart]
   [leihs.borrow.features.shopping-cart.timeout :as timeout]
   [leihs.borrow.features.model-show.availability :as availability]
   [leihs.core.core :refer [dissoc-in flip presence]]
   [leihs.borrow.lib.prefs :as prefs]
   ["autolinker" :as autolinker]))

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
     [::on-fetched-data]]
    :db (assoc-in db [::errors @model-id] nil)}))

(defn pool-ids-with-reservable-quantity [db model-id]
  (let [quants (get-in db
                       [:ls
                        ::data
                        model-id
                        :total-reservable-quantities])]
    (->> quants
         (filter #(-> % :quantity (> 0)))
         (map #(-> % :inventory-pool :id)))))

(defn filter-pickup-location-id [db]
  (-> db :routing/routing :bidi-match :query-params :pickup-location-id presence))

(defn availability-query-vars [db model-id user-id start-date end-date]
  (let [pool-ids (pool-ids-with-reservable-quantity db model-id)]
    {:modelId model-id
     :userId user-id
     :poolIds pool-ids
     :startDate start-date
     :endDate end-date}))

(defn mark-availability-mode-ready [model mode]
  (let [pending (disj (or (:availability-pending model) #{}) mode)]
    (-> model
        (assoc :availability-pending pending)
        (assoc :availability-ready? (empty? pending))
        ;; Keep a convenient copy of main-warehouse availability for page-level checks.
        (assoc :availability (get-in model [:availability-modes :main :availability] [])))))

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
                              availability/with-future-buffer)
         start-day (h/date-format-day start-of-current-month)
         end-day (h/date-format-day fetch-until-date)]
     (if errors
       {:db (assoc-in db [::errors @model-id] errors)}
       {:db (assoc-in db [:ls ::data @model-id]
                      (-> (:model data)
                          (assoc :availability-pending #{:main}
                                 :availability-ready? false
                                 :availability-modes {}
                                 :availability [])))
        :dispatch-n [[::fetch-availability user-id start-day end-day :main]]}))))

(reg-event-fx
 ::fetch-availability
 (fn-traced [{:keys [db]} [_ user-id start-date end-date mode]]
   (let [mode (or mode :main)
         model-id @model-id
         pool-ids (pool-ids-with-reservable-quantity db model-id)
         start-date-exceeds-max? (> (js/Date. start-date) max-date)
         end-or-max-date (if (> (js/Date. end-date) max-date)
                           (h/date-format-day max-date)
                           end-date)
         mode-path [:ls ::data model-id :availability-modes mode]]
     (cond
       (empty? pool-ids)
       {:db (update-in db [:ls ::data model-id]
                       #(-> %
                            (assoc-in [:availability-modes mode :availability] [])
                            (mark-availability-mode-ready mode)))}
       start-date-exceeds-max?
       {:db (update-in db mode-path
                       #(availability/set-loading-as-ended % end-date false))}
       :else
       {:db (assoc-in db (conj mode-path :fetching-until-date) end-date)
        :dispatch [::re-graph/query
                   (rc/inline "leihs/borrow/features/model_show/getAvailability.gql")
                   (availability-query-vars db model-id user-id start-date end-or-max-date)
                   [::on-fetched-availability end-date mode]]}))))

(reg-event-db
 ::set-order-panel-pickup-location
 (fn-traced [db [_ pickup-location-id]]
   (assoc-in db [::data :order-panel :pickup-location-id] pickup-location-id)))

(reg-event-fx
 ::on-fetched-availability
 (fn-traced [{:keys [db]}
             [_ end-date mode {{{new-availability :availability} :model} :data
                               errors :errors}]]
   (let [mode-path [:ls ::data @model-id :availability-modes mode]]
     (if errors
       {:db (update-in db mode-path
                       #(availability/set-loading-as-ended % end-date false))
        :dispatch [::errors/add-many errors]}
       {:db (-> db
                (update-in mode-path
                           #(-> (or % {})
                                (availability/update-availability new-availability)
                                (availability/set-loading-as-ended end-date true)))
                (update-in [:ls ::data @model-id]
                           #(mark-availability-mode-ready % mode)))}))))

(reg-event-fx
 ::ensure-availability-fetched-until
 (fn-traced [{:keys [db]} [_ user-id requested-date]]
   (let [model (get-in db [:ls ::data @model-id])
         modes (keys (:availability-modes model))
         dispatches
         (keep (fn [mode]
                 (let [mode-state (get-in model [:availability-modes mode])
                       max-fetched-or-fetching
                       (js/Date. (or (:fetching-until-date mode-state)
                                     (:fetched-until-date mode-state)))
                       range-start (datefn/addDays max-fetched-or-fetching 1)
                       range-end (availability/with-future-buffer requested-date)]
                   (when (datefn/isAfter requested-date max-fetched-or-fetching)
                     [::fetch-availability
                      user-id
                      (-> range-start h/date-format-day)
                      (-> range-end h/date-format-day)
                      mode])))
               modes)]
     (when (seq dispatches)
       {:dispatch-n dispatches}))))

(reg-event-db
 ::clear-availability
 (fn-traced [db _]
   (update-in db [:ls ::data @model-id]
              #(merge % {:availability []
                         :availability-modes {}
                         :availability-pending nil
                         :availability-ready? false}))))

(reg-event-fx
 ::favorite-model
 (fn-traced
   [_ [_ model-id]]
   {:dispatch [::favs/favorite-model model-id [:ls ::data model-id :is-favorited]]}))

(reg-event-fx
 ::unfavorite-model
 (fn-traced
   [_ [_ model-id]]
   {:dispatch [::favs/unfavorite-model model-id [:ls ::data model-id :is-favorited]]}))

(reg-event-db
 ::open-order-panel
 (fn-traced [db [_ _user-id filters]]
   (assoc-in db [::data :order-panel]
             {:is-open? true
              :pickup-location-id (or (some-> filters :pickup-location-id presence)
                                      (filter-pickup-location-id db))})))

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

(reg-sub
 ::inventory-pools
 (fn [[_ id] _]
   [(rf/subscribe [::model-data id])
    (rf/subscribe [::current-profile])])
 (fn [[model current-profile] _]
   (let [profile-pools-by-id (->> (:inventory-pools current-profile)
                                  (map (juxt :id identity))
                                  (into {}))
         assoc-reservable-quantity
         (fn [pool]
           (assoc pool
                  :total-reservable-quantity
                  (->> model
                       :total-reservable-quantities
                       (filter #(-> % :inventory-pool :id (= (:id pool))))
                       first
                       :quantity)))
         assoc-pickup-locations
         (fn [pool]
           (let [from-profile (get profile-pools-by-id (:id pool))]
             (-> pool
                 (assoc :pickup-locations
                        (or (:pickup-locations from-profile)
                            (:pickup-locations pool)))
                 (assoc :default-pickup-location-name
                        (or (:default-pickup-location-name from-profile)
                            (:default-pickup-location-name pool)))
                 (assoc :enable-alternative-pickup-locations
                        ;; Prefer availability (just fetched); `or` would treat false as missing.
                        (if (some? (:enable-alternative-pickup-locations pool))
                          (:enable-alternative-pickup-locations pool)
                          (:enable-alternative-pickup-locations from-profile))))))
         assoc-suspension
         (fn [pool]
           (let [is-suspended? (some #(= (-> % :inventory-pool :id) (-> pool :id))
                                     (:suspensions current-profile))]
             (merge pool
                    (when is-suspended? {:user-is-suspended true}))))
         main-availability (get-in model [:availability-modes :main :availability]
                                   (:availability model))
         pools-from-quants
         (->> model
              :total-reservable-quantities
              (filter #(-> % :quantity pos?))
              (map (fn [{{pool-id :id} :inventory-pool :as quant}]
                     (let [from-profile (get profile-pools-by-id pool-id)
                           from-availability (->> main-availability
                                                  (map :inventory-pool)
                                                  (filter #(= (:id %) pool-id))
                                                  first)]
                       (merge from-availability
                              from-profile
                              {:id pool-id}
                              (select-keys (:inventory-pool quant) [:id :name])))))
              (remove #(nil? (:id %))))
         pools (if (seq pools-from-quants)
                 pools-from-quants
                 (->> main-availability
                      (map :inventory-pool)
                      (remove nil?)))]
     (->> pools
          (map assoc-reservable-quantity)
          (map assoc-pickup-locations)
          (map assoc-suspension)
          (filter #(pos? (or (:total-reservable-quantity %) 0)))))))

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
              (dissoc-in [:ls ::cart/data :pending-count])
              (assoc-in [::data :order-panel :is-saving?] false))
      :dispatch [::errors/add-many errors]}
     (let [now (js/Date.)
           start-day (-> now datefn/startOfMonth h/date-format-day)
           end-day (-> args
                       :endDate
                       datefn/parseISO
                       availability/with-future-buffer
                       h/date-format-day)]
       {:db (-> db
                (dissoc-in [:ls ::cart/data :pending-count])
                (update-in [:ls ::data @model-id]
                           #(merge % {:availability []
                                      :availability-modes {}
                                      :availability-pending #{:main}
                                      :availability-ready? false}))
                (assoc-in [::data :order-panel] {:success? true}))
        :dispatch-n [[::timeout/refresh]
                     [::fetch-availability user-id start-day end-day :main]]}))))

(defn order-panel
  [model filters shown?]
  (let [form-valid? (reagent/atom false)]
    (fn [model filters shown?]
      (let [now (js/Date.)
            current-profile @(subscribe [::current-profile])
            can-change-profile? @(subscribe [::can-change-profile?])
            profile-name (when can-change-profile? (:name current-profile))
            text-locale @(subscribe [::translate/text-locale])
            date-locale @(subscribe [::translate/date-locale])
            filter-start-date (some-> filters :start-date datefn/parseISO)
            filter-end-date (some-> filters :end-date datefn/parseISO)
            initial-start-date (or filter-start-date now)
            initial-end-date (or filter-end-date
                                 (datefn/addDays initial-start-date 1))
            order-panel-data @(subscribe [::order-panel-data])
            active-pickup-location-id (or (:pickup-location-id order-panel-data)
                                          (:pickup-location-id filters))
            mode-state (get-in model [:availability-modes :main])
            raw-availability (or (:availability mode-state)
                                 (:availability model)
                                 [])
            panel-availability (if active-pickup-location-id
                                 (map #(assoc % :dates (or (:dates-for-alt-locations %) (:dates %)))
                                      raw-availability)
                                 raw-availability)
            fetched-until-date (some-> (or (:fetched-until-date mode-state)
                                           (:fetched-until-date model))
                                       js/Date.
                                       datefn/endOfDay)
            show-day-quants @(subscribe [::prefs/show-day-quants])
            on-show-day-quants-change #(dispatch [::prefs/set-show-day-quants %])
            user-id (:id current-profile)
            pools @(subscribe [::inventory-pools (:id model)])
            availability-ready? (:availability-ready? model)
            is-saving? (:is-saving? order-panel-data)
            on-cancel #(dispatch [::close-order-panel])
            on-submit (fn [jsargs]
                        (let [args (js->clj jsargs :keywordize-keys true)]
                          (dispatch [::model-create-reservation
                                     (cond-> {:modelId (:id model)
                                              :startDate (h/date-format-day (:startDate args))
                                              :endDate (h/date-format-day (:endDate args))
                                              :quantity (int (:quantity args))
                                              :poolIds [(:poolId args)]
                                              :userId user-id}
                                       (:pickupLocationId args)
                                       (assoc :pickupLocationId (:pickupLocationId args)))])))
            on-pickup-location-change
            (fn [jsargs]
              (let [args (js->clj jsargs :keywordize-keys true)]
                (dispatch [::set-order-panel-pickup-location
                           (:pickupLocationId args)])))
            on-inventory-pool-change
            (fn [jsargs]
              (let [args (js->clj jsargs :keywordize-keys true)
                    next-pickup (:pickupLocationId args)]
                (when (not= next-pickup (:pickup-location-id order-panel-data))
                  (dispatch [::set-order-panel-pickup-location next-pickup]))))
            on-validate (fn [v] (reset! form-valid? v))
            model-data (-> model
                           (assoc :availability panel-availability)
                           h/camel-case-keys)]

        (when availability-ready?
          [:> UI/Components.Design.ModalDialog {:shown shown?
                                                :dismissible true
                                                :on-dismiss on-cancel
                                                :title (:name model)
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
              :initialPickupLocationId (:pickup-location-id filters)
              :onInventoryPoolChange on-inventory-pool-change
              :onPickupLocationChange on-pickup-location-change
              :inventoryPools (map h/camel-case-keys pools)
              :initialShowDayQuants (or show-day-quants false)
              :onShowDayQuantsChange on-show-day-quants-change
              :onSubmit on-submit
              :onValidate on-validate
              :modelData model-data
              :locale text-locale
              :dateLocale date-locale
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

(defn autolink-description [model]
  (assoc model :description (autolinker/link (:description model) (clj->js {:sanitizeHtml true}))))

(defn order-success-notification [order-panel-data]
  [:> UI/Components.Design.ConfirmDialog
   {:shown (:success? order-panel-data)
    :title (t :order-success-notification/title)
    :onConfirm #(dispatch [::dismiss-order-success])
    :dismissible true
    :onDismiss #(dispatch [::dismiss-order-success])}
   [:<>
    [:p.fw-bold
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
    [:> UI/Components.Design.PageLayout.ContentContainer
     (cond
       is-loading? [ui/loading (t :loading)]
       errors [ui/error-view errors]
       :else
       [:<>
        [:> UI/Components.ModelShow {:model (-> model
                                                h/camel-case-keys
                                                (enrich-recommends-with-href filters)
                                                autolink-description)
                                     :t {:description (t :description)
                                         :properties (t :properties)
                                         :documents (t :documents)
                                         :compatibles (t :compatibles)
                                         :addItemToCart (t :add-item-to-cart)
                                         :addToFavorites (t :add-to-favorites)
                                         :removeFromFavorites (t :remove-from-favorites)
                                         :previousImage (t :previous-image)
                                         :nextImage (t :next-image)}
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
