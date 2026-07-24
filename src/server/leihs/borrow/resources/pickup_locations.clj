(ns leihs.borrow.resources.pickup-locations
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def columns [:pickup_locations.id
              :pickup_locations.name
              :pickup_locations.description
              :pickup_locations.inventory_pool_id])

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

(defn get-by-id [tx id]
  (when id
    (-> (apply sql/select columns)
        (sql/from :pickup_locations)
        (sql/where [:= :id id])
        sql-format
        (->> (jdbc-query tx))
        first)))

(defn get-multiple [{{tx :tx} :request} _ {pool-id :id}]
  (get-by-pool-id tx pool-id))

(defn get-one [{{tx :tx} :request} _ {:keys [pickup-location-id]}]
  (get-by-id tx pickup-location-id))

(defn belongs-to-pool? [tx pickup-location-id pool-id]
  (boolean
   (when (and pickup-location-id pool-id)
     (-> (sql/select :%count.*)
         (sql/from :pickup_locations)
         (sql/where [:and
                     [:= :id pickup-location-id]
                     [:= :inventory_pool_id pool-id]])
         sql-format
         (->> (jdbc-query tx))
         first
         :count
         pos?))))
