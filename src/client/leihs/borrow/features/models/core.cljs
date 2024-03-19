(ns leihs.borrow.features.models.core
  (:require ["/borrow-ui" :as UI]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [leihs.borrow.client.routes :as routes]
            [leihs.borrow.components :as ui]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.features.models.model-filter :as filter-modal :refer [default-dispatch-fn
                                                                                filter-comp]]
            [leihs.borrow.lib.pagination :as pagination]
            [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db
                                               reg-event-fx reg-sub subscribe]]
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.lib.translate :refer [set-default-translate-path t]]
            [leihs.core.core :refer [remove-blanks]]
            [re-graph.core :as re-graph]
            [shadow.resource :as rc]))

(set-default-translate-path :borrow.models)

(def query-gql
  (rc/inline "leihs/borrow/features/models/getModels.gql"))

;-; EVENTS 
(reg-event-fx
 ::routes/models
 (fn-traced [_ [_ {:keys [query-params]}]]
   {:dispatch [::get-models query-params]}))

(defn prepare-query-vars [filters]
  (let [term (:term filters)
        start-date (:start-date filters)
        end-date (:end-date filters)
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
      (assoc :poolIds [pool-id]))))

(def BOOLEANS #{:only-available})
(def INTEGERS #{:quantity})

(defn with-parsed-json-values [m]
  (->> m
       (map (fn [[k v]]
              [k (cond (BOOLEANS k) (js/JSON.parse v)
                       (INTEGERS k) (js/Number v)
                       :else v)]))
       (into {})))

; Arguments for the graphql query
(defn get-query-vars [query-params extra-vars profile-id]
  (-> query-params
      with-parsed-json-values
      remove-blanks
      prepare-query-vars
      (merge extra-vars {:userId profile-id})))

(defn number-of-cached [db cache-key]
  (some-> db :ls ::data (get cache-key) :edges count))

(defn get-cache-key [query-params]
  (-> query-params hash))

(reg-event-fx
 ::get-models
 (fn-traced [{:keys [db]} [_ query-params extra-vars]]
   (let [query-vars (get-query-vars query-params extra-vars (current-user/get-current-profile-id db))
         cache-key (get-cache-key query-vars)
         n (number-of-cached db cache-key)]
     {:dispatch [::re-graph/query
                 query-gql
                 (cond-> query-vars (>= n 20) (assoc :first n))
                 [::on-fetched-models cache-key]]})))

(reg-event-fx
 ::on-fetched-models
 (fn-traced [{:keys [db]} [_ cache-key {:keys [data errors]}]]
   (if errors
     {:db (assoc-in db [:ls ::data cache-key] {:errors errors})}
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
           (get-cache-key (get-query-vars filter-opts extra-vars (current-user/get-current-profile-id db)))))

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

(reg-sub ::errors
         (fn [[_ cache-key] _]
           (subscribe [::data-under-cache-key cache-key]))
         (fn [data _] (:errors data)))

;-; VIEWS

(defn models-list [models model-filters]
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
                                               :model-id (:id model)
                                               :query-params model-filters)
                      :isFavorited (:is-favorited model)})))]
    [:<>
     [:> UI/Components.ModelList {:list models-list}]
     (when debug? [:p (pr-str @(subscribe [::data]))])]))

(defn load-more [cache-key extra-vars]
  (let [profile-id @(subscribe [::current-user/current-profile-id])
        fetching-more? @(subscribe [::fetching cache-key])
        has-next-page? @(subscribe [::has-next-page? cache-key])
        filters @(subscribe [::filter-modal/options])
        dates-valid? (<= (:start-date filters) (:end-date filters))]
    [:<>
     (if (and fetching-more? dates-valid?)
       [ui/loading]
       (when has-next-page?
         [:div.p-3.text-center
          [:button.btn.btn-outline-primary
           {:on-click #(dispatch [::pagination/get-more
                                  query-gql
                                  (get-query-vars filters extra-vars profile-id)
                                  [:ls ::data cache-key]
                                  [:models]])}
           (t :!borrow.pagination/load-more)]]))]))

(defn search-results [extra-vars]
  (let [filter-opts @(subscribe [::filter-modal/options])
        cache-key @(subscribe [::cache-key filter-opts extra-vars])
        models @(subscribe [::edges cache-key])
        errors @(subscribe [::errors cache-key])]
    [:<>
     (cond
       (not (or errors models)) [ui/loading]
       errors [ui/error-view errors]
       (empty? models) [:div.text-center (t :no-items-found)]
       :else
       [:<>
        [models-list models filter-opts]
        [load-more cache-key extra-vars]])]))

(defn view []
  (let [extra-search-vars nil]
    [:> UI/Components.Design.PageLayout.ContentContainer
     [:> UI/Components.Design.PageLayout.Header {:title (t :title)}
      [:div.pt-2 [filter-comp default-dispatch-fn]]]
     [:> UI/Components.Design.Stack
      [search-results extra-search-vars]]]))
