(ns playground.regraph-example
  (:require [cljs-http.client :as http]
            [reagent.core :as reagent]
            [reagent.cookies :as cookies]
            [re-graph.core :as re-graph]
            [re-frame.core :as re-frame]))

(defn ^:export run []

  (def query "Calendar(
                $model_id: String!,
                $inventory_pool_id: String!,
                $start_date: String!,
                $end_date: String!
              ) {
                calendar(
                  model_id: $model_id, 
                  inventory_pool_id: $inventory_pool_id,
                  start_date: $start_date,
                  end_date: $end_date
                ) {
                  list {
                    d
                    quantity
                    visits_count
                  }
                }
              }")

  (re-frame/dispatch
    [::re-graph/init
     {:ws-url "ws://localhost:8888/graphql-ws"
      :http-url "http://localhost:8888/graphql"
      :connection-init-payload
      {:cookies {:leihs-user-session (cookies/get-raw :leihs-user-session)}}}])

  (re-frame/reg-event-db
    ::on-message
    [(when ^boolean goog.DEBUG re-frame/debug)]
    (fn [_ [_ payload]]
      {::result payload}))

  (re-frame/reg-sub
    ::query-result
    (fn [db _]
      (let [res  (-> db ::result clj->js)]
        (if res
          (js/JSON.stringify res)
          "Please subscribe!"))))

  (defn dispatch-subscribe []
    (re-frame/dispatch [::re-graph/subscribe
                        :calendar 
                        query
                        {:model_id "c9c1f4d4-0814-52fb-a804-bf78c0f554ad",
                         :inventory_pool_id "8bd16d45-056d-5590-bc7f-12849f034351",
                         :start_date "2019-09-02",
                         :end_date "2019-09-02"}
                        [::on-message]]))

  (defn dispatch-unsubscribe []
    (re-frame/dispatch [::re-graph/unsubscribe :calendar]))

  (defn ui
    []
    [:div
     [:h1 "Re-graph example"]
     [:span
      [:button {:on-click dispatch-subscribe} "subscribe"]
      [:button {:on-click dispatch-unsubscribe} "unsubscribe"]]
     [:br]
     [:br]
     [:div @(re-frame/subscribe [::query-result])]])

  (reagent/render [ui] (js/document.getElementById "app")))
