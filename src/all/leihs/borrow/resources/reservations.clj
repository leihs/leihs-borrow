(ns leihs.borrow.resources.reservations
  (:refer-clojure :exclude [count])
  (:require [leihs.borrow.time :as time]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.availability :as availability]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.database.helpers :as database]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.spec.alpha :as spec]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(doseq [s [::model_id ::inventory_pool_id ::start_date ::end_date]]
  (spec/def s (comp not nil?)))

(spec/def ::reservation
  (spec/keys :req-un [::model_id ::inventory_pool_id ::start_date ::end_date]))

(defn columns [tx]
  (as-> (database/columns tx "reservations") <>
    (remove #{:created_at :updated_at :start_date :end_date} <>)
    (conj <>
          (helpers/date-time-created-at)
          (helpers/date-time-updated-at)
          (helpers/date-start-date)
          (helpers/date-end-date))))

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

(defn unsubmitted-sqlmap [tx user-id]
  (-> (apply sql/select (columns tx))
      (sql/from :reservations)
      (sql/where [:and
                  [:= :status (sql/call :cast
                                        "unsubmitted"
                                        :reservation_status)]
                  [:= :user_id user-id]])))

(defn for-customer-order [tx user-id]
  (-> (unsubmitted-sqlmap tx user-id)
      sql/format
      (->> (jdbc/query tx))))

(defn available-in-respective-pools? [context reservations]
  (->> reservations
       (map #(spec/assert ::reservation %))
       (group-by #(-> %
                      (select-keys [:model_id
                                    :inventory_pool_id
                                    :start_date
                                    :end_date])
                      vals))
       (every? (fn [[[model-id pool-id start-date end-date] rs]]
                 (let [available-quantity
                       (models/available-quantity-in-date-range
                         context
                         {:inventory-pool-ids [pool-id]
                          :start-date start-date
                          :end-date end-date
                          :exclude-reservation-ids (map :id reservations)}
                         {:id model-id})]
                   (>= available-quantity (clojure.core/count rs)))))))

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
  ; TODO: `reservation_advance_days` !!!
  (if (> quantity
         (->> pool-avails (map :quantity) (apply +)))
    (throw (ex-info "The desired quantity is not available." {}))
    (loop [[{pool-quantity :quantity :as pool-avail} & remaining-pool-avails]
           (sort-by identity
                    #(let [first-comp (compare (:quantity %2)
                                               (:quantity %1))]
                       (if (= first-comp 0)
                         (compare (:name %1)
                                  (:name %2))
                         first-comp))
                    pool-avails)
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
  (if-not (models/reservable? context args {:id model-id})
    (throw
      (ex-info
        "Model either does not exist or is not reservable by the user." {})))
  (let [pools (cond->> (pools/to-reserve-from tx
                                              (:id authenticated-entity)
                                              start-date
                                              end-date)
                (seq inventory-pool-ids)
                (filter #((set inventory-pool-ids) (:id %))))]
    (if (empty? pools)
      (throw (ex-info "Not possible to reserve from any pool under given conditions."))
      (let [pool-avails (->> (availability/get-available-quantities
                             context
                             {:inventory-pool-ids (map :id pools)
                              :model-ids [model-id]
                              :start-date start-date
                              :end-date end-date}
                             nil)
                           (map (fn [el]
                                  (assoc el
                                         :name
                                         (->> pools
                                              (filter #(= (:id el) (:id %)))
                                              first
                                              :name)))))]
      (->> quantity
           (distribute pool-avails)
           (map (fn [{:keys [quantity] :as attrs}]
                  (let [row (-> attrs
                                (select-keys [:inventory_pool_id :model_id :quantity])
                                (assoc :start_date (sql/call :cast start-date :date))
                                (assoc :end_date (sql/call :cast end-date :date))
                                (assoc :quantity 1
                                       :user_id (:id authenticated-entity)
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
           flatten)))))
