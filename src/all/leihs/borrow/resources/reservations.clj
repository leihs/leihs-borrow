(ns leihs.borrow.resources.reservations
  (:refer-clojure :exclude [count])
  (:require [leihs.borrow.time :as time]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.availability :as availability]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.database.helpers :as database]
            [leihs.core.sql :as sql]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

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

(defn get-multiple [{{:keys [tx]} :request :as context}
                    {:keys [order-by]}
                    value]
  (-> (sql/select :*)
      (sql/from :reservations)
      (sql/where [:=
                  (case (::lacinia/container-type-name context)
                    :PoolOrder :order_id)
                  (:id value)])
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      sql/format
      (query tx)))

(defn create [{{:keys [tx authenticated-entity]} :request :as context}
              {:keys [model-id quantity] :as args}
              value]
  (let [user-id (:id authenticated-entity)]
    (if-not (models/reservable? tx model-id user-id)
      (throw (ex-info "Model either does not exist or is not reservable by the user." {})))
    (if-not (<= quantity (->> (availability/get context args value)
                              :dates
                              (map :quantity)
                              (apply min)))
      (throw (ex-info "The desired quantity is not available." {})))
    (let [row (-> (transform-keys csk/->snake_case args)
                  (update :start_date #(sql/call :cast % :date))
                  (update :end_date #(sql/call :cast % :date))
                  (assoc :quantity 1
                         :user_id user-id
                         :status (sql/call :cast "unsubmitted" :reservation_status)
                         :created_at (time/now tx)
                         :updated_at (time/now tx)))]
      (-> (sql/insert-into :reservations)
          (sql/values (->> row
                           repeat
                           (take quantity)))
          (assoc :returning
                 (as-> (database/columns tx "reservations") <>
                   (remove #{:created_at :updated_at} <>)
                   (conj <>
                         (helpers/iso8601-created-at)
                         (helpers/iso8601-updated-at))))
          sql/format
          (query tx)))))
