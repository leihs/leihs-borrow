(ns leihs.borrow.resources.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [clojure.tools.logging :as log]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.sql :as sql]
            [leihs.core.user.queries :refer [merge-search-term-where-clause]]))

(defn sql-order-users
  [sqlmap]
  (sql/order-by
    sqlmap
    (sql/call :concat :users.firstname :users.lastname :users.login :users.id)))

(def base-sqlmap
  (-> (sql/select
        :users.*
        [(sql/raw "users.firstname || ' ' || users.lastname") :name])
      (sql/from :users)
      sql-order-users))

(defn get-multiple [context {:keys [offset limit order-by search-term]} _]
  (jdbc/query
    (-> context :request :tx)
    (-> (cond-> base-sqlmap 
          search-term
            (merge-search-term-where-clause search-term)
          (seq order-by)
            (-> (sql/order-by (helpers/treat-order-arg order-by :users)))
          offset
            (sql/offset offset)
          limit
            (sql/limit limit))
        sql/format)))

(defn get-by-id [tx id]
  (-> base-sqlmap
      (sql/merge-where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one [{{:keys [tx]} :request user-id ::target-user/id} _ _]
  (get-by-id tx user-id))

(defn get-current
  [{{tx :tx} :request user-id ::target-user/id} _ _]
  {:id user-id
   :user (get-by-id tx user-id)})

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
