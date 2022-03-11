(ns leihs.borrow.resources.users.shared
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

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

(defn get-by-id [tx id]
  (-> base-sqlmap
      (sql/merge-where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))
