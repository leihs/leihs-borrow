(ns leihs.borrow.features.customer-orders.index
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
       (update-in , [:orders] (fnil identity {}))
       (assoc-in , [:orders :errors] errors)
       (assoc-in , [:orders :data] data))))

(rf/reg-sub ::orders (fn [db] (get-in db [:orders])))

(rf/reg-sub 
 ::submitted-orders 
 (fn [] [(rf/subscribe [::orders])])
 (fn [[orders]] (->> (get-in orders [:data :submittedOrders :edges]) (map :node) not-empty)))
(rf/reg-sub 
 ::rejected-orders 
 (fn [] [(rf/subscribe [::orders])])
 (fn [[orders]] (->> (get-in orders [:data :rejectedOrders :edges]) (map :node) not-empty)))
(rf/reg-sub 
 ::approved-orders 
 (fn [] [(rf/subscribe [::orders])])
 (fn [[orders]] (->> (get-in orders [:data :approvedOrders :edges]) (map :node) not-empty)))

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
  (fn []
    (let
     [fetched @(rf/subscribe [::orders])
      orders (get-in fetched [:data])
      errors (:errors fetched)
      is-loading? (not (or orders errors))
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


            #_[:p.debug (pr-str model)]])])))
