(ns leihs.borrow.resources.models.core
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(def base-sqlmap
  (-> (sql/select
       :models.*
       [(sql/raw "trim(both ' ' from concat_ws(' ', models.product, models.version))")
        :name])
      (sql/modifiers :distinct)
      (sql/from :models)
      (sql/order-by [:name :asc])))

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn borrowable-items [tx model-id pool-id]
  (-> (sql/select :*)
      (sql/from :items)
      (sql/merge-where [:= :model_id model-id])
      (sql/merge-where [:= :inventory_pool_id pool-id])
      (sql/merge-where [:= :retired nil])
      (sql/merge-where [:= :is_borrowable true])
      (sql/merge-where [:= :parent_id nil])
      sql/format
      (->> (jdbc/query tx))))
