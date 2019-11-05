(ns leihs.borrow.client.features.search
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]))

;-; EVENTS 
(rf/reg-event-fx
 ::on-fetched-search-filters
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:search :filters :available] data)})))

(rf/reg-event-db
 ::set-filter
 (fn [db [_ key value]]
   (assoc-in db [:search :filters :current key] value)))

(rf/reg-event-fx
 ::get-models
 (fn [_ [_ filters]]
   (let
    [query-vars
     {:searchTerm (get filters :term)
      :startDate (get filters :start-date)
      :endDate (get filters :end-date)
      ;( TODO: cats & pools :categories (map :id (get filters :end-date)))
      }]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/client/queries/searchModels.gql")
                 query-vars
                 [::on-fetched-models]]})))

(rf/reg-event-fx
 ::on-fetched-models
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:search :results] (get-in data [:models]))})))


;-; SUBSCRIPTIONS 
(rf/reg-sub ::available-filters (fn [db] (get-in db [:search :filters :available] nil)))
(rf/reg-sub ::current-filters (fn [db] (get-in db [:search :filters :current] nil)))

(rf/reg-sub ::found-models (fn [db] (get-in db [:search :results] nil)))

;-; VIEWS
(defn form-line [name label input-props]
  [:label {:style {:display :table-row}}
   [:span.w-8 {:style {:display :table-cell :padding-right "0.5rem"}} (str label " ")]
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
     [current @(rf/subscribe [::current-filters])]
      [:form.p-3
       {:on-submit (fn [e] (.preventDefault e) (rf/dispatch [::get-models current]))}
       [:fieldset {:style {:display :table}}
        [:legend "SUCHE"]
        [form-line :search-term "Text"
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

