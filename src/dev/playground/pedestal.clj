(ns playground.pedestal
  (:require [io.pedestal.http :as http]
            [leihs.core.status :as status]
            [clojure.tools.logging :as log]))

(def routes #{["/borrow/status" :get `status/status-handler]})

(def service-map {::http/port 9999,
                  ::http/routes routes,
                  ::http/type :jetty})

(def service (atom nil))

(defn start []
  (log/info "starting pedestal service on port 9999")
  (reset! service (http/create-server service-map))
  (http/start @service))

(defn stop []
  (when @service
    (log/info "stopping pedestal service")
    (http/stop @service)
    (reset! service nil)))
