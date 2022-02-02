(ns leihs.borrow.graphql.resolvers
  (:refer-clojure :exclude [resolve])
  (:require [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.graphql.mutations :as mutations]
            [leihs.borrow.graphql.queries :as queries]
            [leihs.core.core :refer [spy-with]]
            [leihs.core.graphql.helpers :refer [transform-resolvers
                                                wrap-resolver-with-error
                                                wrap-resolver-with-kebab-case
                                                wrap-resolver-with-camelCase]]
            [leihs.borrow.resources.delegations :as delegations]
            [java-time :refer [local-date before?]])
  (:import (java.time.format DateTimeFormatter)))

(defn wrap-resolver-with-dates-validation [resolver]
  (fn [context {:keys [start-date end-date] :as args} value]
    (if (and start-date
             end-date
             (before?
               (local-date DateTimeFormatter/ISO_LOCAL_DATE end-date)
               (local-date DateTimeFormatter/ISO_LOCAL_DATE start-date)))
      (throw (ex-info "End date cannot be before start date." {}))
      (resolver context args value))))

(defn wrap-resolver-with-target-user-id [resolver]
  (fn [{{:keys [tx]
         {auth-user-id :id} :authenticated-entity
         :as request} :request
        container ::lacinia/container-type-name
        target-user-id ::target-user/id
        :as context}
       args
       value]
    (let [[user-id :as distinct-user-ids]
          (->> [(and (#{:User} container) (:id value))
                (:userId value)
                (:userId args)]
               (remove nil?)
               distinct)
          target-user-id*
          (or target-user-id
              (cond
                (> (count distinct-user-ids) 1)
                (throw (ex-info "User ID inconsistency found!" {}))
                (and user-id
                     (not= user-id auth-user-id)
                     (not (delegations/member? tx auth-user-id user-id)))
                (throw (ex-info "User ID not authorized!" {}))
                :else (or user-id auth-user-id)))]
      (resolve/with-context
        (resolver (assoc context ::target-user/id target-user-id*)
                  args
                  value)
        {::target-user/id target-user-id*}))))

(defn wrap-debug [resolver]
  (fn [context args value]
    (resolver context args value)))

(def resolvers
  (-> queries/resolvers
      (merge mutations/resolvers)
      (transform-resolvers (comp wrap-resolver-with-error
                                 wrap-resolver-with-target-user-id
                                 wrap-resolver-with-camelCase
                                 wrap-resolver-with-kebab-case
                                 wrap-resolver-with-dates-validation))))

;#### debug ###################################################################
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
