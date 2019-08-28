(ns playground.lacinia-pedestal
  (:refer-clojure :exclude [subs])
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [leihs.borrow.graphql :as borrow-graphql]
            [com.walmartlabs.lacinia.pedestal.subscriptions :as subs]
            [com.walmartlabs.lacinia.pedestal :as lac-ped]
            [com.walmartlabs.lacinia.schema :as schema]
            [io.pedestal.http.ring-middlewares :as ring-middle]
            [leihs.core.ds :as ds]
            [clojure.tools.logging :as log]))

(def schema (borrow-graphql/load-schema))

(def add-tx
  {:name ::add-tx
   :enter #(assoc-in % [:request :tx] (ds/get-ds))})

(def options {:graphiql true,
              :subscriptions true})

(def interceptors
  (let [defaults (lac-ped/default-interceptors schema options)]
    (-> defaults
        (lac-ped/inject ring-middle/cookies :before ::lac-ped/inject-app-context)
        ; Though not intuitive, it must be inserted BEFORE. See source code
        ; of inject-app-context-interceptor.
        (lac-ped/inject add-tx :before ::lac-ped/inject-app-context))))

(def subscription-interceptors
  (let [defaults (subs/default-subscription-interceptors schema options)]
    (-> defaults
        (lac-ped/inject ring-middle/cookies :before ::subs/inject-app-context)
        (lac-ped/inject add-tx :before ::subs/inject-app-context)
        ; https://github.com/walmartlabs/lacinia-pedestal/issues/89
        (lac-ped/inject {:enter #(assoc-in % [:request :lacinia-app-context :request] (:request %))}
                                 :after ::subs/inject-app-context))))

(def service
  (lac-ped/service-map schema
                                (merge options {:interceptors interceptors
                                                :subscription-interceptors subscription-interceptors})))

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
  (->> @runnable-service
       :io.pedestal.http/routes
       ; (into [])
       ; (map first)
       ; first
       ; (nth 2)
       ))
