(ns leihs.borrow.client.features.models.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   [reagent.core :as r]
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
  (rc/inline "leihs/borrow/client/features/models/getModels.gql"))

;-; EVENTS 
(rf/reg-event-fx
 ::routes/models
 (fn [_ [_ {:keys [query-params]}]]
   {:dispatch-n (list [::filters/set-multiple query-params]
                      [::get-models])}))

(ls/reg-event-fx
 ::get-models
 (fn [{:keys [db]} [_ extra-args]]
   (let [filters (filters/current db)
         start-date (:start-date filters)
         end-date (:end-date filters)
         dates-valid? (<= start-date end-date) ; if somehow end is before start, ignore it instead of error
         query-vars (merge {:searchTerm (:term filters)
                            :startDate (when dates-valid? start-date)
                            :endDate (when dates-valid? end-date)
                            :onlyAvailable (when dates-valid? (:available-between? filters))
                            :bothDatesGiven (boolean (and dates-valid? start-date end-date))}
                           extra-args)]
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


(rf/reg-sub ::fetching
            (fn [db _] (get-in db [::results :fetching])))

(rf/reg-sub ::has-next-page?
            (fn [db _] (get-in db [::results :pageInfo :hasNextPage])))

(rf/reg-sub
 ::results
 (fn [db] (-> (get-in db [::results :edges]))))

;-; VIEWS
(defn form-line [name label input-props]
  [:label.row
   [:span.text-xs.col-3.col-form-label
    (str label " ")]
   [:div.col-9
    [:input
     (merge
      input-props
      {:name name
       :placeholder label
       :class (str "form-control " (get input-props :class))
       :style (merge (get input-props :style))})]]])

(def product-card-width-in-rem 12)
(def product-card-margins-in-rem 1)

(defn search-panel [submit-fn clear-fn]
  (let [filters @(rf/subscribe [::filters/current])
        term @(rf/subscribe [::filters/term])
        results @(rf/subscribe [::results])
        start-date @(rf/subscribe [::filters/start-date])
        end-date @(rf/subscribe [::filters/end-date])
        available-between? @(rf/subscribe [::filters/available-between?])
        quantity @(rf/subscribe [::filters/quantity])
        routing @(rf/subscribe [:routing/routing])
        on-search-view? (= (get-in routing [:bidi-match :handler]) ::routes/models)]

    [:div.px-3.py-4.bg-light {:class (when on-search-view? "mb-4")
                              :style {:box-shadow "0 0rem 2rem rgba(0, 0, 0, 0.15) inset"}}
     [:form.form.form-compact
      {:action "/search"
       :on-submit (fn [event] (.preventDefault event) (submit-fn filters))}

      [:div.form-group
       [form-line :search-term "Suche"
        {:type :text
         :value term
         :on-change #(rf/dispatch [::filters/set-one :term (-> % .-target .-value)])}]]

      [:div.form-group
       [:div.row
        [:span.text-xs.col-3.col-form-label
         (str "Zeitraum" " ")]
        [:div.col-9
         [:label.custom-control.custom-checkbox
          [:input.custom-control-input
           {:type :checkbox
            :name :only-available
            :checked available-between?
            :on-change (fn [_]
                         (rf/dispatch [::filters/set-one :available-between? (not available-between?)]))}]
          [:span.custom-control-label "nur Verfügbare anzeigen"]]]]]


      [:div {:class (if-not available-between? "d-none")}
       [:div.form-group
        [form-line :start-date "von"
         {:type :date
          :required true
          :disabled (not available-between?)
          :value start-date
          :on-change #(rf/dispatch [::filters/set-one :start-date (-> % .-target .-value)])}]]

       [:div.form-group
        [form-line :end-date "bis"
         {:type :date
          :required true
          :disabled (not available-between?)
          :min start-date
          :value end-date
          :on-change #(rf/dispatch [::filters/set-one :end-date (-> % .-target .-value)])}]]

       [:div.form-group
        [form-line :quantity "Anzahl"
         {:type :number
          :min 1
          :value quantity
          :on-change #(rf/dispatch [::filters/set-one :quantity (-> % .-target .-value)])}]]]

      [:button.btn.btn-success.dont-invert.rounded-pill
       {:type :submit
        :class :mt-2}
       "Get Results"]

      [:button.btn.btn-secondary.dont-invert.rounded-pill.mx-1
       {:type :button
        :disabled (not (or (seq filters) (seq results)))
        :on-click clear-fn
        :class :mt-2}
       "Clear"]]]))

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
                      :isDimmed false
                      :caption (:name model)
                      :subCaption (:manufacturer model)
                      :href  (routing/path-for ::routes/models-show
                                               :model-id (:id model))})))]
    [:div.mx-1.mt-2
     [:> UI/Components.CategoryList {:list models-list}]
     (when debug? [:p (pr-str @(rf/subscribe [::results]))])]))

(defn load-more [extra-args]
  (let [fetching-more? @(rf/subscribe [::fetching])
        has-next-page? @(rf/subscribe [::has-next-page?])
        filters @(rf/subscribe [::filters/current])
        term (:term filters)
        start-date (:start-date filters)
        end-date (:end-date filters)
        dates-valid? (<= start-date end-date) ; if somehow end is before start, ignore it instead of error
        only-available? (:only-available? filters)]
    [:div
     [:hr]
     (if (and fetching-more? dates-valid?)
       [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (when has-next-page?
         [:div.p-3.text-center
          [:button.border.border-black.p-2.rounded
           {:on-click #(rf/dispatch [::pagination/get-more
                                     query-gql
                                     (merge {:searchTerm term
                                             :startDate start-date
                                             :endDate end-date
                                             :onlyAvailable only-available?
                                             :bothDatesGiven (boolean (and dates-valid? start-date end-date))}
                                            extra-args)
                                     [::results]
                                     [:models]])}
           "LOAD MORE"]]))]))

(defn search-and-list [submit-fn clear-fn extra-params]
  (let [models @(rf/subscribe [::results])]
    [:<>
     [search-panel submit-fn clear-fn]
     (cond
       (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (empty? models) [:p.p-6.w-full.text-center "nothing found!"]
       :else
       [:<>
        [models-list models]
        [load-more extra-params]])]))

(defn view []
  [search-and-list
   #(rf/dispatch [:routing/navigate [::routes/models {:query-params %}]])
   #(rf/dispatch [::clear %])
   nil])
