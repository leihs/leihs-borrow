(ns leihs.borrow.client.features.model-show.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/models-show
 (fn [_ [_ args]]
   (let [model-id (get-in args [:route-params :model-id])
         q (get-in args [:query-params])
         order-form-params {:startDate (:start q) :endDate (:end q)}
         order-form? (boolean (some identity (vals order-form-params)))
         ]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/client/features/model_show/getModelShow.gql")
                 (merge {:modelId model-id :withOrderForm order-form?} order-form-params)
                 [::on-fetched-data model-id]]})))

(rf/reg-event-db
 ::on-fetched-data
 (fn [db [_ model-id {:keys [data errors]}]]
   (-> db
       (update-in , [:models model-id] (fnil identity {}))
       (assoc-in , [:models model-id :errors] errors)
       (assoc-in , [:models model-id :data] data))))

(rf/reg-event-fx
 ::fetch-available-quantity
 (fn [{:keys [db]} [_ model-id start end]]
     {:db (assoc-in db [:models model-id :is-loading-availability [start end]] true)
      :dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/client/features/model_show/getModelAvailableQuantity.gql")
                 {:modelId model-id :startDate start :endDate end}
                 [::on-fetched-available-quantity model-id start end]]}))

(rf/reg-event-db
 ::on-fetched-available-quantity
 (fn [db [_ model-id start end {:keys [data errors]}]]
   (-> db
       (assoc-in , [:models model-id :is-loading-availability [start end]] false)
       (assoc-in , [:models model-id :errors] errors)
       (update-in , [:models model-id :data :model] merge (get-in data [:model])))))

(rf/reg-sub
 ::model-data
 (fn [db [_ id]]
   (get-in db [:models id])))

(rf/reg-sub
 ::loading-availability?
 (fn [db [_ id start end]]
   (get-in db [:models id :is-loading-availability [start end]])))

(rf/reg-event-fx
 ::model-create-reservation
 (fn [_ [_ args]]
   {:dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/client/queries/createReservationMutation.gql")
               args
               [::on-model-create-reservation-result]]}))

(rf/reg-event-fx
 ::on-model-create-reservation-result
 (fn [{:keys [_db]} [_ {:keys [data errors]}]]
   (if errors
     {:alert (str "FAIL! " (pr-str errors))}
     {:alert (str "OK! " (pr-str data))})))

(defn order-panel [model params]
  ; TODO: get availability from api, not param!
  (let [state (reagent/atom (merge params {:quantity 1}))]
    (fn [model params]
      (let [given-order-dates? (and (:start params) (:end params))
            loading-availability? @(rf/subscribe [::loading-availability? (:id model) (:start @state) (:end @state)])
            max-available (:availableQuantityInDateRange model)
            submit-disabled? (or loading-availability? (> (:start @state) max-available))
            on-dates-change #(rf/dispatch [::fetch-available-quantity (:id model) (:start @state) (:end @state)])
            on-submit #(rf/dispatch
                        [::model-create-reservation
                         (merge {:modelId (:id model)
                                 :startDate (:start @state)
                                 :endDate (:end @state)
                                 :quantity (:quantity @state)})])]
        (when given-order-dates?
          [:form {::on-submit #((.preventDefault %) (on-submit))}
           [:div.border-b-2.border-gray-300.mt-4.pb-4
            [:h3.font-bold.text-lg.Xtext-color-muted.mb-2 "Make a reservation"]
            [:div.flex.-mx-2
             [:label.px-2.w-1_2
              [:span.text-color-muted "from "]
              [:input {:type :date
                       :name :start-date
                       :value (:start @state)
                       :max (:end @state)
                       :on-change
                       (fn [e] (let [val (.-value (.-target e))]
                                 (swap! state assoc :start val)
                                 (on-dates-change)))}]]
             [:label.px-2.w-1_2
              [:span.text-color-muted "until "]
              [:input {:type :date
                       :name :end-date
                       :value (:end @state)
                       :min (:start @state)
                       :on-change
                       (fn [e] (let [val (.-value (.-target e))]
                                 (swap! state assoc :end val)
                                 (on-dates-change)))}]]]
            [:div.flex.items-end.mt-2.-mx-2
             [:div.px-2.flex-none.w-1_2
              [:div.flex.flex-wrap.items-end.-mx-2
               [:div.flex-1.w-1_2.px-2
                [:label.block
                 [:span.block.text-color-muted "quantity "]
                 [:input.w-full.input-show-validation
                  {:type :number
                   :name :quantity
                   :max max-available
                   :value (:quantity @state)
                   :on-change
                   (fn [e] (let [val (.-value (.-target e))] (swap! state assoc :quantity (int val))))}]]]
               [:div.flex-1.w-1_2.px-2.text-color-muted
                (if loading-availability?
                  [:span
                   {:aria-label (str "maximum available quantity is loading")}
                   "/" ui/thin-space "…"]
                  [:span
                   {:aria-label (str "maximum available quantity is " max-available)}
                   "/" ui/thin-space max-available ui/thin-space "max."])]]]

             [:div.flex-auto.px-2.w-1_2
              [:button.px-4.py-2.w-100.rounded-lg.bg-content-inverse.text-color-content-inverse.font-semibold.text-lg
               {:disabled submit-disabled? :type :submit}
               "Order"]]]]])))))

(defn view []
  (let
   [routing @(rf/subscribe [:routing/routing])
    model-id (get-in routing [:bidi-match :route-params :model-id])
    params (get-in routing [:bidi-match :query-params])
    fetched @(rf/subscribe [::model-data model-id])
    model (get-in fetched [:data :model])
    errors (:errors fetched)
    is-loading? (not (or model errors))]

    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div [ui/spinner-clock]] [:pre "loading model" [:samp model-id] "…"]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header
         [:h1.text-3xl.font-extrabold.leading-none
          (:name model)
          [:span " "]
          [:small.font-normal.text-gray-600.leading-none (:manufacturer model)]]]

         ; FIXME: show all images not just the first one
        (if-let [first-image (first (:images model))]
          [:div.flex.justify-center.py-4.mt-4.border-b-2.border-gray-300
           [:div [:img {:src (:imageUrl first-image)}]]])

        [order-panel model params]

        (if-let [description (:description model)]
          [:p.py-4.border-b-2.border-gray-300.preserve-linebreaks description])

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
           [:div.flex.flex-wrap.-mx-2
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
                     (if-let [img (get-in rec [:images 0 :imageUrl])]
                       [:img.absolute.object-contain.object-center.h-full.w-full.p-1 {:src img}]
                       [:div.absolute.h-full.w-full.bg-gray-400 " "])]]

                   [:a.text-gray-700.font-semibold {:href href}
                    (:name rec)]]])))]])

        #_[:p.debug (pr-str model)]])]))
