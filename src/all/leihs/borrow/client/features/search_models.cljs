(ns leihs.borrow.client.features.search-models
  (:require-macros [leihs.borrow.client.macros :refer [spy]])
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui]
   #_[leihs.borrow.client.features.shopping-cart :as cart]))

(def query-gql
  (rc/inline "leihs/borrow/client/queries/searchModels.gql"))

(def search-filters-gql
  (rc/inline "leihs/borrow/client/queries/getSearchFilters.gql"))

;-; EVENTS 
(rf/reg-event-fx
 ::routes/search
 (fn [{:keys [db]} [_ args]]
   (let [params (get-in args [:query-params])
         filters {:term (get params :term)
                  :start-date (get params :start-date)
                  :end-date (get params :end-date)
                  ;( TODO: categories & pools 
                  }]
     {:dispatch-n (list
                   [::set-filters filters]
                   [::get-models filters])})))

(rf/reg-event-fx
 ::fetch-search-filters
 (fn [_ [_ _]]
   {:dispatch [::re-graph/query
               search-filters-gql
               {}
               [::on-fetched-search-filters]]}))

;tmp
(defn fetch-search-filters []
  (rf/dispatch-sync [::re-graph/query
                     search-filters-gql
                     {}
                     [::on-fetched-search-filters]]))

(rf/reg-event-fx
 ::on-fetched-search-filters
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:search :filters :available] data)})))

(rf/reg-event-db
 ::set-filters
 (fn [db [_ filters]]
   (assoc-in db [:search :filters :current] filters)))

(rf/reg-event-db
 ::set-filter
 (fn [db [_ key value]]
   (assoc-in db [:search :filters :current key] value)))

(rf/reg-event-fx
 ::get-models
 (fn [{:keys [db]} [_ filters]]
   (let
    [query-vars
     {:searchTerm (get filters :term)
      :startDate (get filters :start-date)
      :endDate (get filters :end-date)
      ;TODO: categories & pools 
      }]

     {; NOTE: no caching yet, clear results before new search  
      :db (assoc-in db [:search :results] nil)

      :dispatch-n (list
                   [:routing/navigate [::routes/search {:query-params filters}]]
                   [::re-graph/query
                    query-gql
                    query-vars
                    [::on-fetched-models]])})))

(rf/reg-event-fx
 ::on-fetched-models
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:search :results] (get-in data [:models]))})))

;-; SUBSCRIPTIONS 
(rf/reg-sub ::available-filters
            (fn [db] (get-in db [:search :filters :available] nil)))
(rf/reg-sub ::current-filters
            (fn [db] (get-in db [:search :filters :current] nil)))
(rf/reg-sub ::search-results
            (fn [db] (-> (get-in db [:search :results :edges]))))

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
      current @(rf/subscribe [::current-filters])
      on-search-view? (= (get-in routing [:bidi-match :handler]) ::routes/search)]
      [:form.p-3
       {:action "/search"
        :on-submit
        (fn [event]
          (.preventDefault event)
          (if on-search-view?
            (rf/dispatch [::get-models current])
            (rf/dispatch [:routing/navigate [::routes/search {:query-params current}]])))}
       [:fieldset {:style {:display :table}}
        [:legend.sr-only "Suche"]
        [form-line :search-term "Suche"
         {:type :text
          :value (get current :term)
          :on-change #(rf/dispatch [::set-filter :term (-> % .-target .-value)])}]

        [form-line :start-date "Start-datum"
         {:type :date
          :required true
          :value (get current :start-date)
          :on-change #(rf/dispatch [::set-filter :start-date (-> % .-target .-value)])}]

        [form-line :end-date "End-datum"
         {:type :date
          :required true
          :min (get current :start-date)
          :value (get current :end-date)
          :on-change #(rf/dispatch [::set-filter :end-date (-> % .-target .-value)])}]

        #_[form-line :category "Kategorie"
           {:type :select
            :value (get current :categories)
            :on-change #(rf/dispatch [::set-filter :categories (-> % .-target .-value)])}]

        #_[form-line :pool "Pool"
           {:type :select
            :value (get current :pools)
            :on-change #(rf/dispatch [::set-filter :pools (-> % .-target .-value)])}]

        (let [active? (boolean (and (get current :start-date) (get current :end-date)))]
          [ui/button "Get Results"
           active?
           {:type :submit
            :title (when-not active? "Select start- and end-date to search!")
            :class :mt-2}])]])))

(def product-card-width-in-rem 12)
(def product-card-margins-in-rem 1)

; (defn product-card [model width-in-rem]
;   (let 
;    [href (routing/path-for ::routes/models-show (:id model))]
;     [:div.ui-product-card
;      {:style {:width (str width-in-rem "rem")
;               :min-height "15rem"
;               :overflow-y "scroll"
;               :border "1px solid tomato"
;               :padding "1rem"
;               :display "inline-block"
;               :margin-right "1rem"}}
;      [:h2 [:a {:href href} (:name model)]]
;      (if-let [img (get-in model [:images 0 :imageUrl])]
;        [:img {:src img :style {:width "100%"}}])
;      #_[:p (pr-str model)]
;      [:button
;       {:type :button :on-click #(rf/dispatch [::cart/add-item (:id model)])}
;       "+"]]))

(defn model-grid-item [model]
  (let [routing @(rf/subscribe [:routing/routing])
        params (get-in routing [:bidi-match :query-params])
        max-quant (:availableQuantityInDateRange model)
        available? (> max-quant 0)
        model-show-params {:end (:end-date params) :start (:start-date params) :maxQuantity max-quant}
        href (routing/path-for ::routes/models-show
                               :model-id (:id model)
                               :query-params model-show-params)]
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
     (when debug? [:p (pr-str @(rf/subscribe [::search-results]))])]))

(defn view
  []
  (let [models @(rf/subscribe [::search-results])
        {:keys [term start-date end-date]} @(rf/subscribe [::current-filters])]
    [:<>
     [search-panel]
     #_[:p (pr-str models)]
     (cond
       (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (empty? models) [:p.p-6.w-full.text-center "nothing found!"]
       :else
       [:<>
        #_[:div
        [:h1 (str "This is search view ")
        [:p "params:"]
        [:pre (pr-str (get-in routing [:bidi-match :query-params]))]
        [:hr]]]
        [products-list models]
        [:hr]
        [:div.p-3.text-center
         [:button.border.border-black.p-2.rounded
          {:on-click #(rf/dispatch [::pagination/get-more
                                    query-gql
                                    {:searchTerm term,
                                     :startDate start-date,
                                     :endDate end-date}
                                    [:search :results]
                                    [:models]])}
          "LOAD MORE"]]])]))
