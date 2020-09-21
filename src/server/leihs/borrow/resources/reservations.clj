(ns leihs.borrow.resources.reservations
  (:refer-clojure :exclude [count])
  (:require [leihs.borrow.time :as time]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.delegations :as delegations]
            [leihs.borrow.resources.availability :as availability]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.settings :as settings]
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
                  [:= :status "unsubmitted"]
                  [:= :user_id user-id]])))

(defn draft-sqlmap [tx user-id]
  (-> (apply sql/select (columns tx))
      (sql/from :reservations)
      (sql/where [:and
                  [:= :status "draft"]
                  [:= :user_id user-id]])))

(defn for-customer-order [tx user-id]
  (-> (unsubmitted-sqlmap tx user-id)
      sql/format
      (->> (jdbc/query tx))))

(defn with-invalid-availability [context reservations]
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
                          :exclude-reservation-ids (map :id reservations)}
                         {:id model-id})]
                   (cond-> invalid-rs
                     (< available-quantity (clojure.core/count rs))
                     (into rs))))
               [])))

(defn unsubmitted-with-invalid-availability?
  [{{:keys [tx] user-id :target-user-id} :request :as context}]
  (->> user-id
       (for-customer-order tx)
       (with-invalid-availability context)
       empty?
       not))

(defn valid-until-sql [tx]
  [(sql/call :to_char
             (sql/raw
               (str "updated_at + interval '"
                    (:timeout_minutes (settings/get tx))
                    " minutes'"))
             helpers/date-time-format)
   :updated_at])

(defn touch-unsubmitted! [tx user-id]
  (-> (sql/update :reservations)
      (sql/set {:updated_at (time/now tx)})
      (sql/where [:and
                  [:= :status "unsubmitted"]
                  [:= :user_id user-id]])
      (sql/returning (valid-until-sql tx))
      sql/format
      (->> (jdbc/query tx))
      first
      :updated_at))

(defn get-drafts [tx user-id ids]
  (-> (apply sql/select (columns tx))
      (sql/from :reservations)
      (sql/merge-where [:= :status "draft"])
      (sql/merge-where [:= :user_id user-id])
      (sql/merge-where [:in :id ids])
      sql/format
      (query tx)))

(defn get-multiple
  [{{:keys [tx] user-id :target-user-id} :request :as context}
   {:keys [order-by]}
   value]
  (-> (apply sql/select (columns tx))
      (sql/from :reservations)
      (sql/where (case (::lacinia/container-type-name context)
                   :PoolOrder [:= :order_id (:id value)]
                   :Visit [:in :id (:reservation-ids value)]
                   :CurrentUser [:and
                                 [:= :status "unsubmitted"]
                                 [:= :user_id user-id]]))
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      sql/format
      (query tx)))

(defn delete [{{:keys [tx] user-id :target-user-id} :request}
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
  [{{:keys [tx] user-id :target-user-id} :request :as context}
   {:keys [model-id start-date end-date quantity inventory-pool-id] :as args}
   _]
  (when-not (models/reservable? context args {:id model-id})
    (throw
      (ex-info
        "Model either does not exist or is not reservable by the user." {})))
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
        log/spy
        (query tx))))

(defn create
  [{{:keys [tx] user-id :target-user-id} :request :as context}
   {:keys [model-id start-date end-date quantity inventory-pool-ids]
    exclude-reservation-ids :exclude-reservation-ids 
    :or {exclude-reservation-ids []}
    :as args}
   _]
  (when-not (models/reservable? context args {:id model-id})
    (throw
      (ex-info
        "Model either does not exist or is not reservable by the user." {})))
  (let [pools (cond->> (pools/to-reserve-from tx
                                              user-id
                                              start-date
                                              end-date)
                (seq inventory-pool-ids)
                (filter #((set inventory-pool-ids) (:id %))))]
    (if (empty? pools)
      (throw (ex-info "Not possible to reserve from any pool under given conditions." {}))
      (let [pool-avails (->> (availability/get-available-quantities
                               context
                               {:inventory-pool-ids (map :id pools)
                                :model-ids [model-id]
                                :start-date start-date
                                :end-date end-date
                                :user-id user-id
                                :exclude-reservation-ids exclude-reservation-ids}
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
                                         :user_id user-id
                                         :status "unsubmitted"
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

(defn add-to-cart
  [{{:keys [tx] user-id :target-user-id} :request :as context}
   {:keys [ids]}
   _]
  (let [drafts (get-drafts tx user-id ids)]
    (if (empty? drafts)
      (throw (ex-info "No draft reservations found to process." {})))
    (if-not (empty? (with-invalid-availability context drafts))
      (throw (ex-info "Some reserved quantities are not available anymore." {})))
    (-> (sql/update :reservations)
        (sql/set {:status "unsubmitted", :updated_at (time/now tx)})
        (sql/where [:in :id (map :id drafts)])
        (sql/returning :*)
        sql/format
        (query tx))))
