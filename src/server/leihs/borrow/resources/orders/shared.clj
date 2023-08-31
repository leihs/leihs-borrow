(ns leihs.borrow.resources.orders.shared
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn delegated-user-id [tx order-id]
  (-> (sql/select :delegated_user_id)
      (sql/modifiers :distinct)
      (sql/from :reservations)
      (sql/where [:= :order_id order-id])
      sql/format
      (->> (jdbc/query tx))
      first :delegated_user_id))
