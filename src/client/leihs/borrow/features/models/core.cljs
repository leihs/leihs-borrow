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
    [leihs.borrow.lib.translate :refer [t]]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.helpers :refer [spy spy-with log obj->map]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.pagination :as pagination]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.components :as ui]
    ["/leihs-ui-client-side-external-react" :as UI]
    [leihs.borrow.features.current-user.core :as current-user]))

(def query-gql
  (rc/inline "leihs/borrow/features/models/getModels.gql"))

;-; EVENTS 
(reg-event-fx
  ::routes/models
  (fn-traced [_ [_ {:keys [query-params]}]]
    {:dispatch-n (list [::filters/set-multiple query-params]
                       [::get-models])}))

(defn base-query-vars [filters]
  (let [start-date (:start-date filters)
        end-date (:end-date filters)
        user-id (:user-id filters)
        pool-id (:pool-id filters)
        dates-valid? (<= start-date end-date)] ; if somehow end is before start, ignore it instead of error
    (cond-> {:searchTerm (:term filters)
             :startDate (when dates-valid? start-date)
             :endDate (when dates-valid? end-date)
             :onlyAvailable (when dates-valid? (:available-between? filters))
             :quantity (:quantity filters)
             :bothDatesGiven (boolean (and dates-valid? start-date end-date))}
      pool-id
      (assoc :poolIds [pool-id])
      user-id
      (assoc :userId user-id))))

(defn query-vars [filters extra-vars]
  (-> filters base-query-vars (merge extra-vars)))

(defn number-of-cached [db cache-key]
  (some-> db :ls ::data (get cache-key) :edges count))

(reg-event-fx
  ::get-models
  (fn-traced [{:keys [db]} [_ extra-vars]]
    (let [q-vars (-> db filters/current (query-vars extra-vars))
          cache-key (hash q-vars)
          n (number-of-cached db cache-key)]
      {:dispatch [::re-graph/query
                  query-gql
                  (cond-> q-vars n (assoc :first n))
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
    {:dispatch-n (list [::filters/clear-current]
                       [::clear-data]
                       [:routing/navigate [::routes/home]])}))

(reg-event-db
  ::clear-data
  (fn-traced [db _] (update db :ls dissoc ::data)))

(reg-event-db ::clear-data-under-key
              (fn-traced [db [_ cache-key]]
                (update-in db [:ls ::data] dissoc cache-key)))

(reg-sub ::cache-key
         :<- [::filters/current]
         (fn [f [_ extra-vars]]
           (-> f (query-vars extra-vars) hash)))

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

(reg-sub ::target-users
         :<- [::current-user/data]
         (fn [cu]
           (let [delegations (:delegations cu)]
             (when (not-empty delegations)
               (concat [(:user cu)] delegations)))))

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

(defn search-panel [submit-fn clear-fn filters cache-key]
  (let [current-user-data @(subscribe [::current-user/data])
        filters @(subscribe [::filters/current])
        pools @(subscribe [::current-user/pools])
        routing @(subscribe [:routing/routing])
        on-search-view? (= (get-in routing [:bidi-match :handler]) ::routes/models)]
    [:> UI/Components.ModelFilterForm
     {:className (str "mt-3 mb-3" (when on-search-view? ""))
      :initialTerm (:term filters)
      :initialUserId (:user-id filters)
      :initialPoolId (:pool-id filters)
      :initialAvailableBetween (:available-between? filters)
      :initialStartDate (:start-date filters)
      :initialEndDate (:end-date filters)
      :initialQuantity (:quantity filters)
      :user (:user current-user-data)
      :delegations (:delegations current-user-data)
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
                                                 :model-id (:id model))})))]
    [:<>
     [:> UI/Components.ModelList {:list models-list}]
     (when debug? [:p (pr-str @(subscribe [::data]))])]))

(defn load-more [cache-key extra-vars]
  (let [fetching-more? @(subscribe [::fetching cache-key])
        has-next-page? @(subscribe [::has-next-page? cache-key])
        filters @(subscribe [::filters/current])
        dates-valid? (<= (:start-date filters) (:end-date filters))]
    [:<>
     (if (and fetching-more? dates-valid?)
       [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (when has-next-page?
         [:div.p-3.text-center
          [:button.border.border-black.p-2.rounded
           {:on-click #(dispatch [::pagination/get-more
                                  query-gql
                                  (query-vars filters extra-vars)
                                  [:ls ::data cache-key]
                                  [:models]])}
           (t :borrow.pagination/load-more)]]))]))

(defn search-and-list [submit-fn clear-fn extra-vars]
  (let [cache-key @(subscribe [::cache-key extra-vars])
        models @(subscribe [::edges cache-key])
        filters @(subscribe [::filters/current])]
    [:<>
     ^{:key cache-key} [search-panel submit-fn clear-fn filters cache-key]
     (cond
       (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (empty? models) [:p.p-6.w-full.text-center (t :borrow.pagination/nothing-found)]
       :else
       [:<>
        [models-list models]
        [load-more cache-key extra-vars]])]))

(defn view []
  [search-and-list
   #(dispatch [:routing/navigate [::routes/models {:query-params %}]])
   #(dispatch [::clear])
   nil])
