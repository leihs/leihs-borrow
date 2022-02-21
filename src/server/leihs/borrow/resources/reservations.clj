(ns leihs.borrow.resources.reservations
  (:refer-clojure :exclude [count])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [clojure.spec.alpha :as spec]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.delegations :as delegations]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.settings :as settings]
            [leihs.borrow.time :as time]
            [leihs.core.core :refer [raise]]
            [leihs.core.database.helpers :as database]
            [leihs.core.sql :as sql]))

(doseq [s [::inventory_pool_id ::start_date ::end_date]]
  (spec/def s (comp not nil?)))

(spec/def ::reservation
  (spec/keys :req-un [::model_id
                      ::option_id
                      ::item_id
                      ::inventory_pool_id
                      ::start_date
                      ::end_date]))

(defn columns [tx]
  (as-> (database/columns tx "reservations") <>
    (remove #{:created_at :updated_at :start_date :end_date} <>)
    (conj <>
          (helpers/date-time-created-at :reservations)
          (helpers/date-time-updated-at :reservations)
          (helpers/date-start-date :reservations)
          (helpers/date-end-date :reservations))))

(defn get-by-ids [tx ids]
  (-> (apply sql/select (columns tx))
      (sql/from :reservations)
      (sql/where [:in :id ids])
      sql/format
      (->> (jdbc/query tx))))

(defn query [sql-format tx]
  (jdbc/query tx
              sql-format
              {:row-fn 
               #(cond-> %
                  (:status %)
                  (update :status clojure.string/upper-case))}))

(defn count [tx model-id]
  (-> (sql/select :%count.*)
      (sql/from :reservations)
      (sql/where [:= :reservations.model_id model-id])
      sql/format
      (query tx)
      first
      :count))

(defn updated-at [tx model-id]
  (-> (sql/select :reservations.updated_at)
      (sql/from :reservations)
      (sql/where [:= :model_id model-id])
      (sql/order-by [:reservations.updated_at :desc])
      (sql/limit 1)
      sql/format
      (query tx)
      first
      :updated_at))

(defn base-sqlmap [tx user-id]
  (-> (apply sql/select (columns tx))
      (sql/from :reservations)
      (sql/merge-where [:= :reservations.user_id user-id])))

(defn unsubmitted-sqlmap [tx user-id]
  (-> (base-sqlmap tx user-id)
      (sql/merge-where [:= :reservations.status "unsubmitted"])))

(defn draft-sqlmap [tx user-id]
  (-> (base-sqlmap tx user-id)
      (sql/merge-where [:= :reservations.status "draft"])))

(defn unsubmitted-and-draft-sqlmap [tx user-id]
  (-> (base-sqlmap tx user-id)
      (sql/merge-where [:in :reservations.status ["unsubmitted" "draft"]])))

(defn unsubmitted [tx user-id]
  (-> (unsubmitted-sqlmap tx user-id)
      sql/format
      (query tx)))

(defn complies-with-max-reservation-time? [tx r]
  (-> (sql/select
       [(sql/raw (format "CASE
                          WHEN maximum_reservation_time IS NOT NULL
                          THEN '%s'::date - '%s'::date <= maximum_reservation_time
                          ELSE TRUE
                          END"
                         (:end_date r) (:start_date r)))
        :result])
      (sql/from :settings)
      sql/format
      (->> (jdbc/query tx))
      first
      :result))

(defn broken [tx user-id]
  (let [brs (-> (unsubmitted-sqlmap tx user-id)
                sql/format
                (query tx))]
    (reduce (fn [memo r]
              (cond-> memo
                (not (and (->> (pools/to-reserve-from tx
                                                      user-id
                                                      (:start_date r)
                                                      (:end_date r))
                               (map :id)
                               (some #{(:inventory_pool_id r)}))
                          (complies-with-max-reservation-time? tx r)))
                (conj r)))
            []
            brs)))

(defn with-invalid-availability
  [{{:keys [tx]} :request :as context} reservations]
  (->> reservations
       (map #(spec/assert ::reservation %))
       (group-by #(-> %
                      (select-keys [:model_id
                                    :inventory_pool_id
                                    :start_date
                                    :end_date])
                      vals))
       (reduce (fn [invalid-rs
                    [[model-id pool-id start-date end-date] rs]]
                 (let [available-quantity
                       (models/available-quantity-in-date-range
                         context
                         {:inventory-pool-ids [pool-id]
                          :start-date start-date
                          :end-date end-date
                          :exclude-reservation-ids (->> reservations (map :id))}
                         {:id model-id})]
                   (cond-> invalid-rs
                     (< available-quantity (clojure.core/count rs))
                     (into rs))))
               [])))

(defn merge-where-invalid-start-date [sqlmap]
  (sql/merge-where
    sqlmap
    [:<
     :reservations.start_date 
     (sql/raw
       (str "CURRENT_DATE"
            " + "
            "MAKE_INTERVAL("
            "days => COALESCE(workdays.reservation_advance_days, 0)"
            ")"))]))

(defn unsubmitted-with-invalid-start-date
  [{{:keys [tx]} :request user-id ::target-user/id :as context}]
  (-> (unsubmitted-sqlmap tx user-id)
      (sql/merge-join :inventory_pools
                      [:=
                       :reservations.inventory_pool_id
                       :inventory_pools.id])
      pools/with-workdays-sqlmap
      merge-where-invalid-start-date
      sql/format
      (query tx)))

(defn unsubmitted-with-invalid-availability
  [{{:keys [tx]} :request user-id ::target-user/id :as context}]
  (->> user-id
       (unsubmitted tx)
       (with-invalid-availability context)))

(defn valid-until-sql [tx]
  [(sql/call :to_char
             (sql/raw
               (str "updated_at + interval '"
                    (:timeout_minutes (settings/get tx))
                    " minutes'"))
             helpers/date-time-format)
   :updated_at])

(defn touch! [tx ids]
  (-> (sql/update :reservations)
      (sql/set {:updated_at (time/now tx)})
      (sql/where [:in :id ids])
      (sql/returning (valid-until-sql tx))
      sql/format
      (query tx)
      first
      :updated_at))

(defn unsubmitted->draft [tx ids]
  (-> (sql/update :reservations)
      (sql/set {:status "draft"})
      (sql/where [:in :id ids])
      sql/format
      (->> (jdbc/execute! tx))))

(defn draft->unsubmitted [tx user-id]
  (-> (sql/update :reservations)
      (sql/set {:status "unsubmitted"})
      (sql/merge-where [:= :status "draft"])
      (sql/merge-where [:= :user_id user-id])
      sql/format
      (->> (jdbc/execute! tx))))

(defn get-drafts
  ([tx user-id] (get-drafts tx user-id nil))
  ([tx user-id ids]
   (-> (draft-sqlmap tx user-id)
       (cond-> ids (sql/merge-where [:in :id ids]))
       sql/format
       (query tx))))

(defn merge-where-according-to-container
  [sqlmap container {:keys [id] :as value}]
  (case container
    :PoolOrder
    (sql/merge-where sqlmap [:= :reservations.order_id id])
    :Rental
    (-> sqlmap
        (sql/merge-where ["<@"
                          (sql/raw "ARRAY[reservations.id]")
                          (->> value
                               :reservation-ids
                               (map #(sql/call :cast % :uuid))
                               sql/array)]))
    :Contract
    (sql/merge-where sqlmap [:= :reservations.contract_id id])
    (:User :UnsubmittedOrder)
    (sql/merge-where sqlmap [:in :reservations.status ["unsubmitted" "draft"]])
    :DraftOrder
    (sql/merge-where sqlmap [:= :reservations.status "draft"])
    sqlmap))

(defn get-multiple
  [{{:keys [tx]} :request
    container ::lacinia/container-type-name
    target-user-id ::target-user/id
    :as context}
   {:keys [order-by]}
   {:keys [user-id] :as value}]
  (-> (base-sqlmap tx (or user-id target-user-id))
      (merge-where-according-to-container container value)
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by :reservations)))
      sql/format
      (query tx)))

(defn delete [{{:keys [tx]} :request user-id ::target-user/id}
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
      (query tx)
      (->> (map :id))))

(defn distribute [pool-avails quantity]
  (if (> quantity
         (->> pool-avails (map :quantity) (apply +)))
    (raise "The desired quantity is not available.")
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

(defn create-draft
  [{{:keys [tx]} :request user-id ::target-user/id :as context}
   {:keys [model-id start-date end-date quantity inventory-pool-id] :as args}
   _]
  (when-not (models/reservable? context args {:id model-id})
    (raise "Model either does not exist or is not reservable by the user."))
  (let [row {:inventory_pool_id inventory-pool-id
             :model_id model-id
             :start_date (sql/call :cast start-date :date)
             :end_date (sql/call :cast end-date :date)
             :quantity 1
             :user_id user-id
             :status "draft"
             :created_at (time/now tx)
             :updated_at (time/now tx)}]
    (-> (sql/insert-into :reservations)
        (sql/values (->> row
                         repeat
                         (take quantity)))
        (assoc :returning (columns tx))
        sql/format
        (query tx))))

#_(defn unsubmitted-for-affiliated-user-exists?
    "Returns true if there exists some reservation created either
  for oneself or for one's delegation which is different from `user-id`.
  It means that there is already some shopping cart open for such a user.
  A user can have only one shopping cart open: either for oneself or for
  one's delegation."
    [tx user-id auth-user-id]
    (let [d-ids (->> auth-user-id
                     (delegations/get-multiple-by-user-id tx)
                     (map :id))]
      (-> (sql/select {:exists
                       (-> (sql/select true)
                           (sql/from :reservations)
                           (sql/where [:= :status "unsubmitted"])
                           (sql/merge-where (cond
                                              (and (= user-id auth-user-id) (not (empty? d-ids)))
                                              [:in :user_id d-ids]
                                              (and (= user-id auth-user-id) (empty? d-ids))
                                              false
                                              (some #{user-id} d-ids)
                                              [:= :user_id auth-user-id]
                                              :else (raise "No condition met."))))})
          sql/format
          (->> (jdbc/query tx))
          first
          :exists)))

(defn create
  [{{:keys [tx] {auth-user-id :id} :authenticated-entity} :request
    user-id ::target-user/id
    :as context}
   {:keys [model-id start-date end-date quantity]
    [pool-id] :inventory-pool-ids
    exclude-reservation-ids :exclude-reservation-ids 
    :or {exclude-reservation-ids []}
    :as args}
   _]
  (when-not (models/reservable? context args {:id model-id})
    (raise "Model either does not exist or is not reservable by the user."))
  #_(when (unsubmitted-for-affiliated-user-exists? tx user-id auth-user-id)
      (raise "There already exists an unsubmitted reservation for another user."))
  (when-not (->> (pools/to-reserve-from tx user-id start-date end-date)
                 (map :id)
                 (some #{pool-id}))
    (raise "Not allowed to create reservation in this pool."))
  (let [available-quantity (models/available-quantity-in-date-range
                            context
                            {:inventory-pool-ids [pool-id]
                             :start-date start-date
                             :end-date end-date
                             :exclude-reservation-ids exclude-reservation-ids}
                            {:id model-id})]
    (when (< available-quantity quantity)
      (raise "Desired quantity is not available anymore.")))
  (let [row (-> {:inventory_pool_id pool-id, :model_id model-id}
                (assoc :start_date (sql/call :cast start-date :date))
                (assoc :end_date (sql/call :cast end-date :date))
                (assoc :quantity 1
                       :user_id user-id
                       :delegated_user_id (when (not= auth-user-id user-id) auth-user-id)
                       :status "unsubmitted"
                       :created_at (time/now tx)
                       :updated_at (time/now tx)))
        created-rs (-> (sql/insert-into :reservations)
                       (sql/values (->> row repeat (take quantity)))
                       (assoc :returning (columns tx))
                       sql/format
                       (query tx))]
    (when-some [broken-rs (not-empty (broken tx user-id))]
      (when (-> (->> created-rs (map :id) set)
                (set/intersection (->> broken-rs (map :id) set))
                empty? not)
        (raise "Reservation could not be created due to broken conditions.")))
    created-rs))

(defn add-to-cart
  [{{:keys [tx]} :request user-id ::target-user/id :as context}
   {:keys [ids]}
   _]
  (let [drafts (get-drafts tx user-id ids)]
    (if (empty? drafts)
      (raise "No draft reservations found to process."))
    (if-not (empty? (with-invalid-availability context drafts))
      (raise "Some reserved quantities are not available anymore."))
    (-> (sql/update :reservations)
        (sql/set {:status "unsubmitted", :updated_at (time/now tx)})
        (sql/where [:in :id (map :id drafts)])
        (sql/returning :*)
        sql/format
        (query tx))))
