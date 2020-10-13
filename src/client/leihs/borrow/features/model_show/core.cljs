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
    [leihs.borrow.ui.icons :as icons]
    ["/leihs-ui-client-side-external-react" :as UI]
    ["date-fns" :as datefn]
    [leihs.borrow.lib.helpers :as h]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.features.favorite-models.events :as favs]
    [leihs.borrow.features.current-user.core :as current-user]))

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
      {:modelId @model-id, :userId (filters/user-id db)}
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
          start-date (filters/start-date db)
          end-date (filters/end-date db)
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
    (let [user-id (filters/user-id db)
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
     :dispatch [::favs/favorite-model model-id]}))

(reg-event-fx
  ::unfavorite-model
  (fn-traced
    [{:keys [db]} [_ model-id]]
    {:db (assoc-in db [:ls ::data model-id :is-favorited] false),
     :dispatch [::favs/unfavorite-model model-id]}))

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
  (fn-traced
    [_ [_ args]] {:dispatch
       [::re-graph/mutate
        (rc/inline
          "leihs/borrow/features/model_show/createReservationMutation.gql") args
        [::on-mutation-result args]]}))

(reg-event-fx
  ::on-mutation-result
  (fn-traced [{:keys [db]} [_ args {:keys [data errors]}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:alert (str "OK! " (pr-str data))
       :dispatch [::reset-availability-and-fetch
                  (:modelId args)
                  (:userId args)
                  (:startDate args)
                  (:endDate args)]})))

(defn order-panel
  [model filters]
  (let [now (js/Date.)
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
        has-availability? (seq (:availability model))
        on-submit (fn [jsargs]
                    ; (js/alert (str "Submiting!" (js/JSON.stringify jsargs)))
                    (let [args (js->clj jsargs :keywordize-keys true)]
                      (dispatch
                        [::model-create-reservation
                         {:modelId (:id model),
                          :startDate (h/date-format-day (:startDate args)),
                          :endDate (h/date-format-day (:endDate args)),
                          :quantity (int (:quantity args)),
                          :poolIds [(:poolId args)]
                          :userId (:user-id filters)}])))]
    
    (when (seq pools)
      [:div.border-b-2.border-gray-300.mt-4.pb-4
       [:h3.font-bold.text-lg.Xtext-color-muted.mb-2 "Make a reservation"]
       [:div.d-flex.mx-n2
        [:> UI/Components.BookingCalendar
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
          :onSubmit on-submit,
          :modelData (h/camel-case-keys model)}]]])))

(defn view
  []
  (let [routing @(subscribe [:routing/routing])
        model-id (get-in routing [:bidi-match :route-params :model-id])
        filters @(subscribe [::filters/current])
        model @(subscribe [::model-data model-id])
        errors @(subscribe [::errors model-id])
        is-loading? (not (or model errors))]
    [:section.mx-3.my-4
     (cond
       is-loading?
         [:div [:div [ui/spinner-clock]]
          [:pre (t :loading) [:samp model-id] "…"]]
       errors [ui/error-view errors]
       :else
         [:<>
          [:header.d-flex.align-items-center
           [:h1.w-100.text-3xl.font-extrabold.leading-none (:name model)
            (if-let [manufacturer (:manufacturer model)]
              [:<> " " [:br]
                  [:small.font-normal.text-gray-600.leading-none manufacturer]])]
           [:span.flex-shrink-1.text-4xl.ml-2.pr-2
            [:button
             {:on-click
                #(dispatch
                    [(if (:is-favorited model)
                       ::unfavorite-model
                       ::favorite-model) (:id model)])}
             (if (:is-favorited model)
               icons/favorite-yes-icon
               icons/favorite-no-icon)]]]
          ; FIXME: show all images not just the first one
          (if-let [first-image (first (:images model))]
            [:div.flex.justify-center.py-4.mt-4.border-b-2.border-gray-300
             [:div [:img {:src (:image-url first-image)}]]])
          [order-panel model filters]
          ; [order-panel-1 model filters]
          (if-let [description (:description model)]
            [:p.py-4.border-b-2.border-gray-300.text-base.preserve-linebreaks
             description])
          (if-let [attachments (:attachments model)]
            [:<>
             [:ul.list-inside.list-disc.text-blue-600
              (doall
                (for [a attachments]
                  [:<> {:key (:id a)}
                   [:li.border-b-2.border-gray-300.py-2
                    [:a.text-blue-500 {:href (:url a)} (:filename a)]
                    [:small.text-gray-600
                     (str " (" (ui/decorate-file-size (:size a)) ")")]]]))]])
          (if-let [fields (not-empty (map vector (:properties model)))]
            [:dl.pb-4.mb-4.mt-4.border-b-2.border-gray-300
             (doall
               (for [[field] fields]
                 [:<> {:key (:id field)} [:dt.font-bold (:key field)]
                  [:dd.pl-6 (:value field)]]))])
          (if-let [recommends
                     (->
                       model
                       :recommends
                       :edges
                       not-empty)]
            [:div.mt-4 [:h2.text-xl.font-bold (t :compatibles)]
             [:div.d-flex.flex-wrap.-mx-2
              (doall
                (for [edge recommends]
                  (let [rec (:node edge)
                        href (str "/app/borrow/models/" (:id rec))]
                    [:div {:key (:id rec), :class "w-1/2"}
                     [:div.p-2
                      ; FIXME: use path helper!
                      [:div.square-container.relative.rounded.overflow-hidden.border.border-gray-200
                       [:a {:href href}
                        (if-let [img (get-in rec [:images 0 :image-url])]
                          [:img.absolute.object-contain.object-center.h-full.w-full.p-1
                           {:src img}]
                          [:div.absolute.h-full.w-full.bg-gray-400 " "])]]
                      [:a.text-gray-700.font-semibold {:href href}
                       (:name rec)]]])))]]) #_[:p.debug (pr-str model)]])]))
