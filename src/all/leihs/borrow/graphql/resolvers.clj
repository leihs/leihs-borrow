(ns leihs.borrow.graphql.resolvers
  (:require [clojure.tools.logging :as log]
            [leihs.borrow.graphql.mutations :as mutations]
            [leihs.borrow.graphql.queries :as queries]
            [leihs.core.graphql.helpers :refer [transform-resolvers
                                                wrap-resolver-with-error
                                                wrap-resolver-with-kebab-case
                                                wrap-resolver-with-camelCase]]
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

(def resolvers
  (-> queries/resolvers
      (merge mutations/resolvers)
      (transform-resolvers (comp wrap-resolver-with-error
                                 wrap-resolver-with-camelCase
                                 wrap-resolver-with-kebab-case
                                 wrap-resolver-with-dates-validation))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
