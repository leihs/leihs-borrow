(ns playground.lacinia-pedestal
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [leihs.borrow.graphql :as borrow-graphql]
            [com.walmartlabs.lacinia.pedestal :as lacinia]
            [com.walmartlabs.lacinia.schema :as schema]
            [leihs.core.ds :as ds]
            [clojure.tools.logging :as log]))

(def schema (borrow-graphql/load-schema))

(def add-tx
  {:name ::add-tx
   :enter #(assoc-in % [:request :tx] (ds/get-ds))})

(def options {:graphiql true,
              :subscriptions true})

(def interceptors
  (let [defaults (lacinia/default-interceptors schema options)]
    (-> defaults
        ; Though not intuitive, it must be inserted BEFORE. See source code
        ; of inject-app-context-interceptor.
        (lacinia/inject add-tx :before ::lacinia/inject-app-context))))

(def service
  (lacinia/service-map schema
                       (merge options {:interceptors interceptors})))

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

(comment
  (-> @runnable-service
      :io.pedestal.http/routes
      #_first
      #_(nth 2)))
