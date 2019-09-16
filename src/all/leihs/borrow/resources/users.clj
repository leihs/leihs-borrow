(ns leihs.borrow.resources.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [clojure.tools.logging :as log]
            [leihs.core.sql :as sql]
            [leihs.core.user.queries :refer [merge-search-term-where-clause]]))

(defn sql-order-users
  [sqlmap]
  (sql/order-by
    sqlmap
    (sql/call :concat :users.firstname :users.lastname :users.login :users.id)))

(def users-base-query
  (-> (sql/select :users.*)
      (sql/from :users)
      sql-order-users))

(defn get-multiple
  [context args _]
  (jdbc/query
    (-> context
        :request
        :tx)
    (let [search-term (:search_term args)
          offset (:offset args)
          limit (:limit args)]
      (-> (cond-> users-base-query 
            search-term
              (merge-search-term-where-clause search-term)
            offset
              (sql/offset offset)
            limit
              (sql/limit limit))
          sql/format))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
