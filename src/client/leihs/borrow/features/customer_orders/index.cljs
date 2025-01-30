(ns leihs.borrow.features.customer-orders.index
  (:require
   ["/borrow-ui" :as UI]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.customer-orders.order-filter :as order-filter]
   [leihs.borrow.features.customer-orders.status-summary :refer [status-summary]]
   [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db reg-event-fx
                                      reg-sub subscribe]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [set-default-translate-path t]]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]))

(set-default-translate-path :borrow.rentals)

(defn prepare-query-vars [filters]
  (let [from (:from filters)
        until (:until filters)
        term (:term filters)
        state (:state filters)
        pool-id (:pool-id filters)]
    (cond-> {}
      (not-empty term)
      (assoc :searchTerm term)
      from
      (assoc :from from)
      until
      (assoc :until until)
      state
      (assoc :refinedRentalState state)
      pool-id
      (assoc :poolIds [pool-id]))))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/rentals-index
 (fn-traced [{:keys [db]} [_ {:keys [query-params]}]]
   {:dispatch-n
    (list [::set-loading]
          [::re-graph/query
           (rc/inline "leihs/borrow/features/customer_orders/customerOrdersIndex.gql")
           (merge {:userId (current-user/get-current-profile-id db)}
                  (prepare-query-vars query-params))
           [::on-fetched-data]])
    :db (-> db (assoc ::errors nil))}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc ::data data)
       (assoc-in [::data :loading?] false))))

(reg-event-db
 ::set-loading
 (fn-traced [db _] (assoc-in db [::data :loading?] true)))

(reg-sub ::query-params
         (fn [db _]
           (let [known-filter-keys [:term :pool-id :from :until :seq :tab]]
             (->> db
                  :routing/routing :bidi-match :query-params
                  ((fn [h] (update-vals (select-keys h known-filter-keys) #(or % ""))))))))

(reg-sub ::data (fn [db _] (::data db)))
(reg-sub ::errors (fn [db _] (::errors db)))
(reg-sub ::loading?
         :<- [::data]
         (fn [data _] (let [loading? (:loading? data)]
                        (if (nil? loading?) true loading?))))

(reg-sub
 ::open-rentals
 :<- [::data]
 (fn [data _]
   (->> (get-in data [:open-rentals :edges])
        (map :node)
        not-empty)))

(reg-sub
 ::closed-rentals
 :<- [::data]
 (fn [data _]
   (->> (get-in data [:closed-rentals :edges])
        (map :node)
        not-empty)))

;; UI

(defn order-line [rental]
  (let
   [title (or (:title rental) (:purpose rental))
    href (routing/path-for ::routes/rentals-show :rental-id (:id rental))
    summary-text (rentals/rental-summary-text rental)]

    [:<>
     [:> UI/Components.Design.ListCard {:href href}
      [:div.d-md-flex
       [:div.pe-4 {:style {:flex "1 1 53%"}}
        [:> UI/Components.Design.ListCard.Title
         [:a.stretched-link {:href href}
          title]]

        [:> UI/Components.Design.ListCard.Body
         summary-text]]

       [:div {:style {:flex "1 1 47%"}}
        [:> UI/Components.Design.ListCard.Foot {:class "p-md-0 pe-md-3"}
         [status-summary rental true]]]]]]))

(defn orders-list [orders]
  [:> UI/Components.Design.Stack {:divided "bottom"}
   (doall
    (for [order orders]
      [:<> {:key (:id order)}
       [order-line order]]))])

(defn view []
  (let [errors @(subscribe [::errors])
        loading? @(subscribe [::loading?])
        open-rentals @(subscribe [::open-rentals])
        closed-rentals @(subscribe [::closed-rentals])
        filters @(subscribe [::query-params])]
    [:> UI/Components.Design.PageLayout.ContentContainer
     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)}
      [order-filter/filter-comp
       filters
       #(dispatch [:routing/navigate
                   [::routes/rentals-index {:query-params %}]])]]
     (cond
       loading? [ui/loading]

       errors [ui/error-view errors]

       (and (empty? open-rentals) (empty? closed-rentals))
       [:div.text-center
        (if (empty? filters) (t :no-orders-yet) (t :no-orders-found))]

       :else
       [:<>
        [:> UI/Components.ReactBootstrap.Tabs
         {:class "mb-2"
          :active-key (or (:tab filters) "reservations")
          :on-select #(dispatch [:routing/navigate
                                 [::routes/rentals-index {:query-params (assoc filters :tab %)}]])}
         [:> UI/Components.ReactBootstrap.Tab {:event-key "reservations" :title "Aktuelle Ausleihen"}
          "TODO"]
         [:> UI/Components.ReactBootstrap.Tab {:event-key "open-orders" :title (t :section-title-open-rentals)}
          (when-not (empty? open-rentals)
            [orders-list open-rentals])]
         [:> UI/Components.ReactBootstrap.Tab {:event-key "closed-orders" :title (t :section-title-closed-rentals)}
          (when-not (empty? closed-rentals)
            [orders-list closed-rentals])]]
        [:> UI/Components.Design.Stack {:space 5}]])]))
