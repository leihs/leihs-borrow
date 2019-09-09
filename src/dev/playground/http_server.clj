(ns playground.http-server
  (:require [org.httpkit.server :as httpkit]
            [clojure.tools.logging :as logging]))

(defonce _server (atom nil))

(defn stop []
  (when-let [server @_server]
    (logging/info "Closing http server.")
    (server)
    (reset! _server nil)))

(defn start [conf main-handler]
  "Starts (or stops and then starts) the webserver"
  (let [server-conf (select-keys conf [:port])]
    (stop)
    (logging/info "starting server " server-conf)
    (reset! _server (httpkit/run-server main-handler server-conf)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop))))
