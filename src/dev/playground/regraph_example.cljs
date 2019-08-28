(ns playground.regraph-example
  (:require [cljs-http.client :as http]
            [reagent.core :as reagent]
            [re-graph.core :as re-graph]
            [re-frame.core :as re-frame]))

;; initialise re-graph, possibly including configuration options (see below)
(re-frame/dispatch
  [::re-graph/init {:ws-url "ws://localhost:8888/graphql-ws"
                    :http-url "http://localhost:8888/graphql"}])

(re-frame/reg-event-db
  ::on-thing
  (fn [_ [_ payload]]
    {::result payload}))

;; perform a query, with the response sent to the callback event provided
(defn dispatch-query []
  (re-frame/dispatch [::re-graph/query
                      "{ users(search_term: \"Kmit\", limit: 1) { firstname lastname }}"         ;; your graphql query
                      {}                   ;; arguments map
                      [::on-thing]]))      ;; callback event when response is recieved

(defn dispatch-subscribe []
  (re-frame/dispatch [::re-graph/subscribe
                      :calendar  ;; this id should uniquely identify this subscription
                      "{
                        calendar(
                          model_id: \"c9c1f4d4-0814-52fb-a804-bf78c0f554ad\", 
                          inventory_pool_id: \"8bd16d45-056d-5590-bc7f-12849f034351\", 
                          start_date: \"2019-08-26\", 
                          end_date: \"2019-09-08\") {
                          list {
                            d
                            quantity
                            visits_count
                          }
                        }
                      }"
                      {} ;; arguments map
                      [::on-thing]])       ;; callback event when messages are recieved
  )

(defn dispatch-unsubscribe []
  (re-frame/dispatch [::re-graph/unsubscribe :calendar]))

(re-frame/reg-sub
  ::result
  (fn [db _]
    (-> db
        ::result
        clj->js
        js/JSON.stringify)))

(defn ui
  []
  [:div
   [:h1 "Re-graph example"]
   [:span
    [:button {:on-click dispatch-subscribe} "subscribe"]
    [:button {:on-click dispatch-unsubscribe} "unsubscribe"]]
   [:br]
   [:br]
   [:div @(re-frame/subscribe [::result])]])

(defn ^:export run
  []
  (reagent/render [ui] (js/document.getElementById "app")))
