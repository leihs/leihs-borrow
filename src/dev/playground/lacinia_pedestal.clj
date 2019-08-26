(ns playground.lacinia-pedestal
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [leihs.borrow.graphql :as borrow-graphql]
            [com.walmartlabs.lacinia.pedestal.subscriptions :as subscriptions]
            [com.walmartlabs.lacinia.pedestal :as lacinia-pedestal]
            [com.walmartlabs.lacinia.schema :as schema]
            [io.pedestal.http.ring-middlewares :as ring-middlewares]
            [leihs.core.ds :as ds]
            [clojure.tools.logging :as log]))

(def schema (borrow-graphql/load-schema))

(def add-tx
  {:name ::add-tx
   :enter #(assoc-in % [:request :tx] (ds/get-ds))})

(def options {:graphiql true,
              :subscriptions true})

(def interceptors
  (let [defaults (lacinia-pedestal/default-interceptors schema options)]
    (-> defaults
        (lacinia-pedestal/inject ring-middlewares/cookies :before ::lacinia-pedestal/inject-app-context)
        ; Though not intuitive, it must be inserted BEFORE. See source code
        ; of inject-app-context-interceptor.
        (lacinia-pedestal/inject add-tx :before ::lacinia-pedestal/inject-app-context))))

(def subscription-interceptors
  (let [defaults (subscriptions/default-subscription-interceptors schema options)]
    (-> defaults
        (lacinia-pedestal/inject ring-middlewares/cookies :before ::subscriptions/inject-app-context)
        (lacinia-pedestal/inject add-tx :before ::subscriptions/inject-app-context)
        ; https://github.com/walmartlabs/lacinia-pedestal/issues/89
        (lacinia-pedestal/inject {:enter #(assoc-in % [:request :lacinia-app-context :request] (:request %))}
                                 :after ::subscriptions/inject-app-context))))

(def service
  (lacinia-pedestal/service-map schema
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
