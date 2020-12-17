(ns leihs.borrow.resources.visits
  (:require [leihs.core.sql :as sql]
            [leihs.core.database.helpers :as database]
            [leihs.core.ds :as ds]
            [leihs.borrow.resources.helpers :as helpers]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn columns [tx]
  (as-> (database/columns tx "visits") <>
    (remove #{:date} <>)
    (conj <> (helpers/date))))

(defn base-sqlmap [tx]
  (-> (apply sql/select (columns tx))
      (sql/from :visits)))

(defn merge-where-according-to-container
  [sqlmap container {:keys [id]}]
  (letfn [(merge-join-visits-reservations [sqlmap]
            (sql/merge-join
              sqlmap
              :reservations
              (sql/raw "ARRAY[reservations.id] <@ visits.reservation_ids")))]
    (case container
      :PoolOrder
      (-> sqlmap
          merge-join-visits-reservations
          (sql/merge-where [:= :reservations.order_id id])
          (sql/modifiers :distinct))
      :Order
      (-> sqlmap
          merge-join-visits-reservations
          (sql/merge-join :orders
                          [:= :reservations.order_id :orders.id])
          (sql/merge-where [:= :orders.customer_order_id id])
          (sql/modifiers :distinct))
      sqlmap)))

(defn get-visits-sqlmap
  [{{:keys [tx] user-id :target-user-id} :request
    container ::lacinia/container-type-name}
   {:keys [limit order-by]}
   value]
  (-> (base-sqlmap tx)
      (sql/merge-where [:= :visits.user_id user-id])
      (merge-where-according-to-container container value)
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by :visits)))
      (cond-> limit (-> (sql/limit limit)))))

(defn get-pickups [{{:keys [tx]} :request :as context}
                   args
                   value]
  (-> (get-visits-sqlmap context args value)
      (sql/merge-where [:= :visits.type "hand_over"])
      (sql/merge-where [:= :visits.is_approved true])
      sql/format
      (as-> <> (jdbc/query tx <>))))

(defn get-returns [{{:keys [tx]} :request :as context}
                   args
                   value]
  (-> (get-visits-sqlmap context args value)
      (sql/merge-where [:= :visits.type "take_back"])
      sql/format
      (as-> <> (jdbc/query tx <>))))
