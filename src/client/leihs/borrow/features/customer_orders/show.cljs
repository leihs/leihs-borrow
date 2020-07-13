(ns leihs.borrow.features.customer-orders.show
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
 ::routes/orders-show
 (fn [_ [_ args]]
   (let [order-id (get-in args [:route-params :order-id])]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/features/customer_orders/customerOrderShow.gql")
                 {:id order-id}
                 [::on-fetched-data order-id]]})))

(rf/reg-event-db
 ::on-fetched-data
 (fn [db [_ order-id {:keys [data errors]}]]
   (-> db
       (update-in , [::data order-id ] (fnil identity {}))
       (cond-> errors (assoc-in , [::errors order-id] errors))
       (assoc-in , [::data order-id] (:order data)))))

(rf/reg-sub ::data
            (fn [db [_ id]] (get-in db [::data id])))

(rf/reg-sub ::errors
            (fn [db _] (::errors db)))

(defn reservation-line [quantity line]
  (let
   [model (:model line)
    img (get-in model [:images 0])
    pool (get-in line [:inventoryPool :name])]
    [:div.flex.flex-row.border-b.mb-2.pb-2.-mx-1
     [:div.px-1.flex-none {:class "w-1/4"} [ui/image-square-thumb img]]
     [:div.px-1.flex-1
      [:div.font-semibold (:name model)]
      [:div.text-sm
       (ui/format-date :short (get-in line [:startDate]))
       (str ui/thin-space "–" ui/thin-space)
       (ui/format-date :short (get-in line [:endDate]))]
      [:div.text-sm.text-color-muted
       [:span quantity " Items"]
       [:span " • "]
       [:span pool]]]
     #_[:div.px-1.self-center.flex-none
      [:button.text-sm
       {:on-click #(js/alert "TODO")}
       ui/trash-icon]]]))

(defn view []
  (let [routing @(rf/subscribe [:routing/routing])
        order-id (get-in routing [:bidi-match :route-params :order-id])
        order @(rf/subscribe [::data order-id])
        errors @(rf/subscribe [::errors order-id])
        is-loading? (not (or order errors))]

    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header.mb-3
         [:h1.text-3xl.font-extrabold.leading-tight 
          "Order “" (:purpose order) "”"]
         [:p "from: " (ui/format-date :short (:createdAt order))]
         [:p.mt-2.text-color-muted.text-sm [:a {:href (routing/path-for ::routes/orders-index)} "← all Orders"]]]

        #_[:pre "?" (get-in order [:subOrdersByPool])]

        #_(doall
            (for [suborder (get-in order [:subOrdersByPool])]
              [:div "suborder"
               [:p (pr-str suborder)]
               (doall
                 (for [r (:reservations suborder)]
                   [reservation-line r]))]))

        [:h2.font-bold.text-xl [:mark "Order Data"]]

        [:pre.text-xs {:style {:white-space :pre-wrap}} (js/JSON.stringify (clj->js order) 0 2)]

        #_[:p]])]))
