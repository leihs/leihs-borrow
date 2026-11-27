(ns leihs.borrow.resources.models.core
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def base-sqlmap
  (-> (sql/select-distinct
       :models.*
       [[:raw "trim(both ' ' from models.name)"] :name])
      (sql/from :models)
      (sql/order-by [:models.name :asc])))

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql-format
      (->> (jdbc-query tx))
      first))
