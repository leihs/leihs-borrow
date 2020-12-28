(ns leihs.borrow.resources.orders
  (:refer-clojure :exclude [resolve])
  (:require [leihs.core.core :refer [spy-with]]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.core.database.helpers :as database]
            [clojure.string :refer [upper-case]]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [leihs.borrow.graphql.connections :refer [row-cursor cursored-sqlmap] :as connections]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.mails :as mails]
            [leihs.borrow.time :as time]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.settings :as settings]
            [leihs.borrow.resources.reservations :as reservations]))

(def distinct-states-sql-expr
  (sql/raw "array_agg(DISTINCT upper(orders.state))"))

(defn multiple-base-sqlmap [user-id]
  (-> (sql/select :customer_orders.id
                  :customer_orders.purpose
                  :customer_orders.title
                  [distinct-states-sql-expr :state]
                  (helpers/date-time-created-at :customer_orders)
                  (helpers/date-time-updated-at :customer_orders))
      (sql/from :customer_orders)
      (sql/join :orders
                [:= :customer_orders.id :orders.customer_order_id])
      (sql/where [:= :customer_orders.user_id user-id])
      (assoc :group-by [:customer_orders.id])))

(defn pool-order-row [row]
  (update row :state upper-case))

(defn get-one-by-pool
  [{{:keys [tx]} :request user-id ::target-user/id}
   _
   {pool-order-id :order-id}]
  (-> (sql/select :orders.id
                  :orders.purpose
                  :orders.inventory_pool_id
                  :orders.customer_order_id
                  [(sql/call :upper :orders.state) :state]
                  (helpers/date-time-created-at :orders)
                  (helpers/date-time-updated-at :orders))
      (sql/from :orders)
      (sql/merge-where [:= :id pool-order-id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one
  [{{:keys [tx]} :request user-id ::target-user/id}
   {:keys [id]}
   _]
  (-> (multiple-base-sqlmap user-id)
      (sql/merge-where [:= :customer_orders.id id])
      sql/format
      (->> (jdbc/query tx))
      first
      ))

(defn equal-condition [a1 a2]
  [:and ["@>" a1 a2] ["<@" a1 a2]])

(defn get-connection-sql-map
  [{{:keys [tx]} :request user-id ::target-user/id}
   {:keys [order-by states]}
   value]
  (-> (multiple-base-sqlmap user-id)
      (cond-> states
        (sql/having (equal-condition
                      distinct-states-sql-expr
                      (->> states
                           set
                           (map #(sql/call :cast (name %) :text))
                           sql/array))))
      (cond-> (seq order-by)
        (sql/order-by
          (helpers/treat-order-arg order-by :customer_orders)))))

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
        (sql/where [:= :customer_order_id (:id value)])
        (cond-> (seq order-by)
          (sql/order-by (helpers/treat-order-arg order-by :orders)))
        sql/format
        (as-> <>
          (jdbc/query tx
                      <>
                      {:row-fn pool-order-row})))))

(defn valid-until [tx user-id]
  (-> (reservations/unsubmitted-sqlmap tx user-id)
      (sql/select (reservations/valid-until-sql tx))
      (sql/order-by [:updated_at :asc])
      (sql/limit 1)
      sql/format
      (->> (jdbc/query tx))
      first
      :updated_at))

(defn get-unsubmitted
  [{{:keys [tx]} :request user-id ::target-user/id :as context} _ _]
  (let [rs  (-> (reservations/unsubmitted-sqlmap tx user-id)
                sql/format
                (reservations/query tx))]
    {:valid-until (valid-until tx user-id)
     :reservations rs
     :invalidReservationIds (->> rs
                                 (reservations/with-invalid-availability context)
                                 (map :id))}))

(defn get-draft
  [{{:keys [tx]} :request user-id ::target-user/id :as context} _ _]
  (let [rs  (-> (reservations/draft-sqlmap tx user-id)
                sql/format
                (reservations/query tx))]
    {:reservations rs
     :invalidReservationIds (as-> rs <>
                              (reservations/with-invalid-availability context <>)
                              (map :id <>))}))

(defn submit
  [{{:keys [tx]} :request user-id ::target-user/id :as context}
   {:keys [purpose title]}
   _]
  (let [reservations (reservations/for-customer-order tx user-id)]
    (if (empty? reservations)
      (throw (ex-info "User does not have any unsubmitted reservations." {})))
    (if-not (empty? (reservations/with-invalid-availability context reservations))
      (throw (ex-info "Some reserved quantities are not available anymore." {})))
    (let [customer-order (-> (sql/insert-into :customer_orders)
                             (sql/values [{:purpose purpose
                                           :title title
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
                (sql/set {:status "submitted"
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
  "If any of the unsubmitted reservations has an invalid start date,
  then make them all draft. The unsubmitted order returned is {} in this case.
  Otherwise, if the unsubmitted order is not timed-out or it is timed-out,
  but all the reservations have still valid availability, then the valid
  until date is updated as a side-effect. The unsubmitted order is returned
  in any case."
  [{{:keys [tx]} :request user-id ::target-user/id :as context}
   args
   value]
  (if (reservations/some-unsubmitted-with-invalid-start-date? context)
    (do (reservations/unsubmitted->draft tx user-id)
        {:unsubmitted-order {}})
    (do (if (or (not (timeout? tx user-id))
                (not (reservations/some-unsubmitted-with-invalid-availability? context)))
          (reservations/touch-unsubmitted! tx user-id))
        {:unsubmitted-order (get-unsubmitted context args value)})))
