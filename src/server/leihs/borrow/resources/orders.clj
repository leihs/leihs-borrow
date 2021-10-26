(ns leihs.borrow.resources.orders
  (:refer-clojure :exclude [resolve])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [upper-case]]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [leihs.borrow.graphql.connections :as connections]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.mails :as mails]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.delegations :as delegations]
            [leihs.borrow.resources.reservations :as reservations]
            [leihs.borrow.resources.settings :as settings]
            [leihs.core.core :refer [spy-with]]
            [leihs.core.database.helpers :as database]
            [leihs.core.ds :as ds]
            [leihs.core.sql :as sql])
  (:import [java.util UUID]
           [java.time.format DateTimeFormatter]
           [java.time ZoneOffset]))

(def refined-rental-state->reservation-status
  {:IN_APPROVAL "submitted"
   :REJECTED "rejected"
   :CANCELED "canceled"
   :RETURNED "closed"
   :TO_PICKUP "approved"
   :TO_RETURN "signed"})

(defn multiple-base-sqlmap [user-id]
  (-> (sql/select :unified_customer_orders.id
                  :unified_customer_orders.user_id
                  :unified_customer_orders.purpose
                  :unified_customer_orders.title
                  :unified_customer_orders.state
                  :unified_customer_orders.rental_state
                  (helpers/date-from-date :unified_customer_orders)
                  (helpers/date-until-date :unified_customer_orders)
                  (helpers/date-time-created-at :unified_customer_orders)
                  (helpers/date-time-updated-at :unified_customer_orders)
                  [(sql/call := :unified_customer_orders.origin_table "customer_orders")
                   :is_customer_order]
                  :unified_customer_orders.origin_table
                  :unified_customer_orders.reservation_ids
                  :unified_customer_orders.reservation_states)
      (sql/from :unified_customer_orders)
      (sql/where [(if (coll? user-id) :in :=)
                  :unified_customer_orders.user_id
                  user-id])))

(defn refine-rental-state [tx row]
  (let [rs (->> row
                :reservation_ids
                (reservations/get-by-ids tx))
        expired? (some #(and (contains? #{"submitted" "approved"} (:status %))
                             (reservations/overdue? %))
                       rs)
        overdue? (some #(and (-> % :status (= "signed"))
                             (reservations/overdue? %))
                       rs)
        states (-> row
                   :reservation_states
                   (->> (map (set/map-invert
                              refined-rental-state->reservation-status)))
                   (cond->
                     expired? (conj :EXPIRED)
                     overdue? (conj :OVERDUE))
                   distinct)]
    (assoc row :refined_rental_state states)))

(defn row-fn [tx r]
  (let [from (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE (:from_date r))
        until (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE (:until_date r))]
    (-> r
        (assoc :total-days (+ 1 (java-time/time-between from until :days)))
        (->> (refine-rental-state tx)))))

(defn total-rental-quantity
  [{{:keys [tx]} :request} _ {:keys [reservation-ids]}]
  (-> (sql/select :*)
      (sql/from :reservations)
      (sql/where [:in :id reservation-ids])
      sql/format
      (->> (jdbc/query tx)
           (map :quantity)
           (apply +))))

(defn pickup-rental-quantity
  [{{:keys [tx]} :request} _ {:keys [reservation-ids]}]
  (-> (sql/select :*)
      (sql/from :reservations)
      (sql/where [:in :id reservation-ids])
      (sql/merge-where [:= :status "approved"])
      sql/format
      (->> (jdbc/query tx)
           (map :quantity)
           (apply +))))

(defn return-rental-quantity
  [{{:keys [tx]} :request} _ {:keys [reservation-ids]}]
  (-> (sql/select :*)
      (sql/from :reservations)
      (sql/where [:in :id reservation-ids])
      (sql/merge-where [:= :status "signed"])
      sql/format
      (->> (jdbc/query tx)
           (map :quantity)
           (apply +))))

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

(defn get-one-by-id [tx user-id id]
  (let [delegation-ids (->> user-id
                            (delegations/get-multiple-by-user-id tx)
                            (map :id))]
    (-> (multiple-base-sqlmap (conj delegation-ids user-id))
        (sql/merge-where [:= :unified_customer_orders.id id])
        sql/format
        (as-> <> (jdbc/query tx <> {:row-fn (partial row-fn tx)}))
        first)))

(defn get-one
  [{{:keys [tx]} :request user-id ::target-user/id}
   {:keys [id]}
   _]
  (get-one-by-id tx user-id id))

(defn equal-condition [a1 a2]
  [:and ["@>" a1 a2] ["<@" a1 a2]])

(defn merge-refined-rental-state-condition [sqlmap refined-rental-state]
  (case refined-rental-state
    (:IN_APPROVAL :REJECTED :CANCELED :RETURNED :TO_PICKUP :TO_RETURN)
    (sql/merge-where sqlmap
                     [:any (refined-rental-state->reservation-status
                            refined-rental-state)
                      :unified_customer_orders.reservation_states])
    :EXPIRED
    (sql/merge-where
     sqlmap
     [:exists (-> (sql/select true)
                  (sql/from :reservations)
                  (sql/where [:any :reservations.id :unified_customer_orders.reservation_ids])
                  (sql/merge-where [:in :reservations.status ["submitted" "approved"]])
                  (sql/merge-where [:> (sql/raw "CURRENT_DATE") :reservations.end_date]))])
    :OVERDUE
    (sql/merge-where
     sqlmap
     [:exists (-> (sql/select true)
                  (sql/from :reservations)
                  (sql/where [:any :reservations.id :unified_customer_orders.reservation_ids])
                  (sql/merge-where [:= :reservations.status "signed"])
                  (sql/merge-where [:> (sql/raw "CURRENT_DATE") :reservations.end_date]))])))

(defn get-connection-sql-map
  [{{:keys [tx]} :request user-id ::target-user/id}
   {:keys [order-by states rental-state from until pool-ids
           search-term with-pickups with-returns refined-rental-state]}
   value]
  (-> (multiple-base-sqlmap user-id)
      (cond->
        states
        (sql/merge-where (equal-condition
                          :unified_customer_orders.state
                          (->> states
                               set
                               (map #(sql/call :cast (name %) :text))
                               sql/array)))

        rental-state
        (sql/merge-where [:= :unified_customer_orders.rental_state (name rental-state)])

        (or from until)
        (sql/merge-where
         (sql/raw
          (format
           "(unified_customer_orders.from_date, unified_customer_orders.until_date) OVERLAPS (%s, %s)"
           (or (some->> from (format "'%s'")) "'1900-01-01'::date")
           (or (some->> until (format "'%s'")) "'9999-12-31'::date"))))

        (not (empty? pool-ids))
        (sql/merge-where ["<@"
                          (->> pool-ids (map #(sql/call :cast % :uuid)) sql/array)
                          :unified_customer_orders.inventory_pool_ids])

        search-term
        (sql/merge-where ["~~*"
                          :unified_customer_orders.searchable
                          (str "%" search-term "%")])

        refined-rental-state
        (merge-refined-rental-state-condition refined-rental-state)

        (not (nil? with-pickups))
        (sql/merge-where [:= :unified_customer_orders.with_pickups with-pickups])

        (not (nil? with-returns))
        (sql/merge-where [:= :unified_customer_orders.with_returns with-returns])

        (seq order-by)
        (sql/order-by
         (helpers/treat-order-arg order-by :unified_customer_orders)))))

(defn get-connection [{{:keys [tx]} :request :as context} args value]
  (connections/wrap (spy-with #(-> % (apply [context args value]) sql/format)
                              get-connection-sql-map)
                    context
                    args
                    value
                    #(map (partial row-fn tx) %))) 

(defn orders-columns [tx]
  (as-> (database/columns tx "orders") <>
    (remove #{:created_at :updated_at} <>)
    (conj <>
          (helpers/date-time-created-at)
          (helpers/date-time-updated-at))))

(defn pool-orders-sqlmap [tx customer-order-id]
  (-> (apply sql/select (orders-columns tx))
      (sql/from :orders)
      (sql/where [:= :customer_order_id customer-order-id])))

(defn pool-orders [tx customer-order-id]
  (-> (pool-orders-sqlmap tx customer-order-id)
      sql/format
      (->> (jdbc/query tx))))

(defn pool-orders-for-state-count [tx customer-order-id state]
  (-> (pool-orders-sqlmap tx customer-order-id)
      (sql/merge-where [:= :state state])
      sql/format
      (->> (jdbc/query tx))
      count))

(defn pool-orders-count 
  [{{:keys [tx]} :request} _ {:keys [id]}]
  (count (pool-orders tx id)))

(defn approved-pool-orders-count
  [{{:keys [tx]} :request} _ {:keys [id origin-table]}]
  (when (= origin-table "customer_orders")
    (pool-orders-for-state-count tx id "approved")))

(defn rejected-pool-orders-count
  [{{:keys [tx]} :request} _ {:keys [id origin-table]}]
  (when (= origin-table "customer_orders")
    (pool-orders-for-state-count tx id "rejected")))

(defn submitted-pool-orders-count
  [{{:keys [tx]} :request} _ {:keys [id origin-table]}]
  (when (= origin-table "customer_orders")
    (pool-orders-for-state-count tx id "submitted")))

(defn get-multiple-by-pool [{{:keys [tx]} :request :as context}
                            {:keys [order-by]}
                            value]
  (-> (pool-orders-sqlmap tx (:id value)) 
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by :orders)))
      sql/format
      (as-> <>
        (jdbc/query tx
                    <>
                    {:row-fn pool-order-row}))))

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
  (let [reservations (reservations/unsubmitted tx user-id)]
    (if (empty? reservations)
      (throw (ex-info "User does not have any unsubmitted reservations." {})))
    (if-not (empty? (reservations/with-invalid-availability context reservations))
      (throw (ex-info "Some reserved quantities are not available anymore." {})))
    (let [uuid (-> (sql/insert-into :customer_orders)
                   (sql/values [{:purpose purpose
                                 :title title
                                 :user_id user-id}])
                   (sql/returning :id)
                   sql/format
                   (->> (jdbc/query tx))
                   first
                   :id)]
      (loop [[[pool-id rs :as group-el] & remainder]
             (seq (group-by :inventory_pool_id reservations))
             mails []]
        (if (seq group-el)
          (let [order (-> (sql/insert-into :orders)
                          (sql/values [{:user_id user-id
                                        :inventory_pool_id pool-id
                                        :state "submitted"
                                        :purpose purpose
                                        :customer_order_id uuid}])
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
              (get-one-by-id tx user-id uuid)))))))

(defn cancel
  [{{:keys [tx]} :request user-id ::target-user/id} {:keys [id]} _]
  (let [customer-order (get-one-by-id tx user-id id)
        pool-orders (pool-orders tx id)]
    (when-not (:is_customer_order customer-order)
      (throw (ex-info "The order is not a customer order." {})))
    (when-not (->> pool-orders (map :state) (every? #{"submitted"}))
      (throw (ex-info "Some pool orders don't have submitted state." {})))
    (-> (sql/update :reservations)
        (sql/set {:status "canceled"})
        (sql/where [:in :order_id (map :id pool-orders)])
        sql/format
        (->> (jdbc/execute! tx)))
    (-> (sql/update :orders)
        (sql/set {:state "canceled"})
        (sql/where [:= :customer_order_id id])
        sql/format
        (->> (jdbc/execute! tx)))
    (get-one-by-id tx user-id id)))

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
