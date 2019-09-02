(ns playground.lacinia-pedestal
  (:refer-clojure :exclude [set])
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [leihs.borrow.graphql :as borrow-graphql]
            [leihs.borrow.html :as html]
            [com.walmartlabs.lacinia.pedestal.subscriptions :as lac-ped-subs]
            [com.walmartlabs.lacinia.pedestal :as lac-ped]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia :as lac]
            [io.pedestal.http.ring-middlewares :as ring-middle]
            [leihs.core.ds :as ds]
            [playground.pedestal :as play-ped]
            [playground.pedestal.interceptors :as play-ped-int]
            [clojure.tools.logging :as log]
            [clojure.set :as set]))

(def schema (borrow-graphql/load-schema))

(def options {:graphiql true,
              :subscriptions true})

(def routes (set/union (lac-ped/graphql-routes schema options)
                       play-ped/routes))

(def add-tx-to-request
  {:name ::add-tx-to-request
   :enter #(assoc-in % [:request :tx] (ds/get-ds))})

(def interceptors
  (let [defaults (lac-ped/default-interceptors schema options)]
    (-> defaults
        (lac-ped/inject ring-middle/cookies
                        :before
                        ::lac-ped/inject-app-context)
        ; Though not intuitive, it must be inserted BEFORE. See source code
        ; of inject-app-context-interceptor.
        (lac-ped/inject add-tx-to-request
                        :before
                        ::lac-ped/inject-app-context))))

(def add-request-to-lacinia-app-context
  {:name ::add-request-to-lacinia-app-context
   :enter #(assoc-in %
                     [:request :lacinia-app-context :request]
                     (:request %))})

(def add-connection-params-to-lacinia-app-context
  {:name ::add-connection-params-to-request
   :enter #(assoc-in %
                     [:request :lacinia-app-context :connection-params]
                     (:connection-params %))})

(def subscription-interceptors
  (let [defaults (lac-ped-subs/default-subscription-interceptors schema options)]
    (-> defaults
        (lac-ped/inject ring-middle/cookies
                        :before
                        ::lac-ped-subs/inject-app-context)
        (lac-ped/inject add-tx-to-request
                        :before
                        ::lac-ped-subs/inject-app-context)
        (lac-ped/inject add-connection-params-to-lacinia-app-context
                        :after
                        ::lac-ped-subs/inject-app-context)
        ; https://github.com/walmartlabs/lacinia-pedestal/issues/89
        (lac-ped/inject add-request-to-lacinia-app-context
                        :after
                        ::lac-ped-subs/inject-app-context))))

(def service
  (as-> options <>
    (merge <> {:routes routes
               :interceptors interceptors
               :subscription-interceptors subscription-interceptors})
    (lac-ped/service-map schema <>)
    (assoc <> ::http/not-found-interceptor play-ped-int/not-found)))

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
