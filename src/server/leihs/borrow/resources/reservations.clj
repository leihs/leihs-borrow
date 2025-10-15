(ns leihs.borrow.resources.reservations
  (:refer-clojure :exclude [count])
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [clojure.set :as set]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.delegations :as delegations]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.workdays :as workdays]
            [leihs.borrow.time :as time]
            [leihs.core.core :refer [raise presence]]
            [leihs.core.db :as db]
            [leihs.borrow.database.helpers :as database]
            [leihs.core.settings :refer [settings]]
            [taoensso.timbre :refer [debug info warn error spy]]))

(doseq [s [::inventory_pool_id ::start_date ::end_date]]
  (spec/def s (comp not nil?)))

(spec/def ::reservation
  (spec/keys :req-un [::model_id
                      ::option_id
                      ::item_id
                      ::inventory_pool_id
                      ::start_date
                      ::end_date]))

(def columns
  [:reservations.* [[:coalesce :returned_date :end_date] :actual_end_date]])

(defn get-by-ids [tx ids]
  (-> (apply sql/select columns)
      (sql/from :reservations)
      (sql/where [:in :id ids])
      sql-format
      (->> (jdbc-query tx))))

(defn query [sql-format tx]
  (->> (jdbc-query tx sql-format)
       (map #(cond-> %
               (:status %)
               (update :status clojure.string/upper-case)))))

(defn count [tx model-id]
  (-> (sql/select :%count.*)
      (sql/from :reservations)
      (sql/where [:= :reservations.model_id model-id])
      sql-format
      (query tx)
      first
      :count))

(defn updated-at [tx model-id]
  (-> (sql/select :reservations.updated_at)
      (sql/from :reservations)
      (sql/where [:= :model_id model-id])
      (sql/order-by [:reservations.updated_at :desc])
      (sql/limit 1)
      sql-format
      (query tx)
      first
      :updated_at))

(defn base-sqlmap [tx user-id]
  (-> (apply sql/select columns)
      (sql/from :reservations)
      (sql/where [:= :reservations.user_id user-id])))

(comment (require '[java-time])
         (import [java.util UUID]
                 [java.time.format DateTimeFormatter]
                 [java.time ZoneOffset ZoneId])
         (-> (base-sqlmap scratch/tx scratch/user-id)
             sql-format
             (->> (jdbc-query scratch/tx))
             first
             :updated_at
             .toLocalDateTime
             (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss'Z'")))
         (-> (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")
             (.withZone (ZoneId/systemDefault)))
         (ZoneId/getAvailableZoneIds)
         (ZoneId/of "Europe/Paris")
         (java-time/instant))

(defn unsubmitted-sqlmap [tx user-id]
  (-> (base-sqlmap tx user-id)
      (sql/where [:= :reservations.status "unsubmitted"])))

(defn draft-sqlmap [tx user-id]
  (-> (base-sqlmap tx user-id)
      (sql/where [:= :reservations.status "draft"])))

(defn unsubmitted-and-draft-sqlmap [tx user-id]
  (-> (base-sqlmap tx user-id)
      (sql/where [:in :reservations.status ["unsubmitted" "draft"]])))

(defn unsubmitted [tx user-id]
  (-> (unsubmitted-sqlmap tx user-id)
      sql-format
      (query tx)))

(defn complies-with-max-reservation-time? [tx r]
  (-> (sql/select
       [[:raw (format "CASE
                         WHEN borrow_maximum_reservation_duration IS NOT NULL
                         THEN '%s'::date - '%s'::date <= borrow_maximum_reservation_duration
                         ELSE TRUE
                       END"
                      (:end_date r) (:start_date r))]
        :result])
      (sql/from :inventory_pools)
      (sql/where [:= :inventory_pools.id (:inventory_pool_id r)])
      sql-format
      (->> (jdbc-query tx))
      first
      :result))

(defn broken
  ([tx user-id]
   (broken tx user-id (-> (unsubmitted-sqlmap tx user-id)
                          sql-format
                          (query tx))))
  ([tx user-id rs]
   (let [start-end->pool-ids
         (->> rs
              (group-by #(-> % (select-keys [:start_date :end_date]) vals))
              (map (fn [[[start end :as start-end] _]]
                     [start-end (map :id (pools/to-reserve-from tx user-id start end))])))]
     (reduce (fn [brs r]
               (cond-> brs
                 (or (let [pool-ids (->> start-end->pool-ids
                                         (filter #(= (first %) [(:start_date r) (:end_date r)]))
                                         first
                                         second)]
                       (not (some #{(:inventory_pool_id r)} pool-ids)))
                     (not (complies-with-max-reservation-time? tx r)))
                 (conj r)))
             []
             rs))))

(defn with-invalid-availability
  [{{tx :tx} :request :as context} reservations]
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
                         :exclude-reservation-ids (->> rs (map :id))}
                        {:id model-id})]
                   (cond-> invalid-rs
                     (< available-quantity (clojure.core/count rs))
                     (into rs))))
               [])))

(defn merge-where-invalid-start-date [sqlmap]
  (sql/where
   sqlmap
   [:<
    :reservations.start_date
    [:raw
     (str "CURRENT_DATE"
          " + "
          "MAKE_INTERVAL("
          "days => COALESCE(inventory_pools.borrow_reservation_advance_days, 0)"
          ")")]]))

(defn unsubmitted-with-invalid-start-date
  [{{tx :tx} :request user-id ::target-user/id :as context}]
  (-> (unsubmitted-sqlmap tx user-id)
      (sql/join :inventory_pools
                [:=
                 :reservations.inventory_pool_id
                 :inventory_pools.id])
      workdays/with-workdays-sqlmap
      merge-where-invalid-start-date
      sql-format
      (query tx)))

(defn unsubmitted-with-invalid-availability
  [{{tx :tx} :request user-id ::target-user/id :as context}]
  (->> user-id
       (unsubmitted tx)
       (with-invalid-availability context)))

(defn valid-until-sql [tx]
  [[:raw (str "reservations.updated_at + interval '"
              (:timeout_minutes (settings tx [:timeout_minutes]))
              " minutes'"
              " AS updated_at")]])

(defn touch! [tx ids]
  (-> (sql/update :reservations)
      (sql/set {:updated_at (time/now tx)})
      (sql/where [:in :id ids])
      (sql/returning (valid-until-sql tx))
      sql-format
      (query tx)
      first
      :updated_at))

(defn unsubmitted->draft [tx ids]
  (-> (sql/update :reservations)
      (sql/set {:status "draft"})
      (sql/where [:in :id ids])
      sql-format
      (->> (jdbc/execute! tx))))

(defn draft->unsubmitted [tx user-id]
  (-> (sql/update :reservations)
      (sql/set {:status "unsubmitted"})
      (sql/where [:= :status "draft"])
      (sql/where [:= :user_id user-id])
      sql-format
      (->> (jdbc/execute! tx))))

(defn get-drafts
  ([tx user-id] (get-drafts tx user-id nil))
  ([tx user-id ids]
   (-> (draft-sqlmap tx user-id)
       (cond-> ids (sql/where [:in :id ids]))
       sql-format
       (query tx))))

(defn merge-where-according-to-container
  [sqlmap container {:keys [id] :as value}]
  (case container
    :PoolOrder
    (sql/where sqlmap [:= :reservations.order_id id])
    :Rental
    (-> sqlmap
        (sql/where [(keyword "<@")
                    [:array [:reservations.id]]
                    [:array (map #(vector :cast % :uuid) (:reservation-ids value))]]))
    :Contract
    (sql/where sqlmap [:= :reservations.contract_id id])
    (:User :UnsubmittedOrder)
    (sql/where sqlmap [:in :reservations.status ["unsubmitted" "draft"]])
    :DraftOrder
    (sql/where sqlmap [:= :reservations.status "draft"])
    sqlmap))

(defn merge-search-term [sqlmap search-term]
  (let [terms (-> search-term
                  (string/split #"\s+")
                  (->> (map presence)
                       (filter identity)
                       (map #(str "%" % "%"))))
        field [:concat_ws
               " "
               :items.id :items.inventory_code :items.serial_number
               :models.id :models.product :models.version :models.manufacturer
               :options.id :options.product :options.version :options.manufacturer :options.inventory_code
               :customer_orders.title :customer_orders.purpose
               :contracts.purpose :contracts.note :contracts.compact_id]
        where-clauses (map #(vector (keyword "~~*") field %) terms)]
    (sql/where sqlmap (cons :and where-clauses))))

(defn get-multiple
  [{{tx :tx} :request
    container ::lacinia/container-type-name
    target-user-id ::target-user/id
    :as context}
   {:keys [meta-state from until pool-ids search-term order-by]}
   {:keys [user-id] :as value}]
  (-> (base-sqlmap tx (or user-id target-user-id))
      (cond-> search-term
        (->
         (sql/left-join :items [:= :reservations.item_id :items.id])
         (sql/left-join :models [:= :reservations.model_id :models.id])
         (sql/left-join :options [:= :reservations.option_id :options.id])
         (sql/left-join :orders [:= :reservations.order_id :orders.id])
         (sql/left-join :customer_orders [:= :orders.customer_order_id :customer_orders.id])
         (sql/left-join :contracts [:= :reservations.contract_id :contracts.id])))
      (merge-where-according-to-container container value)
      (cond->
       (= meta-state :CURRENT_LENDING)
        (sql/where [:or
                    [:= :reservations.status "signed"]
                    [:and [:= :reservations.status "approved"] [:<= [:raw "CURRENT_DATE"] :reservations.end_date]]])

        (or from until)
        (sql/where
         [:raw
          (format
           "(reservations.start_date, reservations.end_date) OVERLAPS (%s, %s)"
           (or (some->> from (format "'%s'")) "'1900-01-01'::date")
           (or (some->> until (format "'%s'")) "'9999-12-31'::date"))])

        (seq pool-ids)
        (sql/where [:in :reservations.inventory_pool_id pool-ids])

        search-term
        (merge-search-term search-term)

        (seq order-by)
        (as-> sqlmap
              (apply sql/order-by sqlmap (helpers/treat-order-arg order-by :reservations))))
      sql-format
      (query tx)))

(comment
  (let [context {:request {:tx (leihs.core.db/get-ds)}
                 ::target-user/id #uuid "23a3d006-581e-4dee-a717-4ac7d0fd7ab7"}
        args {:search-term "beamer"}
        container nil]
    (->> (get-multiple context args container))))

(defn delete [{{tx :tx} :request user-id ::target-user/id}
              {:keys [ids]}
              _]
  (-> (sql/delete-from :reservations)
      (sql/where [:in :reservations.id ids])
      (sql/where
       [:or
        [:= :reservations.user_id user-id]
        [:exists (-> (sql/select true)
                     (sql/from [:delegations_users :du])
                     (sql/where [:= :du.user_id user-id])
                     (sql/where [:= :du.delegation_id :reservations.user_id]))]])
      (sql/returning :id)
      sql-format
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

(defn create-optimistic
  "Creates a reservation without checking availability (as `create` does). However the model must exist and be reservable for the user."
  [{{tx :tx} :request user-id ::target-user/id :as context}
   {:keys [model-id start-date end-date quantity inventory-pool-id] :as args}
   _]
  (when-not (models/reservable? context args {:id model-id})
    (raise "Model either does not exist or is not reservable by the user."))
  (let [row {:inventory_pool_id inventory-pool-id
             :model_id model-id
             :start_date [:cast start-date :date]
             :end_date [:cast end-date :date]
             :quantity 1
             :user_id user-id
             :status "unsubmitted"
             :created_at (time/now tx)
             :updated_at (time/now tx)}]
    (-> (sql/insert-into :reservations)
        (sql/values (->> row
                         repeat
                         (take quantity)))
        (as-> <> (apply sql/returning <> columns))
        sql-format
        (query tx))))

(defn create
  [{{tx :tx {auth-user-id :id} :authenticated-entity} :request
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
                (assoc :start_date [:cast start-date :date])
                (assoc :end_date [:cast end-date :date])
                (assoc :quantity 1
                       :user_id user-id
                       :delegated_user_id (when (not= auth-user-id user-id) auth-user-id)
                       :status "unsubmitted"
                       :created_at (time/now tx)
                       :updated_at (time/now tx)))
        created-rs (-> (sql/insert-into :reservations)
                       (sql/values (->> row repeat (take quantity)))
                       (as-> <> (apply sql/returning <> columns))
                       sql-format
                       (query tx))]
    (when-some [broken-rs (not-empty (broken tx user-id))]
      (when (-> (->> created-rs (map :id) set)
                (set/intersection (->> broken-rs (map :id) set))
                empty? not)
        (raise "Reservation could not be created due to broken conditions.")))
    created-rs))

(defn add-to-cart
  [{{tx :tx} :request user-id ::target-user/id :as context}
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
        sql-format
        (query tx))))
