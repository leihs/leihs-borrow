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
   [leihs.core.core :refer [dissoc-in flip]]))

; TODO: 
; * separate fetching of page & calendar data
; * use plain reg-event-fx for the calendar part (no kebab)

(set-default-translate-path :borrow.model-show)

(def model-id (atom nil))

(def MONTHS-BUFFER 6)
(defn with-future-buffer [date]
  (datefn/addMonths date MONTHS-BUFFER))

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
         opts (::filter-modal/options db)
         start-date (:start-date opts)
         end-date (:end-date opts)
         user-id (current-user/get-current-profile-id db)
         filter-start-date (some-> start-date datefn/parseISO)
         filter-end-date (some-> end-date datefn/parseISO)
         initial-start-date (or filter-start-date now)
         initial-end-date (or filter-end-date
                              (datefn/addDays initial-start-date 1))
         start-of-current-month (datefn/startOfMonth now)
         max-date-loaded (-> initial-end-date
                             with-future-buffer
                             datefn/endOfMonth)]
     {:db (-> db
              (update-in [:ls ::data @model-id] (fnil identity {}))
              (cond-> errors (assoc-in [::errors @model-id] errors))
              (assoc-in [:ls ::data @model-id] (:model data)))
      :dispatch [::fetch-availability
                 user-id
                 (h/date-format-day start-of-current-month)
                 (h/date-format-day max-date-loaded)]})))

(reg-event-fx
 ::fetch-availability
 (fn-traced [{:keys [db]} [_ user-id start-date end-date]]
   (let [model-id @model-id
         pool-ids (pool-ids-with-borrowable-quantity db model-id)]
     (if (empty? pool-ids)
       {:db (update-in db [:ls ::data model-id]
                       #(merge %
                               {:fetching-until-date nil
                                :fetched-until-date nil
                                :availability []
                                :availability-loaded? true}))}
       {:db (assoc-in db
                      [:ls ::data model-id :fetching-until-date]
                      end-date)
        :dispatch [::re-graph/query
                   (rc/inline "leihs/borrow/features/model_show/getAvailability.gql")
                   {:modelId model-id
                    :userId user-id
                    :poolIds pool-ids
                    :startDate start-date
                    :endDate end-date}
                   [::on-fetched-availability]]}))))

(reg-event-db
 ::on-fetched-availability
 (fn-traced [db
             [_ {{{new-availability :availability} :model} :data
                 errors :errors}]]
   (-> db
       (cond-> errors (assoc-in [::errors @model-id] errors))
       (update-in [:ls ::data @model-id]
                  #(merge %
                          {:fetching-until-date nil
                           :fetched-until-date (:fetching-until-date %)
                           :availability new-availability
                           :availability-loaded? true})))))

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
     {:dispatch-n (list [::fetch-availability
                         user-id
                         (-> (js/Date.)
                             datefn/startOfMonth
                             h/date-format-day)
                         (-> args
                             :endDate
                             datefn/parseISO
                             with-future-buffer
                             datefn/endOfMonth
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
            start-of-current-month (datefn/startOfMonth now)
            max-date-loaded (-> model
                                :fetched-until-date
                                js/Date.
                                datefn/endOfDay)
            fetching-until-date (some-> model
                                        :fetching-until-date
                                        js/Date.
                                        datefn/endOfDay)
            user-id (:id current-profile)
            pools @(subscribe [::inventory-pools (:id model)])
            availability-loaded? (:availability-loaded? model)
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

        (when availability-loaded?
          [:> UI/Components.Design.ModalDialog {:shown shown?
                                                :dismissible true
                                                :on-dismiss on-cancel
                                                :title (t :order-dialog/title)
                                                :class "ui-booking-calendar"}
           [:> UI/Components.Design.ModalDialog.Body
            [:> UI/Components.OrderPanel
             {:initialOpen true,
              :initialQuantity (or (:quantity filters) 1),
              ; START DYNAMIC FETCHING PROPS
              :initialStartDate initial-start-date,
              :initialEndDate initial-end-date,
              :maxDateLoaded max-date-loaded,
              :profileName profile-name
              :isLoadingFuture (boolean fetching-until-date),
              ; END DYNAMIC FETCHING PROPS
              :onShownDateChange (fn [date-object]
                                   (let [until-date (get (js->clj date-object) "date")]
                                     (when (not (or fetching-until-date
                                                    (datefn/isEqual until-date max-date-loaded)
                                                    (datefn/isBefore until-date max-date-loaded)))
                                       (dispatch [::fetch-availability
                                              ; Always fetching from start-of-current-month for the
                                              ; time being, as there are issue if scrolling
                                              ; too fast and was not sure if there was something
                                              ; wrong with concating the availabilities.
                                                  user-id
                                                  (-> start-of-current-month h/date-format-day)
                                                  (-> until-date
                                                      with-future-buffer
                                                      h/date-format-day)]))))
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

(defn enrich-recommends-with-href [m]
  (update-in m
             [:recommends :edges]
             (flip map)
             #(assoc-in %
                        [:node :href]
                        (routing/path-for ::routes/models-show
                                          :model-id
                                          (-> % :node :id)))))

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
        availability-loaded? (:availability-loaded? model)
        any-availability? (boolean (seq (:availability model)))]
    [:section
     (cond
       is-loading? [ui/loading (t :loading)]
       errors [ui/error-view errors]
       :else
       [:<>
        [:> UI/Components.ModelShow {:model (-> model
                                                h/camel-case-keys
                                                enrich-recommends-with-href)
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
                                     :onOrderClick #(dispatch [::open-order-panel])
                                     :isAddButtonEnabled (and availability-loaded? any-availability?)
                                     :isFavoriteButtonEnabled (and availability-loaded? any-availability?)
                                     :buttonInfo (when (and availability-loaded? (not any-availability?)) (t :not-available-for-current-profile))}]

        ; NOTE: order panel is inside a modal, so we dont need to pass it through as a child to `ModelShow` 
        [order-panel model filters (:is-open? order-panel-data)]

        [order-success-notification order-panel-data]])]))
