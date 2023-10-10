(ns leihs.borrow.resources.users.shared
  (:require [leihs.core.db :as db]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [taoensso.timbre :refer [debug info warn error spy]]))

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

(comment (def tx (db/get-ds-next))
         (get-by-id tx "c0777d74-668b-5e01-abb5-f8277baa0ea8"))
