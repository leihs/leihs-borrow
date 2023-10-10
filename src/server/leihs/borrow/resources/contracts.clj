(ns leihs.borrow.resources.contracts
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]

            [clojure.string :refer [lower-case]]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.borrow.graphql.connections :as connections]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.database.helpers :as database]
            [leihs.core.settings :refer [settings!]]))

(defn base-sqlmap [tx user-id]
  (-> (sql/select-distinct :contracts.*)
      (sql/from :contracts)
      (sql/where [:= :contracts.user_id user-id])))

(defn where-or-where-according-to-container
  [sqlmap container {:keys [id]}]
  (case container
    :User (-> sqlmap
              (dissoc :where)
              (sql/where [:= :contracts.user_id id]))
    :PoolOrder (-> sqlmap
                   (sql/join :reservations
                             [:= :reservations.contract_id :contracts.id])
                   (sql/where [:= :reservations.order_id id]))
    :Rental (-> sqlmap
                (sql/join :reservations
                          [:= :reservations.contract_id :contracts.id])
                (sql/join :unified_customer_orders
                          [(keyword "<@")
                           [:raw "ARRAY[reservations.id]"]
                           :unified_customer_orders.reservation_ids])
                (sql/where [:= :unified_customer_orders.id id])
                (sql/group-by :contracts.id))
    sqlmap))

(defn get-connection-sql-map [{{tx :tx-next} :request
                               user-id ::target-user/id
                               container ::lacinia/container-type-name}
                              {:keys [states order-by]}
                              value]
  (-> (base-sqlmap tx user-id)
      (cond-> value
        (where-or-where-according-to-container container value))
      (cond-> states
        (sql/where
         [:in
          :contracts.state
          (map #(-> % name lower-case) states)]))
      (cond-> (seq order-by)
        (as-> sqlmap
          (apply sql/order-by sqlmap (helpers/treat-order-arg order-by :contracts))))))

(defn print-url [{{tx :tx-next} :request} _ {:keys [id inventory-pool-id]}]
  (str (:external_base_url (settings! tx))
       "/manage/" inventory-pool-id 
       "/contracts/" id))

(defn get-one
  [{{tx :tx-next} :request user-id ::target-user/id}
   {:keys [id]}
   {:keys [contract-id]}]
  (-> (base-sqlmap tx user-id)
      (sql/where [:= :contracts.id (or id contract-id)])
      sql-format
      (->> (jdbc-query tx))
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
