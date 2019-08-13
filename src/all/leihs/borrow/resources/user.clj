(ns leihs.borrow.resources.user
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(def user-base-query
  (-> (sql/select :users.*)
      (sql/from :users)))

(defn get-user
  [context _ value]
  (first (jdbc/query (-> context
                         :request
                         :tx)
                     (-> user-base-query
                         (sql/where [:= :users.id
                                     (or (:user_id value) ; for RequesterOrganization
                                         (:value value) ; for RequestFieldUser
                                       )])
                         sql/format))))

(defn get-user-by-id
  [tx id]
  (first (jdbc/query tx
                     (-> user-base-query
                         (sql/where [:= :users.id id])
                         sql/format))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
