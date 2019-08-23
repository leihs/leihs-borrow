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
   [:div @(re-frame/subscribe [::result])]])

(defn ^:export run
  []
  (dispatch-query)
  (reagent/render [ui] (js/document.getElementById "app")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;; SUBSCRIPTIONS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; start a subscription, with responses sent to the callback event provided
; (re-frame/dispatch [::re-graph/subscribe
;                     :my-subscription-id  ;; this id should uniquely identify this subscription
;                     "{ hello }"          ;; your graphql query
;                     ; {:some "variable"} ;; arguments map
;                     [::on-thing]])       ;; callback event when messages are recieved

;; stop the subscription
; (re-frame/dispatch [::re-graph/unsubscribe :my-subscription-id])
