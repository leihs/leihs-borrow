(ns leihs.borrow.resources.orders
  (:refer-clojure :exclude [resolve])
  (:require [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.core.database.helpers :as database]
            [clojure.string :refer [upper-case]]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [leihs.borrow.connections :refer [row-cursor cursored-sqlmap] :as connections]
            [leihs.borrow.mails :as mails]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.settings :as settings]
            [leihs.borrow.resources.reservations :as reservations]))

(def distinct-states-sql-expr
  (sql/raw "array_agg(DISTINCT upper(orders.state))"))

(defn multiple-base-sqlmap [tx user-id]
  (let [common-columns [:id :purpose :created_at :updated_at]]
    (-> (sql/select :orders_union.id
                    :orders_union.purpose
                    [distinct-states-sql-expr :state]
                    (helpers/date-time-created-at :orders_union)
                    (helpers/date-time-updated-at :orders_union))
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

(defn get-connection-sql-map
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request}
   {:keys [order-by states]}
   value]
  (-> (multiple-base-sqlmap tx user-id)
      (cond-> states
        (sql/having (equal-condition
                      distinct-states-sql-expr
                      (->> states
                           set
                           (map #(sql/call :cast (name %) :text))
                           sql/array))))
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))))

(defn get-connection [context args value]
  (connections/wrap get-connection-sql-map
                    context
                    args
                    value)) 

(defn get-multiple-by-pool [{{:keys [tx]} :request :as context}
                            {:keys [order-by]}
                            value]
  (let [columns (as-> (database/columns tx "orders") <>
                  (remove #{:created_at :updated_at} <>)
                  (conj <>
                        (helpers/date-time-created-at)
                        (helpers/date-time-updated-at)))]
    (-> (apply sql/select columns)
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
                      {:row-fn pool-order-row})))))

(defn valid-until-sql [tx]
  [(sql/call :to_char
             (sql/raw
               (str "updated_at + interval '"
                    (:timeout_minutes (settings/get tx))
                    "minutes'"))
             helpers/date-time-format)
   :updated_at])

(defn valid-until [tx user-id]
  (-> (reservations/unsubmitted-sqlmap tx user-id)
      (sql/select (valid-until-sql tx))
      (sql/order-by [:updated_at :asc])
      (sql/limit 1)
      sql/format
      (->> (jdbc/query tx))
      first
      :updated_at))

(defn get-unsubmitted
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request} _ _]
  {:valid-until (valid-until tx user-id)
   :reservations (-> (reservations/unsubmitted-sqlmap tx user-id)
                     sql/format
                     (reservations/query tx))})

(defn submit
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request :as context}
   {:keys [purpose]}
   _]
  (let [reservations (reservations/for-customer-order tx user-id)]
    (if (empty? reservations)
      (throw (ex-info "User does not have any unsubmitted reservations." {})))
    (if-not (reservations/available-in-respective-pools? context reservations)
      (throw (ex-info "Some reserved quantities are not available anymore." {})))
    (let [customer-order (-> (sql/insert-into :customer_orders)
                             (sql/values [{:purpose purpose
                                           :user_id user-id}])
                             (sql/returning :id
                                            :purpose
                                            (helpers/date-time-created-at)
                                            (helpers/date-time-updated-at))
                             sql/format
                             (->> (jdbc/query tx))
                             first
                             (assoc :state #{"SUBMITTED"}))]
      (loop [[[pool-id rs :as group-el] & remainder]
             (seq (group-by :inventory_pool_id reservations))
             mails []]
        (if (seq group-el)
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
                (reservations/query tx))
            (recur remainder
                   (conj mails #(mails/send-received context order))))
          (do (set! ds/after-tx mails)
              customer-order))))))

(defn timeout? [tx user-id]
  (let [minutes (:timeout_minutes (settings/get tx))]
    (-> (sql/select
          [(sql/call :>
                     (sql/call :now)
                     (sql/call :+
                               (-> (reservations/unsubmitted-sqlmap tx user-id)
                                   (sql/select :updated_at)
                                   (sql/order-by [:updated_at :desc])
                                   (sql/limit 1))
                               (sql/raw (str "interval '" minutes " minutes'"))))
           :result])
        sql/format
        (->> (jdbc/query tx))
        first
        :result)))

(defn refresh-timeout
  "Always returns the valid until date. If the unsubmitted order is not
  timed-out, then it will be updated as a side-effect."
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request :as context}
   args
   value]
  (if (or (not (timeout? tx user-id))
          (->> user-id
               (reservations/for-customer-order tx)
               (reservations/available-in-respective-pools? context)))
    (-> (sql/update :reservations)
        (sql/set {:updated_at (sql/call :now)})
        (sql/where [:and
                    [:= :status (sql/call :cast
                                          "unsubmitted"
                                          :reservation_status)]
                    [:= :user_id user-id]])
        (sql/returning (valid-until-sql tx))
        (sql/format)
        (->> (jdbc/query tx))
        first
        :updated_at))
  {:unsubmitted-order (get-unsubmitted context args value)})
