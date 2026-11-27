(ns leihs.borrow.resources.users.shared
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(defn sql-order-users
  [sqlmap]
  (sql/order-by
   sqlmap
   [[:concat :users.firstname :users.lastname :users.login :users.id]]))

(def base-sqlmap
  (-> (sql/select
       :users.*
       [[:raw "users.firstname || ' ' || users.lastname"] :name])
      (sql/from :users)
      sql-order-users))

(defn get-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql-format
      (->> (jdbc-query tx))
      first))
