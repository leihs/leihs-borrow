(ns leihs.borrow.features.customer-orders.index
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    #_[reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.client.routes :as routes]
    #_[leihs.borrow.components :as ui]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
  ::routes/orders-index
  (fn [_ [_ _]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/customer_orders/customerOrdersIndex.gql")
                {}
                [::on-fetched-data]]}))

(rf/reg-event-db
  ::on-fetched-data
  (fn [db [_ {:keys [data errors]}]]
    (-> db
        (cond-> errors (assoc ::errors errors))
        (assoc ::data data))))

(rf/reg-sub ::data (fn [db _] (::data db)))

(rf/reg-sub ::errors (fn [db _] (::errors db)))

(rf/reg-sub 
  ::submitted-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:submittedOrders :edges])
         (map :node) 
         not-empty)))

(rf/reg-sub 
  ::rejected-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:rejectedOrders :edges])
         (map :node)
         not-empty)))

(rf/reg-sub 
  ::approved-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:approvedOrders :edges])
         (map :node)
         not-empty)))

(defn order-line [order]
  (let
    [label (:purpose order)
     href (routing/path-for ::routes/orders-show :order-id (:id order))]
    [:<>
     [:a {:href href} 
      label " "
      [:span.text-color-muted (ui/format-date :short (:createdAt order))]]]))

(defn orders-list [orders]
  [:ul (doall (for [order orders] [:li {:key (:id order)} [order-line order]]))])

(defn view []
  (let [data @(rf/subscribe [::data])
        errors @(rf/subscribe [::errors])
        is-loading? (not (or data errors))
        submitted-orders @(rf/subscribe [::submitted-orders])
        rejected-orders @(rf/subscribe [::rejected-orders])
        approved-orders @(rf/subscribe [::approved-orders])]

    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header.mb-3
         [:h1.text-3xl.font-extrabold.leading-none "Orders"]]

        (when-not (empty? submitted-orders)
          [:div.mt-3
           [:h2.text-xl.font-semibold "Active Orders"]
           [orders-list submitted-orders ""]])

        (when-not (empty? rejected-orders)
          [:div.mt-3
           [:h2.text-xl.font-semibold "Rejected Orders"]
           [orders-list rejected-orders]])

        (when-not (empty? approved-orders)
          [:div.mt-3
           [:h2.text-xl.font-semibold "Approved Orders"]
           [orders-list approved-orders ""]])

        #_[:pre {:style {:white-space :pre-wrap}} (pr-str rejected-orders)]

        #_[:pre {:style {:white-space :pre-wrap}} (pr-str approved-orders)]

        #_[:p.debug (pr-str data)]])]))
