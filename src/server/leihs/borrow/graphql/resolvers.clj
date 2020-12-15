(ns leihs.borrow.graphql.resolvers
  (:require [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.borrow.graphql.mutations :as mutations]
            [leihs.borrow.graphql.queries :as queries]
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
  (fn [{{tx :tx {auth-user-id :id} :authenticated-entity} :request 
        container ::lacinia/container-type-name
        :as context}
       args
       value]
    (let [[user-id :as distinct-user-ids]
           (->> [(and (#{:CurrentUser :User} container) (:id value))
                 (:user-id value)
                 (:user-id args)]
                (remove nil?)
                distinct)]
      (cond
        (> (count distinct-user-ids) 1)
        (throw (ex-info "User ID inconsistency found!" {}))

        (and user-id
             (not= user-id auth-user-id)
             (not (delegations/member? tx auth-user-id user-id)))
        (throw (ex-info "User ID not authorized!" {}))

        :else (resolver (assoc-in context
                                  [:request :target-user-id]
                                  (or user-id auth-user-id))
                        args
                        value)))))

(def resolvers
  (-> queries/resolvers
      (merge mutations/resolvers)
      (transform-resolvers (comp wrap-resolver-with-error
                                 wrap-resolver-with-camelCase
                                 wrap-resolver-with-kebab-case
                                 wrap-resolver-with-dates-validation
                                 wrap-resolver-with-target-user-id))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
