(ns leihs.borrow.authenticate
  (:require [clojure.tools.logging :as log]
            [leihs.core.graphql.helpers :as helpers]))

(def skip-authorization-handler-keys
  [:home
   ;;; DEV
   :reagent-example
   :reframe-example
   :regraph-example
   :shadow-example
   :regraph-ring
   :graphql-ws
   ;;; DEV
   :sign-in])

(defn- skip?
  [handler-key]
  (some #(= handler-key %) skip-authorization-handler-keys))

(defn wrap-ensure-authenticated-entity
  [handler]
  (fn [request]
    (if (or (skip? (:handler-key request)) (:authenticated-entity request))
      (handler request)
      {:status 401,
       :body (helpers/error-as-graphql-object "NOT_AUTHENTICATED"
                                              "Not authenticated!")})))
