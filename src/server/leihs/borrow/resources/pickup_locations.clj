(ns leihs.borrow.resources.pickup-locations
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def columns [:pickup_locations.id
              :pickup_locations.name
              :pickup_locations.description])

(defn base-sqlmap [pool-id]
  (-> (apply sql/select columns)
      (sql/from :pickup_locations)
      (sql/where [:= :inventory_pool_id pool-id])
      (sql/order-by :name)))

(defn get-by-pool-id [tx pool-id]
  (-> pool-id
      base-sqlmap
      sql-format
      (->> (jdbc-query tx))))

(defn get-multiple [{{tx :tx} :request} _ {pool-id :id}]
  (get-by-pool-id tx pool-id))
