(ns leihs.borrow.features.models.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [akiroz.re-frame.storage :refer [persist-db]]
   [re-frame.core :as rf]
   [re-frame.db :as db]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.lib.helpers :refer [spy spy-with log obj->map]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.pagination :as pagination]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   ["/leihs-ui-client-side-external-react" :as UI]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.models.filter-modal :as filter-modal :refer [filter-comp default-dispatch-fn]]
   [leihs.core.core :refer [remove-blanks]]))

(set-default-translate-path :borrow.models)

(def query-gql
  (rc/inline "leihs/borrow/features/models/getModels.gql"))

;-; EVENTS 
(reg-event-fx
 ::routes/models
 (fn-traced [_ [_ {:keys [query-params]}]]
   {:dispatch-n (list [::filter-modal/save-options query-params]
                      [::current-user/set-chosen-user-id (:user-id query-params)]
                      [::get-models query-params])}))

(defn prepare-query-vars [filters]
  (let [term (:term filters)
        start-date (:start-date filters)
        end-date (:end-date filters)
        user-id (:user-id filters)
        pool-id (:pool-id filters)
        quantity (:quantity filters)
        only-available (:only-available filters)
        dates-valid? (<= start-date end-date)] ; if somehow end is before start, ignore it instead of error
    (cond-> {:bothDatesGiven (boolean (and start-date end-date dates-valid?))}
      term
      (assoc :searchTerm term)
      quantity
      (assoc :quantity quantity)
      (when dates-valid? start-date)
      (assoc :startDate start-date)
      (when dates-valid? end-date)
      (assoc :endDate end-date)
      (when dates-valid? only-available)
      (assoc :onlyAvailable only-available)
      pool-id
      (assoc :poolIds [pool-id])
      user-id
      (assoc :userId user-id))))

(def BOOLEANS #{:only-available})
(def INTEGERS #{:quantity})

(defn with-parsed-json-values [m]
  (->> m
       (map (fn [[k v]]
              [k (cond (BOOLEANS k) (js/JSON.parse v)
                       (INTEGERS k) (js/Number v)
                       :else v)]))
       (into {})))

(defn get-query-vars [query-params extra-vars]
  (-> query-params
      with-parsed-json-values
      remove-blanks
      prepare-query-vars
      (merge extra-vars)))

(defn number-of-cached [db cache-key]
  (some-> db :ls ::data (get cache-key) :edges count))

(defn get-cache-key [query-params extra-vars]
  (-> query-params (get-query-vars extra-vars) hash))

(reg-event-fx
 ::get-models
 (fn-traced [{:keys [db]} [_ query-params extra-vars]]
   (let [query-vars (get-query-vars query-params extra-vars)
         cache-key (get-cache-key query-params extra-vars)
         n (number-of-cached db cache-key)]
     {:dispatch [::re-graph/query
                 query-gql
                 (cond-> query-vars (>= n 20) (assoc :first n))
                 [::on-fetched-models cache-key]]})))

(reg-event-fx
 ::on-fetched-models
 (fn-traced [{:keys [db]} [_ cache-key {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:ls ::data cache-key] (get-in data [:models]))})))

(reg-event-fx
 ::clear
 (fn-traced [_ _]
   {:dispatch-n (list [::filter-modal/clear-options]
                      [::clear-data]
                      [:routing/navigate [::routes/home]])}))

(reg-event-db
 ::clear-data
 (fn-traced [db _] (update db :ls dissoc ::data)))

(reg-event-db ::clear-data-under-key
              (fn-traced [db [_ cache-key]]
                (update-in db [:ls ::data] dissoc cache-key)))

(reg-sub ::cache-key
         (fn [db [_ filter-opts extra-vars]]
           (get-cache-key filter-opts extra-vars)))

(reg-sub ::data-under-cache-key
         (fn [db [_ cache-key]]
           (-> (get-in db [:ls ::data cache-key]))))

(reg-sub ::edges
         (fn [[_ cache-key] _]
           (subscribe [::data-under-cache-key cache-key]))
         (fn [d _] (:edges d)))

(reg-sub ::fetching
         (fn [[_ cache-key] _]
           (subscribe [::data-under-cache-key cache-key]))
         (fn [data _] (:fetching data)))

(reg-sub ::has-next-page?
         (fn [[_ cache-key] _]
           (subscribe [::data-under-cache-key cache-key]))
         (fn [data _] (-> data :page-info :has-next-page)))

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

(defn search-panel [submit-fn clear-fn extra-vars]
  (let [current-user-data @(subscribe [::current-user/data])
        user-data (:user current-user-data)
        filters @(subscribe [::filter-modal/options])
        pools @(subscribe [::current-user/pools])
        routing @(subscribe [:routing/routing])
        cache-key @(subscribe [::cache-key filters extra-vars])
        on-search-view? (= (get-in routing [:bidi-match :handler]) ::routes/models)]
    [:> UI/Components.ModelFilterForm
     {:key cache-key ; force reload of internal state when filters etc change
      :className (str "mt-3 mb-3" (when on-search-view? ""))
      :initialTerm (:term filters)
      :initialUserId (:user-id filters)
      :initialPoolId (:pool-id filters)
      :initialOnlyAvailable (:only-available filters)
      :initialStartDate (:start-date filters)
      :initialEndDate (:end-date filters)
      :initialQuantity (:quantity filters)
      :user user-data
      :delegations (:delegations user-data)
      :pools pools
      :onSubmit #(submit-fn (obj->map %))
      :onClear clear-fn}]))

(defn models-list [models]
  (let
   [debug? @(subscribe [:is-debug?])
    models-list (doall
                 (for [m models]
                   (let [model (:node m)
                         max-quant (:available-quantity-in-date-range model)
                         unavailable? (and max-quant (<= max-quant 0))]
                     {:id (:id model)
                      :imgSrc (or (get-in model [:cover-image :image-url])
                                  (get-in model [:images 0 :image-url]))
                      :isDimmed false
                      :caption (:name model)
                      :subCaption (:manufacturer model)
                      :href  (routing/path-for ::routes/models-show
                                               :model-id (:id model))
                      :isFavorited (:is-favorited model)})))]
    [:<>
     [:> UI/Components.ModelList {:list models-list}]
     (when debug? [:p (pr-str @(subscribe [::data]))])]))

(defn load-more [cache-key extra-vars]
  (let [fetching-more? @(subscribe [::fetching cache-key])
        has-next-page? @(subscribe [::has-next-page? cache-key])
        filters @(subscribe [::filter-modal/options])
        dates-valid? (<= (:start-date filters) (:end-date filters))]
    [:<>
     (if (and fetching-more? dates-valid?)
       [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (when has-next-page?
         [:div.p-3.text-center
          [:button.border.border-black.p-2.rounded
           {:on-click #(dispatch [::pagination/get-more
                                  query-gql
                                  (get-query-vars filters extra-vars)
                                  [:ls ::data cache-key]
                                  [:models]])}
           (t :!borrow.pagination/load-more)]]))]))

(defn search-results [extra-vars]
  (let [filter-opts @(subscribe [::filter-modal/options])
        cache-key @(subscribe [::cache-key filter-opts extra-vars])
        models @(subscribe [::edges cache-key])]
    [:<>
     (cond
       (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (empty? models) [:p.p-6.w-full.text-center (t :!borrow.pagination/nothing-found)]
       :else
       [:<>
        [models-list models]
        [load-more cache-key extra-vars]])]))

(defn view []
  (let [extra-search-vars nil]
    [:> UI/Components.Design.PageLayout
     [:> UI/Components.Design.PageLayout.Header {:title (t :title)}
      [filter-comp default-dispatch-fn]]
     [:> UI/Components.Design.Stack
      [search-results extra-search-vars]]]))
