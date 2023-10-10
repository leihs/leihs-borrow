(ns leihs.borrow.resources.models.core
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def base-sqlmap
  (-> (sql/select-distinct
       :models.*
       [[:raw "trim(both ' ' from concat_ws(' ', models.product, models.version))"]
        :name])
      (sql/from :models)
      (sql/order-by [:name :asc])))

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn borrowable-items [tx model-id pool-id]
  (-> (sql/select :*)
      (sql/from :items)
      (sql/where [:= :model_id model-id])
      (sql/where [:= :inventory_pool_id pool-id])
      (sql/where [:= :retired nil])
      (sql/where [:= :is_borrowable true])
      (sql/where [:= :parent_id nil])
      sql-format
      (->> (jdbc-query tx))))
