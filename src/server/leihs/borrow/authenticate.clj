(ns leihs.borrow.authenticate
  (:require [clojure.tools.logging :as log]
            [leihs.core.core :refer [presence]]
            [leihs.core.graphql.helpers :as helpers]
            [leihs.core.sign-in.external-authentication.back :as ext-auth]
            [leihs.borrow.paths :refer [path]]
            [ring.util.response :as response]))

(def skip-authorization-handler-keys
  (clojure.set/union #{:attachment
                       :graphql ; authenticates by its own
                       :home
                       :image
                       :sign-in
                       :status}
                     ext-auth/skip-authorization-handler-keys))

(defn- skip? [handler-key]
  (some #(= handler-key %) skip-authorization-handler-keys))

(defn wrap-base [handler]
  (fn [{:keys [uri query-string] :as request}]
    (if (:authenticated-entity request)
      (handler request)
      (response/redirect
       (path :sign-in
             nil
             {:return-to (cond-> uri
                           (presence query-string)
                           (str "?" query-string))})))))

(defn wrap [handler]
  (fn [request]
    (if (skip? (:handler-key request))
      (handler request)
      ((wrap-base handler) request))))
