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
    [leihs.borrow.lib.helpers :as h :refer [spy]]
    [leihs.borrow.features.favorite-models.events :as favs]
    [leihs.borrow.features.models.filter-modal :as filter-modal]
    [leihs.borrow.features.current-user.core :as current-user]
    [leihs.borrow.features.shopping-cart.core :as cart]
    [leihs.borrow.features.shopping-cart.timeout :as timeout]
    [leihs.core.core :refer [dissoc-in flip]]))

; TODO: 
; * separate fetching of page & calendar data
; * use plain reg-event-fx for the calendar part (no kebab)

(set-default-translate-path :borrow.model-show)

(def model-id (atom nil))

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/models-show
  (fn-traced
    [{:keys [db]} [_ args]]
    (reset! model-id (get-in args [:route-params :model-id]))
    {:dispatch [::fetch]}))

(reg-event-fx
  ::fetch
  (fn-traced [{:keys [db]} _]
    {:dispatch
     [::re-graph/query
      (rc/inline "leihs/borrow/features/model_show/getModelShow.gql")
      {:modelId @model-id, :userId (current-user/chosen-user-id db)}
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
          opts (::filter-modal/options db)
          start-date (:start-date opts)
          end-date (:end-date opts)
          filter-start-date (some-> start-date datefn/parseISO)
          filter-end-date (some-> end-date datefn/parseISO)
          initial-start-date (or filter-start-date now)
          initial-end-date (or filter-end-date
                               (datefn/addDays initial-start-date 1))
          min-date-loaded (datefn/startOfMonth now)
          max-date-loaded (-> initial-end-date
                              (datefn/addMonths 6)
                              datefn/endOfMonth)]
      {:db (-> db
               (update-in [:ls ::data @model-id] (fnil identity {}))
               (cond-> errors (assoc-in [::errors @model-id] errors))
               (assoc-in [:ls ::data @model-id] (:model data)))
       :dispatch [::fetch-availability
                  (h/date-format-day min-date-loaded)
                  (h/date-format-day max-date-loaded)]})))

(reg-event-fx
  ::fetch-availability
  (fn-traced [{:keys [db]} [_ start-date end-date]]
    (let [user-id (current-user/chosen-user-id db)
          pool-ids (pool-ids-with-borrowable-quantity db @model-id)]
      {:db (assoc-in db
                     [:ls ::data @model-id :fetching-until-date]
                     end-date)
       :dispatch [::re-graph/query
                  (rc/inline "leihs/borrow/features/model_show/getAvailability.gql")
                  {:modelId @model-id
                   :userId user-id
                   :poolIds pool-ids
                   :startDate (h/spy start-date)
                   :endDate (h/spy end-date)}
                  [::on-fetched-availability]]})))

#_(defn merge-availability [old-one new-one]
    (map (fn [{{pool-id :id} :inventory-pool :as old-for-pool}]
           (if-let [new-for-pool (->> new-one
                                      (filter #(-> %
                                                   :inventory-pool
                                                   :id
                                                   (= pool-id)))
                                      first)]
             (update-in old-for-pool
                        [:dates]
                        concat
                        (:dates new-for-pool))
             old-for-pool)) 
         old-one))

#_(defn update-availability [model new-availability]
    (if (empty? (:availability model))
      (assoc model :availability new-availability)
      (update model
              :availability
              merge-availability
              new-availability)))

(defn set-loading-as-ended [model]
  (merge model
         {:fetching-until-date nil
          :fetched-until-date (:fetching-until-date model)}))

(reg-event-db
  ::on-fetched-availability
  (fn-traced [db
              [_ {{{new-availability :availability} :model} :data
                  errors :errors}]]
    (-> db
        (cond-> errors (assoc-in [::errors @model-id] errors))
        (update-in [:ls ::data @model-id]
                   #(-> %
                        set-loading-as-ended
                        (assoc :availability new-availability)
                        #_(update-availability new-availability))))))

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
  (fn-traced [db event]
    (assoc-in db [::order-panel-open] true)))

(reg-event-db
  ::close-order-panel
  (fn-traced [db event]
    (assoc-in db [::order-panel-open] false)))

(reg-sub
 ::order-panel-open?
 (fn [db]
   (get-in db [::order-panel-open])))

(reg-sub ::model-data
         (fn [db [_ id]]
           (get-in db [:ls ::data id])))

(reg-sub
  ::errors
  (fn [db [_ id]]
    (get-in db [::errors id])))

(reg-sub
  ::pools
  (fn [[_ id] _] (rf/subscribe [::model-data id]))
  (fn [m _]
    (letfn [(assoc-borrowable-quantity [p]
              (assoc p
                     :total-borrowable-quantity
                     (->> m
                          :total-borrowable-quantities
                          (filter #(-> % :inventory-pool :id (= (:id p))))
                          first
                          :quantity)))]
      (->> m
        :availability
        (map :inventory-pool)
        (map assoc-borrowable-quantity)))))

(reg-event-fx
  ::model-create-reservation
  (fn-traced [{:keys [db]} [_ args]]
    {:db (assoc-in db [::cart/data :pending-count] (:quantity args))
     :dispatch
     [::re-graph/mutate
      (rc/inline
        "leihs/borrow/features/model_show/createReservationMutation.gql") args
      [::on-mutation-result args]]}))

(reg-event-fx
  ::on-mutation-result
  (fn-traced [{:keys [db]} [_ args {:keys [data errors]}]]
    (if errors
      {:db (-> db
               (assoc-in [:meta :app :fatal-errors] errors)
               (dissoc-in [::cart/data :pending-count]))
       :alert (str "FAIL! " (pr-str errors))}
      {:alert (str "OK! " (pr-str data))
       :dispatch-n (list [::fetch-availability (:startDate args) (:endDate args)]
                         [::timeout/refresh])}))) 

(defn order-panel
  [model filters]
  (let [now (js/Date.)
        user-locale @(subscribe [:leihs.borrow.features.current-user.core/locale])
        filter-start-date (some-> filters :start-date datefn/parseISO)
        filter-end-date (some-> filters :end-date datefn/parseISO)
        initial-start-date (or filter-start-date now)
        initial-end-date (or filter-end-date
                             (datefn/addDays initial-start-date 1))
        min-date-loaded (datefn/startOfMonth now)
        max-date-loaded (-> model
                            :fetched-until-date
                            js/Date.
                            datefn/endOfDay)
        fetching-until-date (some-> model
                                    :fetching-until-date
                                    js/Date.
                                    datefn/endOfDay)
        ; max-quantity (:available-quantity-in-date-range model)
        pools @(subscribe [::pools (:id model)])
        filters-loaded? (seq pools)
        has-availability? (seq (:availability model))
        on-cancel #(dispatch [::close-order-panel])
        on-submit (fn [jsargs]
                    (let [args (js->clj jsargs :keywordize-keys true)]
                      (dispatch [::close-order-panel])
                      (dispatch
                       [::model-create-reservation
                        {:modelId (:id model)
                         :startDate (h/date-format-day (:startDate args))
                         :endDate (h/date-format-day (:endDate args))
                         :quantity (int (:quantity args))
                         :poolIds [(:poolId args)]
                         :userId (:user-id filters)}])))]
    
    (when (and filters-loaded? has-availability?)
      [:> UI/Components.Design.ModalDialog {:shown true
                                            :title (t :order-dialog/title)
                                            :class "ui-booking-calendar"}
       [:> UI/Components.Design.ModalDialog.Body
        [:> UI/Components.OrderPanel
         {:initialOpen true,
          :initialQuantity (or (:quantity filters) 1),
          ; START DYNAMIC FETCHING PROPS
          :initialStartDate initial-start-date,
          :initialEndDate initial-end-date,
          :minDateLoaded min-date-loaded,
          :maxDateLoaded max-date-loaded,
          :isLoadingFuture (boolean fetching-until-date),
          ; END DYNAMIC FETCHING PROPS
          :onShownDateChange (fn [date-object]
                               (let [until-date (get (js->clj date-object) "date")]
                                 (h/log "Calendar shows until: " (h/date-format-day until-date))
                                 (if (or (h/spy fetching-until-date) 
                                         (datefn/isEqual until-date max-date-loaded) 
                                         (datefn/isBefore until-date max-date-loaded))
                                   (h/log "We are either fetching or already have until: "
                                          (h/date-format-day until-date))
                                   (dispatch [::fetch-availability
                                              ; Always fetching from min-date-loaded for the
                                              ; time being, as there are issue if scrolling
                                              ; too fast and was not sure if there was something
                                              ; wrong with concating the availabilities.
                                              (-> min-date-loaded h/date-format-day)
                                              (-> until-date
                                                  (datefn/addMonths 6)
                                                  h/date-format-day)]))))
          :initialInventoryPoolId (:pool-id filters)
          :inventoryPools (map h/camel-case-keys pools)
          :onSubmit on-submit
          :modelData (h/camel-case-keys model)
          :locale user-locale
          :txt (cart/order-panel-texts)}]]
       [:> UI/Components.Design.ModalDialog.Footer
        [:button.btn.btn-secondary {:on-click on-cancel} (t :order-dialog/cancel)]
        [:button.btn.btn-primary {:form :order-dialog-form :type :submit} (t :order-dialog/add)]]])))

(defn enrich-recommends-with-href [m]
  (update-in m
             [:recommends :edges]
             (flip map)
             #(assoc-in %
                        [:node :href] 
                        (routing/path-for ::routes/models-show
                                          :model-id
                                          (-> % :node :id)))))

(defn view
  []
  (let [routing @(subscribe [:routing/routing])
        model-id (get-in routing [:bidi-match :route-params :model-id])
        filters @(subscribe [::filter-modal/options])
        model @(subscribe [::model-data model-id])
        errors @(subscribe [::errors model-id])
        order-panel-open? @(subscribe [::order-panel-open?])
        is-loading? (not (or model errors))]
    [:section
     (cond
       is-loading?
       [:div [:div [ui/spinner-clock]]
        [:pre (t :loading) [:samp model-id] "…"]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:> UI/Components.ModelShow {:model (-> model
                                                h/camel-case-keys
                                                enrich-recommends-with-href)
                                     :t {:addItemToCart (t :add-item-to-cart)
                                         :addToFavorites (t :add-to-favorites)
                                         :removeFromFavorites (t :remove-from-favorites)}
                                     :currentFilters (h/camel-case-keys filters)
                                     :onClickFavorite #(dispatch
                                                        [(if (:is-favorited model)
                                                           ::unfavorite-model
                                                           ::favorite-model) (:id model)])
                                     :onOrderClick #(dispatch [::open-order-panel])
                                    ;;  :orderPanelTmp (when order-panel-open? (reagent/as-element [order-panel model filters]))
                                     }]
        
        ; NOTE: order panel is inside a modal, so we dont need to pass it through as a child to `ModelShow` 
        (when order-panel-open? (reagent/as-element [order-panel model filters]))
        ])] ))
