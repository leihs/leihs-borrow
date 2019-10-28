(ns leihs.borrow.resources.reservations
  (:refer-clojure :exclude [count])
  (:require [leihs.borrow.time :as time]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.availability :as availability]
            [leihs.core.sql :as sql]
            [leihs.core.ds :refer [get-ds]]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn columns [tx]
  (-> (sql/select :column_name)
      (sql/from :information_schema.columns)
      (sql/where [:= :table_name "reservations"])
      sql/format
      (->> (jdbc/query (get-ds))
           (map (comp keyword :column_name)))))

(defn count [tx model-id]
  (-> (sql/select :%count.*)
      (sql/from :reservations)
      (sql/where [:= :model_id model-id])
      sql/format
      (->> (jdbc/query tx))
      first
      :count))

(defn updated-at [tx model-id]
  (def foo 'bar)
  (-> (sql/select :updated_at)
      (sql/from :reservations)
      (sql/where [:= :model_id model-id])
      (sql/order-by [:updated_at :desc])
      (sql/limit 1)
      sql/format
      (->> (jdbc/query tx))
      first
      :updated_at))

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
                 (->> (columns tx)
                      (remove #{:created_at :updated_at})
                      (concat [(sql/raw "TO_CHAR(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS created_at")
                               (sql/raw "TO_CHAR(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS updated_at")])))
          sql/format
          (->> (jdbc/query tx))))))
