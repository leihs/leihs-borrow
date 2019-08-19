(ns playground.regraph-example
  (:require [re-graph.core :as re-graph]
            [re-frame.core :as re-frame]))

;; initialise re-graph, possibly including configuration options (see below)
(re-frame/dispatch [::re-graph/init {:ws-url "ws://localhost:8888/graphql-ws"
                                     :http-url "http://localhost:8888/graphql"}])

(re-frame/reg-event-db
  ::on-thing
  (fn [db [_ {:keys [data errors] :as payload}]]
    ;; do things with data e.g. write it into the re-frame database
    ))

;; start a subscription, with responses sent to the callback event provided
; (re-frame/dispatch [::re-graph/subscribe
;                     :my-subscription-id  ;; this id should uniquely identify this subscription
;                     "{ hello }"          ;; your graphql query
;                     ; {:some "variable"} ;; arguments map
;                     [::on-thing]])       ;; callback event when messages are recieved

;; stop the subscription
; (re-frame/dispatch [::re-graph/unsubscribe :my-subscription-id])

;; perform a query, with the response sent to the callback event provided
(re-frame/dispatch [::re-graph/query
                    "{ hello }"          ;; your graphql query
                    ; {:some "variable"} ;; arguments map
                    [::on-thing]])       ;; callback event when response is recieved
