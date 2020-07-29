(ns leihs.borrow.features.customer-orders.index
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    #_[reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.client.routes :as routes]
    #_[leihs.borrow.components :as ui]))


; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/orders-index
  (fn-traced [_ [_ _]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/customer_orders/customerOrdersIndex.gql")
                {}
                [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ {:keys [data errors]}]]
    (-> db
        (cond-> errors (assoc ::errors errors))
        (assoc ::data data))))

(reg-sub ::data (fn [db _] (::data db)))

(reg-sub ::errors (fn [db _] (::errors db)))

(reg-sub 
  ::submitted-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:submitted-orders :edges])
         (map :node) 
         not-empty)))

(reg-sub 
  ::rejected-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:rejected-orders :edges])
         (map :node)
         not-empty)))

(reg-sub 
  ::approved-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:approved-orders :edges])
         (map :node)
         not-empty)))

(defn order-line [order]
  (let
    [label (:purpose order)
     href (routing/path-for ::routes/orders-show :order-id (:id order))]
    [:<>
     [:a {:href href} 
      label " "
      [:span.text-color-muted (ui/format-date :short (:created-at order))]]]))

(defn orders-list [orders]
  [:ul (doall (for [order orders] [:li {:key (:id order)} [order-line order]]))])

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])
        is-loading? (not (or data errors))
        submitted-orders @(subscribe [::submitted-orders])
        rejected-orders @(subscribe [::rejected-orders])
        approved-orders @(subscribe [::approved-orders])]

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
