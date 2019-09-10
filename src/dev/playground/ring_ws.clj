(ns playground.ring-ws
  (:require [cheshire.core :as json]
            [com.walmartlabs.lacinia :as ql]
            [com.walmartlabs.lacinia.constants :as constants]
            [com.walmartlabs.lacinia.executor :as executor]
            [com.walmartlabs.lacinia.parser :as parser]
            [compojure.core :refer [GET POST defroutes]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.request :refer [body-string]]
            [ring.util.response :as resp :refer [response]]
            [org.httpkit.server :refer [run-server with-channel on-close on-receive send!]]
            [try-lacinia.ws-subprotocol :refer [with-subproto-channel]]
            [leihs.borrow.graphql :refer [schema]]
            [clojure.tools.logging :as log])
  (:import (clojure.lang ExceptionInfo)))

(defn subscription [channel id {:keys [query variables]} request]
  (let [[prepared-query errors] (try
                                  [(-> schema
                                       (parser/parse-query query)
                                       (parser/prepare-with-query-variables variables))]
                                  (catch ExceptionInfo e
                                    [nil e]))

        source-stream-fn (fn [data]
                           (println "sending" data)
                           (->> {:type :data, :id id, :payload {:data data}}
                                json/generate-string
                                (send! channel)))]
    (if (some? errors)
      (->> errors ex-data str (send! channel))
      (let [cleanup-fn
            (executor/invoke-streamer
              {constants/parsed-query-key prepared-query, :request request}
              source-stream-fn)]
        (println "call cleanup-fn return" (cleanup-fn))))))

(defn graphql-ws-handler [request]
  (with-subproto-channel
    request channel #".*" "graphql-ws"
    (println "New WebSocket channel:" channel)
    (on-receive channel
                (fn [data]
                  (let [{:keys [id payload type]}
                        (json/parse-string data keyword)]
                    (case type
                      "connection_init" (->> {:type "connection_ack"}
                                             json/generate-string
                                             (send! channel))

                      "start" (subscription channel id payload request)

                      "stop" (prn "todo")

                      "connection_terminate" (prn :todo)

                      ;; Not recognized!
                      (prn type)))))

    (on-close channel
              ;; todo: cleanup subscritions
              (fn [status] (println "on-close:" status)))))
