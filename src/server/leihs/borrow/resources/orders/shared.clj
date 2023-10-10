(ns leihs.borrow.resources.orders.shared
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(defn delegated-user-id [tx order-id]
  (-> (sql/select-distinct :delegated_user_id)
      (sql/from :reservations)
      (sql/where [:= :order_id order-id])
      sql-format
      (->> (jdbc-query tx))
      first :delegated_user_id))
