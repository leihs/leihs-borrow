(ns leihs.borrow.resources.visits
  (:require [leihs.core.sql :as sql]
            [leihs.core.database.helpers :as database]
            [leihs.core.ds :as ds]
            [leihs.borrow.resources.helpers :as helpers]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn columns [tx]
  (as-> (database/columns tx "visits") <>
    (remove #{:date} <>)
    (conj <> (helpers/date))))

(defn base-sqlmap [tx]
  (-> (apply sql/select (columns tx))
      (sql/from :visits)))

(defn get-visits-sqlmap
  [{{:keys [tx] user-id :target-user-id} :request}
   {:keys [limit order-by]}
   _]
  (-> (base-sqlmap tx)
      (sql/merge-where [:= :user_id user-id])
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      (cond-> limit (-> (sql/limit limit)))))

(defn get-pickups [{{:keys [tx]} :request :as context}
                   args
                   value]
  (-> (get-visits-sqlmap context args value)
      (sql/merge-where [:= :visits.type "hand_over"])
      sql/format
      (as-> <> (jdbc/query tx <>))))

(defn get-returns [{{:keys [tx]} :request :as context}
                   args
                   value]
  (-> (get-visits-sqlmap context args value)
      (sql/merge-where [:= :visits.type "take_back"])
      sql/format
      (as-> <> (jdbc/query tx <>))))
