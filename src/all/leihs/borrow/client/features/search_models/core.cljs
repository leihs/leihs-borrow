(ns leihs.borrow.client.features.search-models.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
    #_[reagent.core :as r]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.client.lib.filters :as filters]
    [leihs.borrow.client.lib.routing :as routing]
    [leihs.borrow.client.lib.pagination :as pagination]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.client.components :as ui]
    #_[leihs.borrow.client.features.shopping-cart.core :as cart]))

(def query-gql
  (rc/inline "leihs/borrow/client/features/search_models/searchModels.gql"))

;-; EVENTS 
(rf/reg-event-fx
  ::routes/search
  (fn [_ _] {:dispatch [::get-models]}))

(rf/reg-event-fx
  ::get-models
  (fn [{:keys [db]} _]
    (let [filters (filters/current db)
          query-vars {:searchTerm (filters ::filters/term)
                      :startDate (filters ::filters/start-date)
                      :endDate (filters ::filters/end-date)}]
      ; NOTE: no caching yet, clear results before new search  
      {:db (assoc-in db [::results] nil)
       :dispatch [::re-graph/query
                  query-gql
                  (spy query-vars)
                  [::on-fetched-models]]})))

(rf/reg-event-fx
  ::on-fetched-models
  (fn [{:keys [db]} [_ {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:db (assoc-in db [::results] (get-in data [:models]))})))

(rf/reg-event-fx
  ::clear
  (fn [_ _]
    {:dispatch-n (list [::filters/clear-current]
                       [::clear-results]
                       [:routing/navigate [::routes/home]])}))

(rf/reg-event-db
  ::clear-results
  (fn [db _] (dissoc db ::results)))

(rf/reg-sub
  ::results
  (fn [db] (-> (get-in db [::results :edges]))))

;-; VIEWS
(defn form-line [name label input-props]
  [:label {:style {:display :table-row}}
   [:span.w-8.text-sm {:style {:display :table-cell :padding-right "0.5rem"}}
    (str label " ")]
   [:div.block.w-full {:style {:display :table-cell}}
    [:input.py-1.px-2.mb-1
     (merge
       input-props
       {:name name
        :placeholder label
        :class "appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded leading-tight focus:outline-none focus:bg-white"
        :style (merge (get input-props :style))})]]])

(defn search-panel []
  (fn []
    (let
      [routing @(rf/subscribe [:routing/routing])
       filters @(rf/subscribe [::filters/current])
       term @(rf/subscribe [::filters/term])
       results @(rf/subscribe [::results])
       start-date @(rf/subscribe [::filters/start-date])
       end-date @(rf/subscribe [::filters/end-date])
       on-search-view? (= (get-in routing [:bidi-match :handler]) ::routes/search)]
      [:div.p-3
       [:form
        {:action "/search"
         :on-submit
         (fn [event]
           (.preventDefault event)
           (if on-search-view?
             (rf/dispatch [::get-models])
             (rf/dispatch [:routing/navigate [::routes/search]])))}
        [:fieldset {:style {:display :table}}
         [:legend.sr-only "Suche"]
         [form-line :search-term "Suche"
          {:type :text
           :value term
           :on-change #(rf/dispatch [::filters/set-one ::filters/term (-> % .-target .-value)])}]

         [form-line :start-date "Start-datum"
          {:type :date
           :required true
           :value start-date
           :on-change #(rf/dispatch [::filters/set-one ::filters/start-date (-> % .-target .-value)])}]

         [form-line :end-date "End-datum"
          {:type :date
           :required true
           :min end-date
           :value end-date
           :on-change #(rf/dispatch [::filters/set-one ::filters/end-date (-> % .-target .-value)])}]

         (let [active? (boolean (and start-date end-date))]
           [ui/button "Get Results"
            active?
            {:type :submit
             :title (when-not active? "Select start- and end-date to search!")
             :class :mt-2}])]]

       (if (or (seq filters) (seq results)) 
         [ui/button "Clear"
          true
          {:type :button
           :on-click #(rf/dispatch [::clear])
           :class :mt-2}])])))

(def product-card-width-in-rem 12)
(def product-card-margins-in-rem 1)

(defn model-grid-item [model]
  (let [routing @(rf/subscribe [:routing/routing])
        params (get-in routing [:bidi-match :query-params])
        max-quant (:availableQuantityInDateRange model)
        available? (> max-quant 0)
        model-show-params {:end (:end-date params) :start (:start-date params) :maxQuantity max-quant}
        href (routing/path-for ::routes/models-show
                               :model-id (:id model))]
    [:div.ui-model-grid-item.max-w-sm.rounded.overflow-hidden.bg-white.px-2.mb-3
     {:style {:opacity (if available? 1 0.35)}}
     [ui/image-square-thumb (get-in model [:images 0]) href]
     [:div.mx-0.mt-1.leading-snug
      [:a {:href href}
       [:span.block.truncate.font-bold (:name model)]
       [:span.block.truncate (:manufacturer model)]]]]))

(defn products-list [models]
  (let
    [debug? @(rf/subscribe [:is-debug?])]
    [:div.mx-1.mt-2
     [:div.w-full.px-0
      [:div.ui-models-list.flex.flex-wrap
       (doall
         (for [m models]
           (let [model (:node m)]
             [:div {:class "w-1/2 min-h-16" :key (:id model)}
              [model-grid-item model]])))]]
     (when debug? [:p (pr-str @(rf/subscribe [::results]))])]))

(defn view
  []
  (let [models @(rf/subscribe [::results])
        term @(rf/subscribe [::filters/term])
        start-date @(rf/subscribe [::filters/start-date])
        end-date @(rf/subscribe [::filters/end-date])]
    [:<>
     [search-panel]
     (cond
       (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (empty? models) [:p.p-6.w-full.text-center "nothing found!"]
       :else
       [:<>
        [products-list models]
        [:hr]
        [:div.p-3.text-center
         [:button.border.border-black.p-2.rounded
          {:on-click #(rf/dispatch [::pagination/get-more
                                    query-gql
                                    {:searchTerm term,
                                     :startDate start-date,
                                     :endDate end-date}
                                    [::results]
                                    [:models]])}
          "LOAD MORE"]]])]))
