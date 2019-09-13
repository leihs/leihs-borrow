(ns playground.http-server
  (:require [org.httpkit.server :as httpkit]
            [clojure.tools.logging :as log]))

(defonce _server (atom nil))

(defn stop []
  (when-let [server @_server]
    (log/info "Closing http server.")
    (server)
    (reset! _server nil)))

(defn start [conf main-handler]
  "Starts (or stops and then starts) the webserver"
  (let [server-conf (-> conf
                        (select-keys [:port])
                        (update :port inc))]
    (stop)
    (log/info "starting server " server-conf)
    (reset! _server (httpkit/run-server main-handler server-conf)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop))))
