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
  (fn [_ [_ {:keys [data errors] :as payload}]]
    {::result data}))

;; perform a query, with the response sent to the callback event provided
(defn dispatch-query []
  (re-frame/dispatch [::re-graph/query
                      "{ hello }"          ;; your graphql query
                      {}                   ;; arguments map
                      [::on-thing]]))      ;; callback event when response is recieved

(re-frame/reg-sub
  ::result
  (fn [db _]
    (-> db ::result :data)))

(defn ui
  []
  [:div
   [:h1 "Re-graph example"]
   [:button {:on-click dispatch-query} "query"]
   [:div @(re-frame/subscribe [::result])]])

(defn ^:export run
  []
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
