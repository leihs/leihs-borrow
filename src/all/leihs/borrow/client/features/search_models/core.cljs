(ns leihs.borrow.client.features.search-models.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   #_[reagent.core :as r]
   [akiroz.re-frame.storage :refer [persist-db]]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.lib.localstorage :as ls]
   [leihs.borrow.client.lib.filters :as filters]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui]
   ["/leihs-ui-client-side" :as UI]
   #_[leihs.borrow.client.features.shopping-cart.core :as cart]))

(def query-gql
  (rc/inline "leihs/borrow/client/features/search_models/searchModels.gql"))

;-; EVENTS 
(rf/reg-event-fx
  ::routes/search
  (fn [_ [_ {:keys [query-params]}]]
    {:dispatch-n (list [::filters/set-all query-params]
                       [::get-models])}))

(ls/reg-event-fx
 ::get-models
 (fn [{:keys [db]} _]
   (let [query-vars {:searchTerm (filters/term db)
                     :startDate (filters/start-date db)
                     :endDate (filters/end-date db)}]
      ; NOTE: no caching yet, clear results before new search  
      {:db (assoc-in db [::results] nil)
       :dispatch [::re-graph/query
                  query-gql
                  query-vars
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
   [:span.w-8.text-xs {:style {:display :table-cell :padding-right "0.5rem"}}
    (str label " ")]
   [:div.d-block.w-full {:style {:display :table-cell}}
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
       [:form.form.form-compact
        {:action "/search"
         :on-submit
         (fn [event]
           (.preventDefault event)
           (rf/dispatch [:routing/navigate [::routes/search {:query-params filters}]]))}
        [:fieldset {:style {:display :table}}
         [:legend.sr-only "Suche"]
         [form-line :search-term "Suche"
          {:type :text
           :value term
           :on-change #(rf/dispatch [::filters/set-one :term (-> % .-target .-value)])}]

         [form-line :start-date "Start-datum"
          {:type :date
           :required true
           :value start-date
           :on-change #(rf/dispatch [::filters/set-one :start-date (-> % .-target .-value)])}]

         [form-line :end-date "End-datum"
          {:type :date
           :required true
           :min end-date
           :value end-date
           :on-change #(rf/dispatch [::filters/set-one :end-date (-> % .-target .-value)])}]

         (let [active? (boolean (and start-date end-date))]
           [:button.btn.btn-success.dont-invert.rounded-pill
            {:type :submit
             :disabled (not active?)
             :title (when-not active? "Select start- and end-date to search!")
             :class :mt-2}
            "Get Results"])

         [:button.btn.btn-secondary.dont-invert.rounded-pill.mx-1
          {:type :button
           :disabled (not (or (seq filters) (seq results)))
           :on-click #(rf/dispatch [::clear])
           :class :mt-2}
          "Clear"]]]])))

(def product-card-width-in-rem 12)
(def product-card-margins-in-rem 1)


(defn models-list [models]
  (let
   [debug? @(rf/subscribe [:is-debug?])
    models-list (doall
                 (for [m models]
                   (let [model (:node m)
                         max-quant (:availableQuantityInDateRange model)
                         unavailable? (and max-quant (<= max-quant 0))]
                     {:id (:id model)
                      :imgSrc (get-in model [:images 0 :imageUrl])
                      :isDimmed unavailable?
                      :caption (:name model)
                      :subCaption (:manufacturer model)
                      :href  (routing/path-for ::routes/models-show
                                               :model-id (:id model))})))]
    [:div.mx-1.mt-2
     [:> UI/Components.CategoryList {:list models-list}]
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
        [models-list models]
        [:hr]
        [:div.p-3.text-center
         [:button.border.border-black.p-2.rounded
          {:on-click #(rf/dispatch [::pagination/get-more
                                    query-gql
                                    {:searchTerm term
                                     :startDate start-date
                                     :endDate end-date}
                                    [::results]
                                    [:models]])}
          "LOAD MORE"]]])]))
