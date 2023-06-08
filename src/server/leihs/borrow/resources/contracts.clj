(ns leihs.borrow.resources.contracts
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [lower-case]]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.borrow.graphql.connections :as connections]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.database.helpers :as database]
            [leihs.core.settings :refer [settings!]]
            [leihs.core.sql :as sql]))

(defn columns [tx]
  (as-> (database/columns tx "contracts") <>
    (remove #{:created_at :updated_at} <>)
    (conj <>
          (helpers/date-time-created-at :contracts)
          (helpers/date-time-updated-at :contracts))))

(defn base-sqlmap [tx user-id]
  (-> (apply sql/select (columns tx))
      (sql/from :contracts)
      (sql/where [:= :contracts.user_id user-id])))

(defn !where-or-merge-where-according-to-container
  [sqlmap container {:keys [id]}]
  (case container
    :User #_"WHERE override!" (sql/where
                                sqlmap
                                [:= :contracts.user_id id])
    :PoolOrder (-> sqlmap
                   (sql/modifiers :distinct)
                   (sql/merge-join :reservations
                                   [:= :reservations.contract_id :contracts.id])
                   (sql/merge-where [:= :reservations.order_id id]))
    :Rental (-> sqlmap
                (sql/merge-join :reservations
                                [:= :reservations.contract_id :contracts.id])
                (sql/merge-join :unified_customer_orders
                                ["<@"
                                 (sql/raw "ARRAY[reservations.id]")
                                 :unified_customer_orders.reservation_ids])
                (sql/merge-where [:= :unified_customer_orders.id id])
                (sql/group :contracts.id))
    sqlmap))

(defn get-connection-sql-map [{{:keys [tx]} :request
                               user-id ::target-user/id
                               container ::lacinia/container-type-name}
                              {:keys [states order-by]}
                              value]
  (-> (base-sqlmap tx user-id)
      (cond-> value
        (!where-or-merge-where-according-to-container container value))
      (cond-> states
        (sql/merge-where
          [:in
           :contracts.state
           (map #(-> % name lower-case) states)]))
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by :contracts)))))

(defn print-url [{{:keys [tx]} :request} _ {:keys [id inventory-pool-id]}]
  (str (:external_base_url (settings! tx))
       "/manage/" inventory-pool-id 
       "/contracts/" id))

(defn get-one
  [{{:keys [tx]} :request user-id ::target-user/id}
   {:keys [id]}
   {:keys [contract-id]}]
  (-> (base-sqlmap tx user-id)
      (sql/merge-where [:= :contracts.id (or id contract-id)])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-connection [context args value]
  (connections/wrap get-connection-sql-map
                    context
                    args
                    value))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
