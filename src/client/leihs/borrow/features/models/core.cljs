(ns leihs.borrow.features.models.core
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [reagent.core :as r]
    [akiroz.re-frame.storage :refer [persist-db]]
    [re-frame.core :as rf]
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

(reg-event-fx
  ::get-models
  (fn-traced [{:keys [db]} [_ extra-args]]
    (let [filters (filters/current db)
          start-date (:start-date filters)
          end-date (:end-date filters)
          user-id (or (:user-id filters) (-> db current-user/data :user :id))
          dates-valid? (<= start-date end-date) ; if somehow end is before start, ignore it instead of error
          query-vars (merge {:searchTerm (:term filters)
                             :startDate (when dates-valid? start-date)
                             :endDate (when dates-valid? end-date)
                             :onlyAvailable (when dates-valid? (:available-between? filters))
                             :userId user-id
                             :bothDatesGiven (boolean (and dates-valid? start-date end-date))}
                            extra-args)]
      ; NOTE: no caching yet, clear data before new search  
      {:db (assoc-in db [::data] nil)
       :dispatch [::re-graph/query
                  query-gql
                  query-vars
                  [::on-fetched-models]]})))

(reg-event-fx
  ::on-fetched-models
  (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:db (assoc-in db [::data] (get-in data [:models]))})))

(reg-event-fx
  ::clear
  (fn-traced [_ _]
    {:dispatch-n (list [::filters/clear-current]
                       [::clear-data]
                       [:routing/navigate [::routes/home]])}))

(reg-event-db
  ::clear-data
  (fn-traced [db _] (dissoc db ::data)))


(reg-sub ::fetching
         (fn [db _] (get-in db [::data :fetching])))

(reg-sub ::has-next-page?
         (fn [db _] (get-in db [::data :page-info :has-next-page])))

(reg-sub
  ::data
  (fn [db] (-> (get-in db [::data :edges]))))

(reg-sub ::target-users
         :<- [::current-user/data]
         (fn [cu]
           (let [delegations (:delegations cu)]
             (when (not-empty delegations)
               (concat [(:user cu)] delegations)))))

(reg-sub ::user-id
         :<- [::current-user/data]
         :<- [::filters/user-id]
         (fn [[co user-id]]
           (or user-id (-> co :user :id))))

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
  (let [filters @(subscribe [::filters/current])
        target-users @(subscribe [::target-users])
        term @(subscribe [::filters/term])
        data @(subscribe [::data])
        start-date @(subscribe [::filters/start-date])
        end-date @(subscribe [::filters/end-date])
        available-between? @(subscribe [::filters/available-between?])
        quantity @(subscribe [::filters/quantity])
        user-id @(subscribe [::user-id])
        routing @(subscribe [:routing/routing])
        on-search-view? (= (get-in routing [:bidi-match :handler]) ::routes/models)]

    [:div.px-3.py-4.bg-light {:class (when on-search-view? "mb-4")
                              :style {:box-shadow "0 0rem 2rem rgba(0, 0, 0, 0.15) inset"}}
     [:form.form.form-compact
      {:action "/search"
       :on-submit (fn [event] (.preventDefault event) (submit-fn filters))}

      [:div.form-group
       [form-line :search-term (t :borrow.filter/search)
        {:type :text
         :value term
         :on-change #(dispatch [::filters/set-one :term (-> % .-target .-value)])}]]

      (when-not (empty? target-users)
        [:label.row
         [:span.text-xs.col-3.col-form-label (t :borrow.filter/for)]
         [:div.col-9
          [:select {:class "form-control"
                    :default-value user-id
                    :name :user-id
                    :on-change #(dispatch [::filters/set-one :user-id (-> % .-target .-value)])}
           (doall
             (for [user target-users]
               [:option {:value (:id user) :key (:id user)}
                (:name user)]))]]])

      [:div.form-group
       [:div.row
        [:span.text-xs.col-3.col-form-label (t :borrow.filter/time-span)]
        [:div.col-9
         [:label.custom-control.custom-checkbox
          [:input.custom-control-input
           {:type :checkbox
            :name :only-available
            :checked available-between?
            :on-change (fn [_]
                         (dispatch [::filters/set-one :available-between? (not available-between?)]))}]
          [:span.custom-control-label (t :borrow.filter/show-only-available)]]]]]


      [:div {:class (if-not available-between? "d-none")}
       [:div.form-group
        [form-line :start-date (t :borrow.filter/from)
         {:type :date
          :required true
          :disabled (not available-between?)
          :value start-date
          :on-change #(dispatch [::filters/set-one :start-date (-> % .-target .-value)])}]]

       [:div.form-group
        [form-line :end-date (t :borrow.filter/until)
         {:type :date
          :required true
          :disabled (not available-between?)
          :min start-date
          :value end-date
          :on-change #(dispatch [::filters/set-one :end-date (-> % .-target .-value)])}]]

       [:div.form-group
        [form-line :quantity (t :borrow.filter/quantity)
         {:type :number
          :min 1
          :value quantity
          :on-change #(dispatch [::filters/set-one :quantity (-> % .-target .-value)])}]]]

      [:button.btn.btn-success.dont-invert.rounded-pill
       {:type :submit
        :class :mt-2}
       (t :borrow.filter/get-results)]

      [:button.btn.btn-secondary.dont-invert.rounded-pill.mx-1
       {:type :button
        :disabled (not (or (seq filters) (seq data)))
        :on-click clear-fn
        :class :mt-2}
       (t :borrow.filter/clear)]]]))

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
    [:div.mx-1.mt-2
     [:> UI/Components.CategoryList {:list models-list}]
     (when debug? [:p (pr-str @(subscribe [::data]))])]))

(defn load-more [extra-args]
  (let [fetching-more? @(subscribe [::fetching])
        has-next-page? @(subscribe [::has-next-page?])
        filters @(subscribe [::filters/current])
        term (:term filters)
        start-date (:start-date filters)
        end-date (:end-date filters)
        user-id (:user-id filters)
        dates-valid? (<= start-date end-date) ; if somehow end is before start, ignore it instead of error
        only-available? (:only-available? filters)]
    [:div
     [:hr]
     (if (and fetching-more? dates-valid?)
       [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (when has-next-page?
         [:div.p-3.text-center
          [:button.border.border-black.p-2.rounded
           {:on-click #(dispatch [::pagination/get-more
                                  query-gql
                                  (merge {:searchTerm term
                                          :startDate start-date
                                          :endDate end-date
                                          :onlyAvailable only-available?
                                          :userId user-id
                                          :bothDatesGiven (boolean (and dates-valid? start-date end-date))}
                                         extra-args)
                                  [::data]
                                  [:models]])}
           (t :borrow.pagination/load-more)]]))]))

(defn search-and-list [submit-fn clear-fn extra-params]
  (let [models @(subscribe [::data])]
    [:<>
     [search-panel submit-fn clear-fn]
     (cond
       (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (empty? models) [:p.p-6.w-full.text-center (t :borrow.pagination/nothing-found)]
       :else
       [:<>
        [models-list models]
        [load-more extra-params]])]))

(defn view []
  [search-and-list
   #(dispatch [:routing/navigate [::routes/models {:query-params %}]])
   #(dispatch [::clear %])
   nil])
