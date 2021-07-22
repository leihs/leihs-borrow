(ns leihs.borrow.features.customer-orders.show
  (:require
    ["date-fns" :as datefn]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    #_[reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.helpers :refer [spy log]]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.client.routes :as routes]
    ["/leihs-ui-client-side-external-react" :as UI]))


; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/rentals-show
  (fn-traced [{:keys [db]} [_ args]]
    (let [order-id (get-in args [:route-params :rental-id])]
      {:dispatch [::re-graph/query
                  (rc/inline "leihs/borrow/features/customer_orders/customerOrderShow.gql")
                  {:id order-id, :userId (filters/user-id db)}
                  [::on-fetched-data order-id]]})))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ order-id {:keys [data errors]}]]
    (-> db
        (update-in , [::data order-id ] (fnil identity {}))
        (cond-> errors (assoc-in , [::errors order-id] errors))
        (assoc-in , [::data order-id] (:rental data)))))

(reg-sub ::data
         (fn [db [_ id]] (get-in db [::data id])))

(reg-sub ::errors
         (fn [db [_ id]] (get-in db [::errors id])))

(reg-sub ::total-days
         (fn [[_ id] _] (rf/subscribe [::data id]))
         (fn [d _] (datefn/differenceInDays
                     (datefn/parseISO (:until-date d))
                     (datefn/parseISO (:from-date d)))))

(defn reservation-line [quantity line]
  (let
    [model (:model line)
     img (get-in model [:images 0])
     pool (get-in line [:inventory-pool :name])]
    [:div.flex.flex-row.border-b.mb-2.pb-2.-mx-1
     [:div.px-1.flex-none {:class "w-1/4"} [ui/image-square-thumb img]]
     [:div.px-1.flex-1
      [:div.font-semibold (:name model)]
      [:div.text-sm
       (ui/format-date :short (get-in line [:start-date]))
       (str ui/thin-space "–" ui/thin-space)
       (ui/format-date :short (get-in line [:end-date]))]
      [:div.text-sm.text-color-muted
       [:span quantity " Items"]
       [:span " • "]
       [:span pool]]]
     #_[:div.px-1.self-center.flex-none
        [:button.text-sm
         {:on-click #(js/alert "TODO")}
         ui/trash-icon]]]))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        order-id (get-in routing [:bidi-match :route-params :rental-id])
        order @(subscribe [::data order-id])
        total-days @(subscribe [::total-days order-id])
        errors @(subscribe [::errors order-id])
        is-loading? (not (or order errors))]

    [:> UI/Components.AppLayout.Page
     {:title (if order (str "Order “" (:purpose order) "”") "…")
      :subTitle (if order (str "from: " (ui/format-date :short (:created-at order))) "…")
      :backLink {:href (routing/path-for ::routes/rentals-index) :children "All Orders"}}
     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>

        #_[:pre "?" (get-in order [:sub-orders-by-pool])]

        #_(doall
            (for [suborder (get-in order [:sub-orders-by-pool])]
              [:div "suborder"
               [:p (pr-str suborder)]
               (doall
                 (for [r (:reservations suborder)]
                   [reservation-line r]))]))

        [:h2.font-bold.text-xl [:mark "Order Data"]]
        [:small.d-block.mt-2.text-muted.text-base total-days]

        [:pre.text-xs {:style {:white-space :pre-wrap}} (js/JSON.stringify (clj->js order) 0 2)]

        #_[:p]])]))
