(ns leihs.borrow.resources.contracts
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [clojure.string :refer [lower-case]]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.core.core :refer [spy-with]]
            [leihs.core.sql :as sql]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.graphql.connections :refer [row-cursor cursored-sqlmap] :as connections]))

(defn base-sqlmap [user-id]
  (-> (sql/select :contracts.*)
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
    :Order (-> sqlmap
               (sql/modifiers :distinct)
               (sql/merge-join :reservations
                               [:= :reservations.contract_id :contracts.id])
               (sql/merge-join :orders
                               [:= :reservations.order_id :orders.id])
               (sql/merge-where [:= :orders.customer_order_id id]))
    sqlmap))

(defn get-connection-sql-map [{{:keys [tx]} :request
                               user-id ::target-user/id
                               container ::lacinia/container-type-name}
                              {:keys [states order-by]}
                              value]
  (-> (base-sqlmap user-id)
      (cond-> value
        (!where-or-merge-where-according-to-container container value))
      (cond-> states
        (sql/merge-where
          [:in
           :contracts.state
           (map #(-> % name lower-case) states)]))
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by :contracts)))))

(defn get-one
  [{{:keys [tx]} :request user-id ::target-user/id}
   {:keys [id]} 
   {:keys [contract-id]}]
  (-> (sql/select :contracts.*)
      (sql/from :contracts)
      (sql/merge-where [:= :contracts.id (or id contract-id)])
      (sql/merge-where [:= :contracts.user_id user-id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-connection [context args value]
  (connections/wrap get-connection-sql-map
                    context
                    args
                    value)) 

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
