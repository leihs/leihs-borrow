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

(defn subscription [channel id query-string]
  (let [[prepared-query errors] (try
                                  [(-> schema
                                       (parser/parse-query query-string)
                                       (parser/prepare-with-query-variables {:msg "test"}))]
                                  (catch ExceptionInfo e
                                    [nil e]))

        source-stream-fn (fn [data]
                           (println "sending" data)
                           (->> {:type :data, :id id, :payload {:data data}}
                                json/generate-string
                                (send! channel)))]
    (if (some? errors)
      (->> errors ex-data str (send! channel))
      (let [cleanup-fn (executor/invoke-streamer
                         {constants/parsed-query-key prepared-query}
                         source-stream-fn)]
        (println "call cleanup-fn return" (cleanup-fn))))))

(defn graphql-ws-handler [request]
  (with-subproto-channel
    request channel #".*" "graphql-ws"
    (println "New WebSocket channel:" channel)
    (on-receive channel
                (fn [data]
                  (let [{:keys [id payload type] :as data-map} (json/parse-string data keyword)]
                    (log/info data-map)
                    (case type
                      "connection_init"
                      (send! channel (json/generate-string {:type "connection_ack"}))

                      "start"
                      (subscription channel id (:query payload))

                      "stop"
                      (prn "todo")

                      "connection_terminate"
                      (prn :todo)

                      ;; Not recognized!
                      (prn type)))))

    (on-close channel
              ;; todo: cleanup subscritions
              (fn [status] (println "on-close:" status)))))
