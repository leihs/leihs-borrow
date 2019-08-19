(ns playground.lacinia-pedestal
  (:require [io.pedestal.http :as http]
            [com.walmartlabs.lacinia.pedestal :as lacinia]
            [com.walmartlabs.lacinia.schema :as schema]
            [clojure.tools.logging :as log]))

(def hello-schema
  (schema/compile
    {:queries {:hello
               ;; String is quoted here; in EDN the quotation is not required
               {:type 'String
                :resolve (constantly "world")}}}))

(def service
  (lacinia/service-map hello-schema {:graphiql true}))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(def runnable-service (atom nil))

(defn start []
  (log/info "starting lacinia-pedestal service on port 8888")
  (reset! runnable-service (http/create-server service))
  (http/start @runnable-service))

(defn stop []
  (when @runnable-service
    (log/info "stopping lacinia-pedestal service")
    (http/stop @runnable-service)
    (reset! runnable-service nil)))
