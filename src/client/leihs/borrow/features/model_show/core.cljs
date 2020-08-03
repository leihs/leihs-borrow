(ns leihs.borrow.features.model-show.core
  #_(:require-macros [leihs.borrow.lib.macros :refer [spy]])
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
                                       #_reg-fx
                                       subscribe
                                       dispatch 
                                       camel-case-keys]]
    #_[leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.components :as ui]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.ui.icons :as icons]
    ["/leihs-ui-client-side" :as UI]
    ["date-fns" :as datefn]
    [leihs.borrow.lib.helpers :as h]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.features.favorite-models.events :as favs]
    [leihs.borrow.features.current-user.core :as current-user]))

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/models-show
  (fn-traced [{:keys [db]} [_ args]]
    (let [id (get-in args [:route-params :model-id])
          filters (filters/current db)
          start-date (:start-date filters)
          end-date (:end-date filters)]
      {:dispatch [::fetch id start-date end-date]})))

(reg-event-fx
  ::fetch
  (fn-traced [_ [_ id start-date end-date]]
    (let [now (js/Date.)
          start-date (datefn/startOfMonth (or start-date now))
          end-date (datefn/endOfMonth (cond (and end-date (<= start-date end-date)) end-date :else (datefn/addMonths now 3)))
          ]
      {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/model_show/getModelShow.gql")
                {:modelId id
                 ; START DYNAMIC FETCHING PROPS
                 :startDate (h/date-format-day start-date)
                 :endDate (h/date-format-day end-date)
                 :minDateLoaded start-date
                 :maxDateLoaded end-date
                 :isLoadingFuture false
                 ; END DYNAMIC FETCHING PROPS
                 :onSubmit #(js/console.log "Date submited!")
                 :bothDatesGiven true ; we always set default values here…
                 :pools ["8bd16d45-056d-5590-bc7f-12849f034351"]} ; FIXME: remove this filter (we want ALL pools)
                [::on-fetched-data id]]})))

(reg-event-fx
  ::reset-availability-and-fetch
  (fn-traced [{:keys [db]} [_ model-id start-date end-date]]
    {:db (update-in db
                    [:ls ::data model-id] 
                    dissoc 
                    :available-quantity-in-date-range)
     :dispatch [::fetch model-id start-date end-date]}))

(reg-event-db
  ::on-fetched-data
  [(path :ls)]
  (fn-traced [ls [_ model-id {:keys [data errors]}]]
    (-> ls
        (update-in [::data model-id] (fnil identity {}))
        (cond-> errors (assoc-in [::errors model-id] errors))
        (assoc-in [::data model-id] (:model data)))))

(reg-event-fx
  ::favorite-model
  (fn-traced [{:keys [db]} [_ model-id]]
    {:db (assoc-in db [:ls ::data model-id :is-favorited] true)
     :dispatch [::favs/favorite-model model-id]}))

(reg-event-fx
  ::unfavorite-model
  (fn-traced [{:keys [db]} [_ model-id]]
    {:db (assoc-in db [:ls ::data model-id :is-favorited] false)
     :dispatch [::favs/unfavorite-model model-id]}))

(reg-sub
  ::model-data
  (fn [db [_ id]]
    (get-in db [:ls ::data id])))

(reg-sub
  ::errors
  (fn [db [_ id]]
    (get-in db [:ls ::errors id])))

(reg-event-fx
  ::model-create-reservation
  (fn-traced [_ [_ args]]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/model_show/createReservationMutation.gql")
                args
                [::on-mutation-result args]]}))

(reg-event-fx
  ::on-mutation-result
  (fn-traced [{:keys [_db]} [_ args {:keys [data errors]}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:alert (str "OK! " (pr-str data))
       :dispatch [::reset-availability-and-fetch
                  (:modelId args)
                  (:startDate args)
                  (:endDate args)]})))

(defn order-panel [model filters]
  (let [filter-start (:start-date filters)
        filter-end (:end-date filters)
        now (js/Date.)
        start-date (datefn/startOfMonth (or filter-start now))
        end-date (datefn/endOfMonth (cond (and filter-end (<= start-date filter-end)) filter-end :else (datefn/addMonths now 3)))

        ; max-quantity (:available-quantity-in-date-range model)
        current-user @(subscribe [::current-user/data])
        has-availability? (boolean (:availability model))
        on-submit (fn [jsargs]
                     (js/alert (str "Submiting!" (js/JSON.stringify jsargs)))
                    (let [args (js->clj jsargs :keywordize-keys true)]
                    (js/console.log args)
                    (js/console.log (:startDate args))
                    
                    (dispatch [::model-create-reservation
                               {:modelId (:id model)
                                :startDate (h/date-format-day (:startDate args))
                                :endDate (h/date-format-day (:endDate args))
                                :quantity (:quantity args)
                                ; :poolId (:poolId args) ???
                                ;:userId (:id current-user) ???
                                }])))]

    [:div.border-b-2.border-gray-300.mt-4.pb-4
     [:h3.font-bold.text-lg.Xtext-color-muted.mb-2 "Make a reservation"]
     [:div.d-flex.mx-n2
      (when has-availability?
            [:> UI/Components.BookingCalendar
             {:initialOpen true
              :initialQuantity 1

              ; START DYNAMIC FETCHING PROPS
              :startDate (h/date-format-day start-date)
              :endDate (h/date-format-day end-date)
              :minDateLoaded start-date
              :maxDateLoaded end-date
              :isLoadingFuture false
              ; END DYNAMIC FETCHING PROPS

              :onLoadMoreFuture #(js/alert (str "Load More!" (js/JSON.stringify %)))
              :onSubmit on-submit

              :modelData (camel-case-keys model)}])]]))

(defn order-panel-1 [model filters]
  (let [state (reagent/atom {:quantity 1
                             :start-date (:start-date filters)
                             :end-date (:end-date filters)
                             :user-id (:user-id filters)})]
    (fn [{:keys [id] :as model} _]
      (swap! state assoc :max-quantity (:available-quantity-in-date-range model))
      (let [current-user @(subscribe [::current-user/data])
            on-submit #(dispatch [::model-create-reservation
                                  {:modelId id
                                   :startDate (:start-date @state)
                                   :endDate (:end-date @state)
                                   :quantity (:quantity @state)
                                   :userId (:user-id @state)}])
            on-change-date-fn (fn [date-key]
                                (fn [e]
                                  (let [val (.-value (.-target e))]
                                    (swap! state assoc date-key val)
                                    (when (and (:start-date @state) (:end-date @state))
                                      (dispatch [::reset-availability-and-fetch
                                                 id
                                                 (:start-date @state)
                                                 (:end-date @state)])))))]
        [:div.border-b-2.border-gray-300.mt-4.pb-4
         [:h3.font-bold.text-lg.Xtext-color-muted.mb-2 "Make a reservation"]
         [:div.d-flex.mx-n2
          [:label.px-2.w-1_2
           [:span.text-color-muted "from "]
           [:input {:type :date
                    :name :start-date
                    :value (:start-date @state)
                    :on-change (on-change-date-fn :start-date)}]]
          [:label.px-2.w-1_2
           [:span.text-color-muted "until "]
           [:input {:type :date
                    :name :end-date
                    :value (:end-date @state)
                    :on-change (on-change-date-fn :end-date)}]]]
         (when (and (:start-date @state) (:end-date @state))
           [:div.d-flex.items-end.mt-2
            [:div.w-1_2
             [:div.d-flex.flex-wrap.align-items-end
              [:div.w-1_2
               [:label.d-block.mb-0
                [:span.d-block.text-color-muted "quantity "]
                [:input.w-full
                 {:type :number
                  :name :quantity
                  :max (:max-quantity @state)
                  :value (:quantity @state)
                  :disabled (or (nil? (:max-quantity @state))
                                (> (:quantity @state) (:max-quantity @state)))
                  :on-change (fn [e]
                               (let [val (.-value (.-target e))]
                                 (swap! state assoc :quantity (int val))))}]]]
              [:div.flex-1.w-1_2 [:span.no-underline.text-color-muted 
                                  {:aria-label (str "maximum available quantity is " (:max-quantity @state))} 
                                  "/" ui/thin-space (or (:max-quantity @state) [ui/spinner-clock]) ui/thin-space "max."]]]]
            [:label.d-block.mb-0.mr-3
             [:span.d-block.text-color-muted "for "]
             [:select {:name :user-id
                       :on-change (fn [e]
                                    (let [val (.-value (.-target e))]
                                      (swap! state assoc :user-id val)))}
              (doall
                (for [user (cons (:user current-user) (:delegations current-user))]
                  [:option {:value (:id user)
                            :selected (= (:user-id @state) (:id user))}
                   (if (= (:user-id @state) (:id user))
                    "me"
                    (:name user))]))]]

            [:div.flex-auto.w-1_2
             [:button.px-4.py-2.w-100.rounded-lg.bg-content-inverse.text-color-content-inverse.font-semibold.text-lg
              (cond-> {:on-click on-submit}
                (> (:quantity @state) (:max-quantity @state))
                (assoc :disabled true))
              "Order"]]])]))))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        model-id (get-in routing [:bidi-match :route-params :model-id])
        filters @(subscribe [::filters/current])
        model @(subscribe [::model-data model-id])
        errors @(subscribe [::errors model-id])
        is-loading? (not (or model errors))]

    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div [ui/spinner-clock]] [:pre "loading model" [:samp model-id] "…"]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header.d-flex.items-stretch
         [:h1.text-3xl.font-extrabold.leading-none
          (:name model)
          " "
          [:small.font-normal.text-gray-600.leading-none (:manufacturer model)]]
         [:span.text-4xl.ml-2.pr-2 
          [:button {:on-click #(dispatch [(if (:is-favorited model)
                                            ::unfavorite-model
                                            ::favorite-model)
                                          (:id model)])}
           (if (:is-favorited model) icons/favorite-yes-icon icons/favorite-no-icon)]]]

        ; FIXME: show all images not just the first one
        (if-let [first-image (first (:images model))]
          [:div.flex.justify-center.py-4.mt-4.border-b-2.border-gray-300
           [:div [:img {:src (:image-url first-image)}]]])

        [order-panel model filters]
        
        #_[order-panel-1 model filters]

        (if-let [description (:description model)]
          [:p.py-4.border-b-2.border-gray-300.text-base.preserve-linebreaks description])

        (if-let [attachments  (:attachments model)]
          [:<>
           [:ul.list-inside.list-disc.text-blue-600
            (doall
              (for [a attachments]
                [:<> {:key (:id a)}
                 [:li.border-b-2.border-gray-300.py-2
                  [:a.text-blue-500 {:href (:url a)} (:filename a)]
                  [:small.text-gray-600 (str " (" (ui/decorate-file-size (:size a)) ")")]]]))]])

        (if-let [fields (not-empty (map vector (:properties model)))]
          [:dl.pb-4.mb-4.mt-4.border-b-2.border-gray-300
           (doall
             (for [[field] fields]
               [:<> {:key (:id field)}
                [:dt.font-bold (:key field)]
                [:dd.pl-6 (:value field)]]))])

        (if-let [recommends (-> model :recommends :edges not-empty)]
          [:div.mt-4
           [:h2.text-xl.font-bold "Ergänzende Modelle"]
           [:div.d-flex.flex-wrap.-mx-2
            (doall
              (for [edge recommends]
                (let
                  [rec (:node edge)
                   href (str "/app/borrow/models/" (:id rec))]
                  [:div {:key (:id rec) :class "w-1/2"}
                   [:div.p-2
                    ; FIXME: use path helper!
                    [:div.square-container.relative.rounded.overflow-hidden.border.border-gray-200
                     [:a {:href href}
                      (if-let [img (get-in rec [:images 0 :image-url])]
                        [:img.absolute.object-contain.object-center.h-full.w-full.p-1 {:src img}]
                        [:div.absolute.h-full.w-full.bg-gray-400 " "])]]

                    [:a.text-gray-700.font-semibold {:href href}
                     (:name rec)]]])))]])

        #_[:p.debug (pr-str model)]])]))
