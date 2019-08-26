(ns leihs.borrow.resources.reservations
  (:require [leihs.core.sql :as sql]
            [clojure.java.jdbc :as jdbc]
            ))

(defn updated-at [tx model-id]
  (-> (sql/select :updated_at)
      (sql/from :reservations)
      (sql/where [:= :model_id model-id])
      (sql/order-by [:updated_at :desc])
      (sql/limit 1)
      sql/format
      (->> (jdbc/query tx))
      first
      :updated_at))
