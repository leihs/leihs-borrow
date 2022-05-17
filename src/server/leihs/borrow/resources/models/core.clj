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
