(ns playground.regraph-ring-full
  (:refer-clojure :exclude [update])
  (:require [cljs-http.client :as http]
            [reagent.core :as reagent]
            [reagent.cookies :as cookies]
            [re-graph.core :as re-graph]
            [re-graph.internals :as re-graph-internals]
            [re-frame.core :as re-frame]))

(defn setup []
  (def query "Calendar($model_id: String!, $inventory_pool_id: String!, $start_date: String!, $end_date: String!) {
                calendar(model_id: $model_id, inventory_pool_id: $inventory_pool_id, start_date: $start_date, end_date: $end_date) {
                  list {
                    d
                    quantity
                    visits_count
                  }
                }
              }")

 (defn dispatch-unsubscribe []
   (re-frame/dispatch [::re-graph/unsubscribe ::calendar]))

 (re-frame/dispatch
   [::re-graph/init
    {:ws-url "ws://localhost:3250/graphql-ws-2"
     :http-url "http://localhost:3250/graphql"
     :connection-init-payload
     {:cookies {:leihs-user-session (cookies/get-raw :leihs-user-session)}}}])

 (re-frame/reg-event-db
   ::on-message
   ; [(when ^boolean goog.DEBUG re-frame/debug)]
   (fn [db [_ payload]]
     (assoc db ::result payload)))

 (re-frame/reg-sub ::query-result
                   (fn [db _]
                     (-> db
                         ::result
                         clj->js
                         js/JSON.stringify)))

 (re-frame/reg-sub ::query-model-id
                   (fn [db _] (::model-id db)))

 (re-frame/reg-sub ::query-inventory-pool-id
                   (fn [db _] (::inventory-pool-id db)))

 (re-frame/reg-sub ::query-start-date
                   (fn [db _] (::start-date db)))

 (re-frame/reg-sub ::query-end-date
                   (fn [db _] (::end-date db)))

 (re-frame/reg-sub ::query-subscription-active?
                   ; [(when ^boolean goog.DEBUG re-frame/debug)]
                   (fn [db _]
                     (some-> db
                             :re-graph
                             re-graph-internals/default-instance-name
                             :subscriptions
                             (get "calendar") 
                             :active?)))

 (re-frame/reg-event-db ::update-model-id
                        (fn [db [_ model-id]]
                          (assoc db ::model-id model-id)))

 (re-frame/reg-event-db ::update-inventory-pool-id
                        (fn [db [_ inventory-pool-id]]
                          (assoc db ::inventory-pool-id inventory-pool-id)))

 (re-frame/reg-event-db ::update-start-date
                        (fn [db [_ start-date]]
                          (assoc db ::start-date start-date)))

 (re-frame/reg-event-db ::update-end-date
                        (fn [db [_ end-date]]
                          (assoc db ::end-date end-date)))

 (defn update-model-id [model-id]
   (re-frame/dispatch [::update-model-id model-id]))

 (defn update-inventory-pool-id [ip-id]
   (re-frame/dispatch [::update-inventory-pool-id ip-id]))

 (defn update-start-date [start-date]
   (re-frame/dispatch [::update-start-date start-date]))

 (defn update-end-date [end-date]
   (re-frame/dispatch [::update-end-date end-date]))

 (update-model-id "c9c1f4d4-0814-52fb-a804-bf78c0f554ad")
 (update-inventory-pool-id "8bd16d45-056d-5590-bc7f-12849f034351")
 (update-start-date "2019-09-06")
 (update-end-date "2019-09-06")

 (defn dispatch-subscribe [model-id inventory-pool-id start-date end-date]
   (re-frame/dispatch [::re-graph/subscribe
                       ::calendar 
                       query
                       {:model_id model-id
                        :inventory_pool_id inventory-pool-id
                        :start_date start-date,
                        :end_date end-date}
                       [::on-message]]))

  (defn ui []
    (let [model-id (re-frame/subscribe [::query-model-id])
          inventory-pool-id (re-frame/subscribe [::query-inventory-pool-id])
          start-date (re-frame/subscribe [::query-start-date])
          end-date (re-frame/subscribe [::query-end-date])
          result (re-frame/subscribe [::query-result])
          subscription-active? (re-frame/subscribe [::query-subscription-active?])]
      (letfn [(target-value [ev] (-> ev .-target .-value))]
        [:div
         [:h1 "Re-graph example"]
         [:label {:style {:margin-right "10px"}} "Model ID"]
         [:input {:on-change #(do (dispatch-unsubscribe)
                                (-> % target-value update-model-id))
                  :default-value @model-id,
                  :style {:width "250px"}}]
         [:br]
         [:label {:style {:margin-right "10px"}} "Inventory pool ID"]
         [:input {:on-change #(do (dispatch-unsubscribe)
                                (-> % target-value update-inventory-pool-id))
                  :default-value @inventory-pool-id,
                  :style {:width "250px"}}]
         [:br]
         [:label {:style {:margin-right "10px"}} "Start date"]
         [:input {:on-change #(do (dispatch-unsubscribe)
                                (-> % target-value update-start-date))
                  :default-value @start-date,
                  :style {:width "250px"}}]
         [:br]
         [:label {:style {:margin-right "10px"}} "End date"]
         [:input {:on-change #(do (dispatch-unsubscribe)
                                (-> % target-value update-end-date))
                  :default-value @end-date,
                  :style {:width "250px"}}]
         [:br]
         [:br]
         [:span
          [:button {:on-click #(dispatch-subscribe
                                 @model-id
                                 @inventory-pool-id
                                 @start-date
                                 @end-date)}
           "subscribe"]
          [:button {:on-click dispatch-unsubscribe} "unsubscribe"]]
         [:br]
         [:br]
         [:div (if @subscription-active? @result "Please subscribe!")]]))))

(defn ^:export run []
  (setup)
  (reagent/render [ui] (js/document.getElementById "app")))

(defn ^:export update []
  (re-graph/destroy)
  (setup)
  (reagent/render [ui] (js/document.getElementById "app")))
