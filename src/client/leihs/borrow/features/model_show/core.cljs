(ns leihs.borrow.features.model-show.core
  (:refer-clojure :exclude [val])
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [reagent.core :as reagent]
    #_[re-frame.core :as rf]
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

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/models-show
  (fn-traced
    [{:keys [db]} [_ args]]
    (let [id (get-in args [:route-params :model-id])
          start-date (filters/start-date db)
          end-date (filters/end-date db)
          user-id (filters/user-id db)]
      {:dispatch [::fetch id user-id start-date end-date]})))

(reg-event-fx
  ::fetch
  (fn-traced
    [{:keys [db]} [_ id user-id start-date end-date]]
    (let [now (js/Date.)
          filter-start-date (some-> start-date datefn/parseISO)
          filter-end-date (some-> end-date datefn/parseISO)
          initial-start-date (or filter-start-date now)
          initial-end-date (or filter-end-date
                               (datefn/addDays initial-start-date 1))
          min-date-loaded (datefn/startOfMonth now)
          max-date-loaded (-> initial-end-date
                              (datefn/addMonths 6)
                              datefn/endOfMonth)
          pools (-> db ::current-user/data :inventory-pools)]
      {:dispatch
       [::re-graph/query
        (rc/inline "leihs/borrow/features/model_show/getModelShow.gql")
        {:modelId id,
         :userId user-id
         :startDate (h/date-format-day min-date-loaded),
         :endDate (h/date-format-day max-date-loaded),
         :bothDatesGiven true, ; we always set default values here…
         :pools (map :id pools)} ; FIXME: remove this filter (we want ALL pools)
        [::on-fetched-data id]]})))

(reg-event-fx
  ::load-more-availability
  (fn-traced [{:keys [db]} [_ model-id user-id start-date end-date]]
    {:db (assoc-in db
                   [:ls ::data model-id :is-loading-more-availability]
                   true)
     :dispatch [::fetch model-id user-id start-date end-date]}))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ model-id {:keys [data errors]}]]
    (-> db
        (update-in [:ls ::data model-id] (fnil identity {}))
        (cond-> errors (assoc-in [::errors model-id] errors))
        (assoc-in [:ls ::data model-id] (:model data)))))

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

(reg-sub ::model-data (fn [db [_ id]] (get-in db [:ls ::data id])))

(reg-sub
  ::errors
  (fn [db [_ id]]
    (get-in db [::errors id])))

(reg-event-fx
  ::model-create-reservation
  (fn-traced
    [_ [_ args]]
    {:dispatch
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
        max-date-loaded (-> initial-end-date
                            (datefn/addMonths 6)
                            datefn/endOfMonth)
        ; max-quantity (:available-quantity-in-date-range model)
        pools @(subscribe [::current-user/pools])
        has-availability? (boolean (:availability model))
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
    
    (when has-availability?
      #_(js/console.log (clj->js [initial-start-date
                                initial-end-date
                                min-date-loaded
                                max-date-loaded]))
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
          :isLoadingFuture (:is-loading-more-availability model),
          ; END DYNAMIC FETCHING PROPS
          :onShownDateChange (fn [date-object]
                              (let [until-date (get (js->clj date-object) "date")]
                                (js/console.log (str "Load More! " until-date))
                                (dispatch [::load-more-availability
                                           (:id model)
                                           (:user-id filters)
                                           (h/date-format-day min-date-loaded)
                                           (h/date-format-day until-date)]))),
          :initialPoolId (:pool-id filters)
          :inventoryPools pools
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
