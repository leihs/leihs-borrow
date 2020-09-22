(ns leihs.borrow.resources.visits
  (:require [leihs.core.core :refer [spy-with]]
            [leihs.core.sql :as sql]
            [leihs.core.database.helpers :as database]
            [leihs.core.ds :as ds]
            [leihs.borrow.resources.helpers :as helpers]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.resolve :as resolve]))

(defn columns [tx]
  (as-> (database/columns tx "visits") <>
    (remove #{:date} <>)
    (conj <> (helpers/date))))

(defn base-sqlmap [tx]
  (-> (apply sql/select (columns tx))
      (sql/from :visits)))

(defn get-one [{{:keys [tx] user-id :target-user-id} :request :as context}
               {:keys [id]}
               _]
  (-> (base-sqlmap tx)
      (sql/merge-where [:= :id id])
      sql/format
      (as-> <> (jdbc/query tx <>))
      first
      (#(schema/tag-with-type %
                              (case (:type %)
                                "hand_over" :Pickup
                                "take_back" :Return)))
      (->> (spy-with meta))))

(defn get-multiple [{{:keys [tx] user-id :target-user-id} :request :as context}
                    {:keys [limit order-by]}
                    _]
  (-> (base-sqlmap tx)
      (sql/merge-where [:= :user_id user-id])
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      (cond-> limit (-> (sql/limit limit)))
      sql/format
      (as-> <> (jdbc/query tx <>))
      (->> (map (fn [v]
                  (schema/tag-with-type v
                                        (case (:type v)
                                          "hand_over" :Pickup
                                          "take_back" :Return)))))))
