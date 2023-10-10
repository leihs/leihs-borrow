(ns leihs.borrow.resources.models
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [camel-snake-kebab.core :as csk]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [clojure.set :as s]
            [clojure.string :as string]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.executor :as executor]
            [java-time :as jt :refer [before? local-date]]
            [leihs.borrow.graphql.connections :as connections]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.legacy-availability.core :as av]
            [leihs.borrow.resources.categories.descendents :as descendents]
            [leihs.borrow.resources.entitlements :as entitlements]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.inventory-pools.visits-restrictions
             :as
             restrict]
            [leihs.borrow.resources.legacy-availability.booking-calendar :as cal]
            [leihs.borrow.resources.models.core :refer [base-sqlmap]]
            [leihs.core.core :refer [presence]]
            [wharf.core :refer [transform-keys]])
  (:import java.time.format.DateTimeFormatter))

(defn reservable-quantity [tx model-id pool-id user-id]
  (-> (sql/select [(as-> :%sum.quantity <>
                     [:cast <> :int]
                     [:coalesce <> 0])
                   :total_quantity])
      (sql/from :models)
      (sql/join [[:raw (str "(" entitlements/all-sql ") AS ents")]]
                [:and
                 [:= :models.id :ents.model_id]
                 [:or
                  [:in
                   :ents.entitlement_group_id
                   (-> (sql/select :entitlement_group_id)
                       (sql/from :entitlement_groups_users)
                       (sql/where [:= :user_id user-id]))]
                  [:= :ents.entitlement_group_id nil]]])
      (sql/where [:= :models.id model-id])
      (sql/where [:= :ents.inventory_pool_id pool-id])
      (sql-format)
      (->> (jdbc-query tx))
      first
      :total_quantity))

(defn total-reservable-quantities
  [{{tx :tx-next} :request user-id ::target-user/id} _ {model-id :id}]
  (let [pools (pools/accessible-to-user tx user-id)]
    (map (fn [pool]
           {:inventory-pool pool
            :quantity (reservable-quantity tx model-id (:id pool) user-id)})
         pools)))

(defn join-entitlements [sqlmap user-id]
  (sql/join sqlmap
            [[:raw (str "(" entitlements/all-sql ") AS ents")]]
            [:and
             [:= :models.id :ents.model_id]
             [:= :inventory_pools.id :ents.inventory_pool_id]
             [:> :ents.quantity 0]
             [:or
              [:in
               :ents.entitlement_group_id
               (-> (sql/select :entitlement_group_id)
                   (sql/from :entitlement_groups_users)
                   (sql/where [:= :user_id user-id]))]
              [:= :ents.entitlement_group_id nil]]]))

(defn merge-reservable-conditions
  ([sqlmap user-id]
   (merge-reservable-conditions sqlmap user-id nil))
  ([sqlmap user-id pool-ids]
   (-> sqlmap
       (sql/join :items
                 [:= :models.id :items.model_id])
       (sql/join :inventory_pools
                 [:= :items.inventory_pool_id :inventory_pools.id])
       (sql/join :access_rights
                 [:= :inventory_pools.id :access_rights.inventory_pool_id])
       (join-entitlements user-id)
       (sql/where [:= :inventory_pools.is_active true])
       (sql/where [:= :access_rights.user_id user-id])
       (cond-> (seq pool-ids)
         (sql/where [:in :inventory_pools.id pool-ids]))
       (sql/where [:= :items.retired nil])
       (sql/where [:= :items.is_borrowable true])
       (sql/where [:= :items.parent_id nil]))))

(defn reservable?
  [{{tx :tx-next} :request user-id ::target-user/id :as context}
   _
   value]
  (-> base-sqlmap
      (merge-reservable-conditions user-id)
      (sql/where [:= :models.id (:id value)])
      sql-format
      (->> (jdbc-query tx))
      first
      nil?
      not))

(defn merge-search-conditions [sqlmap search-term]
  (let [terms (-> search-term
                  (string/split #"\s+")
                  (->> (map presence)
                       (filter identity)
                       (map #(str "%" % "%"))))
        field [:concat_ws
               " "
               :models.product
               :models.version
               :models.manufacturer]
        where-clauses (map #(vector (keyword "~~*") field %) terms)]
    (sql/where sqlmap (cons :and where-clauses))))

(defn merge-category-ids-conditions [sqlmap category-ids]
  (-> sqlmap
      (sql/join :model_links
                [:=
                 :models.id
                 :model_links.model_id])
      (sql/where [:in
                  :model_links.model_group_id
                  category-ids])))

(defn get-category-ids [tx category-id direct-only]
  (if direct-only
    [category-id]
    (as-> category-id <>
      (descendents/descendent-ids tx <>)
      (conj <> category-id))))

(defn available-quantity-in-date-range
  "If the available quantity was already computed through the enclosing
  resolver, then just return it. Otherwise fetch from legacy and compute."
  [{{tx :tx-next} :request user-id ::target-user/id :as context}
   {:keys [inventory-pool-ids
           start-date
           end-date
           exclude-reservation-ids] :as args}
   value]
  (or (:available-quantity-in-date-range value)
      (let [pool-ids (or (not-empty inventory-pool-ids)
                         (map :id (pools/to-reserve-from tx
                                                         user-id
                                                         start-date
                                                         end-date)))]
        (if (or (before?
                 (local-date DateTimeFormatter/ISO_LOCAL_DATE (.toString start-date))
                 (local-date))
                (empty? pool-ids))
          0
          (av/maximum-available-in-period-summed-for-groups tx
                                                            (:id value)
                                                            user-id
                                                            start-date
                                                            end-date
                                                            pool-ids
                                                            exclude-reservation-ids)))))

(defn relevant-pool-ids
  [tx user-id start-date end-date selected-pool-ids]
  (-> (pools/to-reserve-from tx user-id start-date end-date)
      (->> (map :id))
      (cond-> (not (empty? selected-pool-ids))
        (-> set
            (s/intersection (set selected-pool-ids))
            vec))))

(defn merge-available-quantities
  [models
   {{tx :tx-next} :request user-id ::target-user/id :as context}
   {:keys [start-date end-date inventory-pool-ids] :as args}
   value]
  (let [pool-ids (relevant-pool-ids tx user-id start-date end-date inventory-pool-ids)]
    (map #(assoc %
                 :available-quantity-in-date-range
                 (if (or (before?
                          (local-date DateTimeFormatter/ISO_LOCAL_DATE start-date)
                          (local-date))
                         (empty? pool-ids))
                   0
                   (av/maximum-available-in-period-summed-for-groups tx
                                                                     (:id %)
                                                                     user-id
                                                                     start-date
                                                                     end-date
                                                                     pool-ids)))
         models)))

(defn get-availability
  [{{tx :tx-next} :request user-id ::target-user/id :as context}
   {:keys [start-date end-date inventory-pool-ids exclude-reservation-ids]}
   value]
  (let [pools (pools/get-multiple context {:ids inventory-pool-ids} nil)]
    (map (fn [{pool-id :id}]
           (let [pool (pools/get-by-id (-> context :request :tx-next)
                                       pool-id)
                 avail (cal/get tx
                                start-date
                                end-date
                                pool-id
                                user-id
                                (:id value)
                                (or exclude-reservation-ids []))]
             (-> avail
                 (update :dates
                         (fn [dates-with-avail]
                           (map #(restrict/validate-date-with-avail tx % pool)
                                dates-with-avail)))
                 (assoc :inventory-pool pool))))
         pools)))

(defn from-compatibles [sqlmap value user-id pool-ids unscope-reservable]
  (-> sqlmap
      (sql/where [:in :models.id (-> (sql/select :compatible_id)
                                     (sql/from :models_compatibles)
                                     (sql/where [:= :model_id (:id value)]))])))

(defn merge-categories-conditions [sqlmap tx category-id direct-only]
  (let [category-ids (get-category-ids tx category-id direct-only)]
    (cond-> sqlmap
      (seq category-ids)
      (merge-category-ids-conditions category-ids))))

(defn get-multiple-sqlmap
  [{{tx :tx-next} :request user-id ::target-user/id :as context}
   {:keys [ids
           category-id
           limit
           offset
           direct-only
           order-by
           search-term
           unscope-reservable
           pool-ids
           is-favorited]}
   value]
  (-> base-sqlmap
      (cond-> (= (::lacinia/container-type-name context) :Model)
        (from-compatibles value user-id pool-ids unscope-reservable))
      (cond-> (or category-id (= (::lacinia/container-type-name context) :Category))
        (merge-categories-conditions tx (or category-id (:id value)) direct-only))
      (cond-> (not unscope-reservable)
        (merge-reservable-conditions user-id pool-ids))
      (cond-> (seq ids)
        (sql/where [:in :models.id ids]))
      (cond-> search-term
        (merge-search-conditions search-term))
      (cond-> (not (nil? is-favorited))
        (-> (sql/left-join :favorite_models
                           [:and
                            [:= :favorite_models.model_id :models.id]
                            [:= :favorite_models.user_id user-id]])
            (sql/where [(if is-favorited :!= :=)
                        :favorite_models.model_id
                        nil])))
      (cond-> (seq order-by)
        (-> (dissoc :order-by)
            (as-> sqlmap
              (apply sql/order-by sqlmap (helpers/treat-order-arg order-by)))))
      (cond-> limit
        (sql/limit limit))
      (cond-> offset
        (sql/offset offset))))

(defn get-favorites-sqlmap
  [{user-id ::target-user/id :as context}
   args
   value]
  (-> (get-multiple-sqlmap context
                           (assoc args :unscope-reservable true)
                           value)
      (sql/join :favorite_models
                [:and
                 [:= :models.id :favorite_models.model_id]
                 [:= :favorite_models.user_id user-id]])))

(defn favorited?
  [{{tx :tx-next} :request user-id ::target-user/id}
   _
   value]
  (-> (sql/select
       [[:exists
         (-> (sql/select true)
             (sql/from :favorite_models)
             (sql/where [:and
                         [:= :model_id (:id value)]
                         [:= :user_id user-id]]))]])
      sql-format
      (->> (jdbc-query tx))
      first
      :exists))

(defn merge-availability-if-selects-fields
  [models context _ value]
  (let [field-args (some->> context
                            executor/selections-seq2
                            (filter #(= (:name %) :Model/availableQuantityInDateRange))
                            first
                            :args
                            (transform-keys csk/->kebab-case))]
    (cond-> models
      (and (executor/selects-field? context :Model/availableQuantityInDateRange)
           (:start-date field-args) ; checking this because `selects-field?` does not respect @include directive
           (:end-date field-args)) ; checking this because `selects-field?` does not respect @include directive
      (merge-available-quantities context field-args value))))

(defn post-process [models context args value]
  (merge-availability-if-selects-fields models context args value))

(defn get-connection
  [{{tx :tx-next} :request user-id ::target-user/id :as context}
   {:keys [only-available quantity] limit :first :or {quantity 1} :as args}
   value]
  (let [conn-fn (fn [ext-args]
                  (connections/wrap get-multiple-sqlmap
                                    context
                                    (merge args ext-args)
                                    value
                                    #(post-process % context args value)))
        intervene-fn (fn [edges]
                       (filter (fn [edge]
                                 (-> edge
                                     :node
                                     :available-quantity-in-date-range
                                     (>= quantity)))
                               edges))]
    (if only-available
      (connections/intervene conn-fn intervene-fn 150 limit)
      (conn-fn {}))))

(defn get-favorites-connection [context args value]
  (connections/wrap get-favorites-sqlmap
                    context
                    args
                    value
                    #(post-process % context args value)))

(defn get-one [{{tx :tx-next} :request} {:keys [id]} value]
  (-> base-sqlmap
      (sql/where [:= :id (or id (:model-id value))])
      sql-format
      (->> (jdbc-query tx))
      first))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
