(ns leihs.borrow.resources.orders
  (:require [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.core.database.helpers :as database]
            [clojure.string :refer [upper-case]]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.reservations :as reservations]))

(def distinct-states-sql-expr
  (sql/raw "array_agg(DISTINCT upper(orders.state))"))

(defn multiple-base-sqlmap [tx user-id]
  (let [common-columns [:id :purpose :created_at :updated_at]]
    (-> (sql/select :orders_union.id
                    :orders_union.purpose
                    [distinct-states-sql-expr :state]
                    (helpers/iso8601-created-at :orders_union)
                    (helpers/iso8601-updated-at :orders_union))
        (sql/from
          [{:union [(-> (apply sql/select common-columns)
                        (sql/from :customer_orders)
                        (sql/where [:= :user_id user-id]))
                    (-> (apply sql/select common-columns)
                        (sql/from :orders)
                        (sql/where [:= :user_id user-id])
                        (sql/merge-where [:= :customer_order_id nil]))]}
           :orders_union])
        (sql/join :orders
                  [:or
                   [:= :orders_union.id :orders.customer_order_id]
                   [:= :orders_union.id :orders.id]])
        (assoc :group-by [:orders_union.id
                          :orders_union.purpose
                          :orders_union.created_at
                          :orders_union.updated_at]))))

(defn pool-order-row [row]
  (update row :state upper-case))

(defn get-one
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request}
   {:keys [id]}
   _]
  (-> (sql/select :*)
      (sql/from [(multiple-base-sqlmap tx user-id) :tmp])
      (sql/merge-where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn equal-condition [a1 a2]
  [:and ["@>" a1 a2] ["<@" a1 a2]])

(defn get-multiple
  [{{:keys [tx], {user-id :id} :authenticated-entity} :request} 
   {:keys [states order-by]} 
   _]
  (-> (multiple-base-sqlmap tx user-id)
      (cond-> states
        (sql/having (equal-condition
                      distinct-states-sql-expr
                      (->> states
                           set
                           (map #(sql/call :cast (name %) :text))
                           sql/array))))
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      sql/format
      (->> (jdbc/query tx))))

(defn get-multiple-by-pool [{{:keys [tx]} :request :as context}
                            {:keys [order-by]}
                            value]
  (-> (sql/select :*)
      (sql/from :orders)
      ; An old pool order without customer order become
      ; customer order itself and contains itself as a
      ; sub-order too.
      (sql/where [:or
                  [:= :customer_order_id (:id value)]
                  [:= :id (:id value)]])
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      sql/format
      (as-> <>
        (jdbc/query tx
                    <>
                    {:row-fn pool-order-row}))))

(defn submit [{{:keys [tx] {user-id :id} :authenticated-entity} :request}
              {:keys [purpose]}
              _]
  (let [reservations (reservations/for-customer-order tx user-id)]
    (if (empty? reservations)
      (throw (ex-info "User does not have any unsubmitted reservations." {})))
    ; TODO: validations
    (let [customer-order (-> (sql/insert-into :customer_orders)
                             (sql/values [{:purpose purpose
                                           :user_id user-id}])
                             (sql/returning :id
                                            :purpose
                                            (helpers/iso8601-created-at)
                                            (helpers/iso8601-updated-at))
                             sql/format
                             (->> (jdbc/query tx))
                             first)]
      (doseq [[pool-id rs] (group-by :inventory_pool_id reservations)]
        (let [order (-> (sql/insert-into :orders)
                        (sql/values [{:user_id user-id
                                      :inventory_pool_id pool-id
                                      :state "submitted"
                                      :purpose purpose
                                      :customer_order_id (:id customer-order)}])
                        (sql/returning :*)
                        sql/format
                        (->> (jdbc/query tx))
                        first)]
          (-> (sql/update :reservations)
              (sql/set {:status (sql/call :cast
                                          "submitted"
                                          :reservation_status)
                        :inventory_pool_id pool-id
                        :order_id (:id order)})
              (sql/where [:in :id (map :id rs)])
              (sql/returning :*)
              sql/format
              (reservations/query tx))))
      (assoc customer-order :state #{"SUBMITTED"}))))
