(ns leihs.borrow.resources.holidays
  (:refer-clojure :exclude [get])
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def columns [:holidays.start_date
              :holidays.end_date
              :holidays.name
              :holidays.orders_processing])

(defn base-sqlmap [pool-id]
  (-> (apply sql/select columns)
      (sql/from :holidays)
      (sql/where [:= :inventory_pool_id pool-id])
      (sql/where [:>= :holidays.end_date :current_date]) ; only future holidays
      (sql/order-by :start_date :end_date)))

(defn get-by-pool-id [tx pool-id]
  (-> pool-id
      base-sqlmap
      sql-format
      (->> (jdbc-query tx))))

(defn get-multiple [{{tx :tx} :request} _ {pool-id :id}]
  (get-by-pool-id tx pool-id))
