(ns leihs.borrow.resources.reservations
  (:refer-clojure :exclude [count])
  (:require [leihs.borrow.time :as time]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.inventory-pools :as inventory-pools]
            [leihs.borrow.resources.availability :as availability]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.database.helpers :as database]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn columns [tx]
  (as-> (database/columns tx "reservations") <>
    (remove #{:created_at :updated_at} <>)
    (conj <>
          (helpers/iso8601-created-at)
          (helpers/iso8601-updated-at))))

(defn query [sql-format tx]
  (jdbc/query tx
              sql-format
              {:row-fn 
               #(update % :status clojure.string/upper-case)}))

(defn count [tx model-id]
  (-> (sql/select :%count.*)
      (sql/from :reservations)
      (sql/where [:= :model_id model-id])
      sql/format
      (query tx)
      first
      :count))

(defn updated-at [tx model-id]
  (-> (sql/select :updated_at)
      (sql/from :reservations)
      (sql/where [:= :model_id model-id])
      (sql/order-by [:updated_at :desc])
      (sql/limit 1)
      sql/format
      (query tx)
      first
      :updated_at))

(defn for-customer-order [tx user-id]
  (-> (sql/select :*)
      (sql/from :reservations)
      (sql/merge-where [:= :user_id user-id])
      (sql/merge-where
        [:=
         :status
         (sql/call :cast "unsubmitted" :reservation_status)])
      sql/format
      (->> (jdbc/query tx))))

(defn get-multiple
  [{{:keys [tx] user :authenticated-entity} :request :as context}
   {:keys [order-by]}
   value]
  (-> (apply sql/select (columns tx))
      (sql/from :reservations)
      (sql/where (case (::lacinia/container-type-name context)
                   :PoolOrder [:= :order_id (:id value)]
                   :Visit [:in :id (:reservation-ids value)]
                   :CurrentUser [:and
                                 [:= :status (sql/call :cast
                                                       "unsubmitted"
                                                       :reservation_status)]
                                 [:= :user_id (:id user)]]))
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      sql/format
      (query tx)))

(defn delete [{{:keys [tx] {user-id :id} :authenticated-entity} :request}
              {:keys [ids]}
              _]
  (-> (sql/delete-from [:reservations :r])
      (sql/where [:in :r.id ids])
      (sql/merge-where
        [:or
         [:= :r.user_id user-id]
         [:exists (-> (sql/select true)
                      (sql/from [:delegations_users :du])
                      (sql/where [:= :du.user_id user-id])
                      (sql/merge-where [:= :du.delegation_id :r.user_id]))]])
      (sql/returning :id)
      sql/format
      (->> (jdbc/query tx)
           (map :id))))

(defn distribute [pool-avails quantity]
  (if (> quantity
         (->> pool-avails (map :quantity) (apply +)))
    (throw (ex-info "The desired quantity is not available." {}))
    (loop [[{pool-quantity :quantity :as pool-avail} & remaining-pool-avails]
           (sort-by :quantity #(> %1 %2) pool-avails)
         desired-quantity quantity
         result []]
      (if (< pool-quantity desired-quantity)
        (recur remaining-pool-avails
               (- desired-quantity pool-quantity)
               (conj result pool-avail))
        (conj result (assoc pool-avail :quantity desired-quantity))))))

(defn create
  [{{:keys [tx authenticated-entity]} :request :as context}
   {:keys [model-id start-date end-date quantity inventory-pool-ids] :as args}
   value]
  (let [user-id (:id authenticated-entity)]
    (if-not (models/reservable? context args {:id model-id})
      (throw
        (ex-info
          "Model either does not exist or is not reservable by the user." {})))
    (let [pool-ids (or (not-empty inventory-pool-ids)
                       (map :id
                            (inventory-pools/get-multiple context {} nil)))
          pool-avails (availability/get-available-quantities
                        context
                        {:inventory-pool-ids pool-ids
                         :model-ids [model-id]
                         :start-date start-date
                         :end-date end-date}
                        nil)]
      (->> quantity
           (distribute pool-avails)
           (map (fn [{:keys [quantity] :as attrs}]
                  (let [row (-> (transform-keys csk/->snake_case attrs)
                                (assoc :start_date (sql/call :cast start-date :date))
                                (assoc :end_date (sql/call :cast end-date :date))
                                (assoc :quantity 1
                                       :user_id user-id
                                       :status (sql/call :cast
                                                         "unsubmitted"
                                                         :reservation_status)
                                       :created_at (time/now tx)
                                       :updated_at (time/now tx)))]
                    (-> (sql/insert-into :reservations)
                        (sql/values (->> row
                                         repeat
                                         (take quantity)))
                        (assoc :returning (columns tx))
                        sql/format
                        (query tx)))))
           flatten))))
