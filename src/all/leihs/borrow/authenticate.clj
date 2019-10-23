(ns leihs.borrow.authenticate
  (:require [clojure.tools.logging :as log]
            [leihs.core.graphql.helpers :as helpers]))

(def skip-authorization-handler-keys
  #{:home
    :graphql ; authenticates by its own
    :sign-in
    :status
    :image
    :attachment})

(defn- skip? [handler-key]
  (some #(= handler-key %) skip-authorization-handler-keys))

(defn wrap-base [handler]
  (fn [request]
    (if (:authenticated-entity request)
      (handler request)
      {:status 401,
       :body (helpers/error-as-graphql-object "NOT_AUTHENTICATED"
                                              "Not authenticated!")})))

(defn wrap [handler]
  (fn [request]
    (if (skip? (:handler-key request))
      (handler request)
      ((wrap-base handler) request))))
