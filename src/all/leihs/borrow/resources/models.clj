(ns leihs.borrow.resources.models
  (:require [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.executor :as executor]
            [wharf.core :refer [transform-keys]]
            [camel-snake-kebab.core :as csk]
            [java-time :refer [interval local-date before?] :as jt]
            [logbug.debug :as debug]
            [leihs.core.sql :as sql]
            [leihs.borrow.connections :refer [row-cursor cursored-sqlmap] :as connections]
            [leihs.borrow.resources.availability :as availability]
            [leihs.borrow.resources.entitlements :as entitlements]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.categories.descendents :as descendents])
  (:import (java.time.format DateTimeFormatter)))

(def base-sqlmap
  (-> (sql/select :models.*
                  [(sql/call :concat_ws
                             " "
                             :models.product
                             :models.version)
                   :name])
      (sql/modifiers :distinct)
      (sql/from :models)
      (sql/order-by [:name :asc])))

(defn merge-reservable-conditions [sqlmap user-id]
  (-> sqlmap
      (sql/merge-join :items
                      [:= :models.id :items.model_id])
      (sql/merge-join :inventory_pools
                      [:= :items.inventory_pool_id :inventory_pools.id])
      (sql/merge-join :access_rights
                      [:= :inventory_pools.id :access_rights.inventory_pool_id])
      (sql/merge-join (sql/raw (str "(" entitlements/all-sql ") AS pwg"))
                      [:and
                       [:= :models.id :pwg.model_id]
                       [:= :inventory_pools.id :pwg.inventory_pool_id]
                       [:> :pwg.quantity 0]
                       [:or
                        [:in
                         :pwg.entitlement_group_id
                         (-> (sql/select :entitlement_group_id)
                             (sql/from :entitlement_groups_users)
                             (sql/merge-where [:= :user_id user-id]))]
                        [:= :pwg.entitlement_group_id nil]]])
      (sql/merge-where [:= :inventory_pools.is_active true])
      (sql/merge-where [:= :access_rights.user_id user-id])
      (sql/merge-where [:= :items.retired nil])
      (sql/merge-where [:= :items.is_borrowable true])
      (sql/merge-where [:= :items.parent_id nil])))

(defn reservable?
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request :as context}
   _
   value]
  (-> base-sqlmap
      (merge-reservable-conditions user-id)
      (sql/merge-where [:= :models.id (:id value)])
      sql/format
      (->> (jdbc/query tx))
      first
      nil?
      not))

(defn merge-search-conditions [sqlmap search-term]
  (sql/merge-where sqlmap ["~~*"
                           (sql/call :concat_ws
                                     " "
                                     :models.product
                                     :models.version
                                     :models.manufacturer)
                           (str "%" search-term "%")]))

(defn merge-category-ids-conditions [sqlmap category-ids]
  (-> sqlmap
      (sql/merge-join :model_links
                      [:=
                       :models.id
                       :model_links.model_id])
      (sql/merge-where [:in
                        :model_links.model_group_id
                        category-ids])))

(defn get-category-ids [tx value direct-only]
  (if-let [value-id (:id value)]
    (if direct-only
      [value-id]
      (as-> value-id <>
        (descendents/descendent-ids tx <>)
        (conj <> value-id)))))

(defn available-quantity-in-date-range
  "If the available quantity was already computed through the enclosing
  resolver, then just return it. Otherwise fetch from legacy and compute."
  [{{tx :tx {user-id :id} :authenticated-entity} :request :as context}
   {:keys [inventory-pool-ids start-date end-date exclude-reservation-ids] :as args}
   value]
  (or (:available-quantity-in-date-range value)
      (if (before?
            (local-date DateTimeFormatter/ISO_LOCAL_DATE start-date)
            (local-date))
        0
        (let [pool-ids (or (not-empty inventory-pool-ids)
                           (map :id (pools/to-reserve-from tx
                                                           user-id
                                                           start-date
                                                           end-date)))
              legacy-response (availability/get-available-quantities
                                context
                                {:model-ids [(:id value)]
                                 :inventory-pool-ids pool-ids
                                 :start-date start-date
                                 :end-date end-date
                                 :exclude-reservation-ids exclude-reservation-ids}
                                nil)]
          (->> legacy-response
               (map :quantity)
               (apply +)
               (#(if (< % 0) 0 %)))))))

(defn merge-available-quantities
  [models
   {{tx :tx {user-id :id} :authenticated-entity} :request :as context}
   {:keys [start-date end-date]}
   value]
  (map (if (before?
             (local-date DateTimeFormatter/ISO_LOCAL_DATE start-date)
             (local-date))
         #(assoc % :available-quantity-in-date-range 0)
         (let [pool-ids (map :id (pools/to-reserve-from tx
                                                        user-id
                                                        start-date
                                                        end-date))
               legacy-response (availability/get-available-quantities
                                 context
                                 {:model-ids (map :id models)
                                  :inventory-pool-ids pool-ids
                                  :start-date start-date
                                  :end-date end-date}
                                 nil)
               model-quantitites (->> legacy-response
                                      (group-by :model_id)
                                      (map (fn [[model-id pool-quantities]]
                                             [model-id (->> pool-quantities
                                                            (map :quantity)
                                                            (apply +)
                                                            (#(if (< % 0) 0 %)))]))
                                      (into {}))]
           #(assoc %
                   :available-quantity-in-date-range
                   (-> % :id str model-quantitites))))
       models))

(defn get-availability
  [{{tx :tx {user-id :id} :authenticated-entity} :request :as context}
   {:keys [start-date end-date inventory-pool-ids]}
   value]
  ; TODO: `reservation_advance_days` ... ??
  (let [pool-ids (or inventory-pool-ids
                     (map :id
                          (pools/to-reserve-from tx
                                                 user-id
                                                 start-date
                                                 end-date)))
        past-date? #(before? (local-date %) (local-date))
        too-early-date? (fn [date pool]
                          (and (:reservation_advance_days pool)
                               (< (jt/time-between (local-date)
                                                   (local-date date)
                                                   :days)
                                  (:reservation_advance_days pool))))]
    (map (fn [pool-id]
           (let [pool (pools/get-by-id (-> context :request :tx)
                                       pool-id)
                 avail (availability/get
                         context
                         {:start-date start-date
                          :end-date end-date
                          :inventory-pool-id pool-id
                          :model-id (:id value)}
                         nil)]
             (-> avail
                 (update :dates
                         (fn [dates]
                           (map #(cond
                                   (past-date? (:date %))
                                   (assoc % :quantity 0 :visits-count 0)
                                   (too-early-date? (:date %) pool)
                                   (assoc % :quantity 0)
                                   :else %)
                                dates)))
                 (assoc :inventory-pool (log/spy pool)))))
         pool-ids)))

(defn from-compatibles [sqlmap value]
  (-> sqlmap
      (sql/from :models_compatibles)
      (sql/join :models [:=
                         :models.id
                         :models_compatibles.compatible_id])
      (sql/merge-where [:=
                        :models_compatibles.model_id
                        (:id value)])))

(defn merge-categories-conditions [sqlmap tx value direct-only]
  (let [category-ids (get-category-ids tx value direct-only)]
    (cond-> sqlmap
      (seq category-ids)
      (merge-category-ids-conditions category-ids))))

(defn get-multiple-sqlmap
  [{{:keys [tx authenticated-entity]} :request :as context}
   {:keys [ids
           limit
           offset
           direct-only
           order-by
           search-term
           unscope-reservable
           is-favorited]}
   value]
  (-> base-sqlmap
      (cond-> (= (::lacinia/container-type-name context) :Model)
        (from-compatibles value))
      (cond-> (= (::lacinia/container-type-name context) :Category)
        (merge-categories-conditions tx value direct-only))
      (cond-> (not unscope-reservable)
        (merge-reservable-conditions (:id authenticated-entity)))
      (cond-> (seq ids)
        (sql/merge-where [:in :models.id ids]))
      (cond-> search-term
        (merge-search-conditions search-term))
      (cond-> (not (nil? is-favorited))
        (-> (sql/merge-left-join :favorite_models
                                 [:= :favorite_models.model_id :models.id])
            (sql/merge-where [(if is-favorited :!= :=)
                              :favorite_models.model_id
                              nil])))
      (cond-> (seq order-by)
        (-> (sql/order-by (helpers/treat-order-arg order-by))
            (sql/merge-order-by [:name :asc])))
      (cond-> limit
        (sql/limit limit))
      (cond-> offset
        (sql/offset offset))))

(defn get-favorites-sqlmap
  [{{{user-id :id} :authenticated-entity} :request :as context}
   args
   value]
  (-> (get-multiple-sqlmap context
                           (assoc args :unscope-reservable true)
                           value)
      (sql/merge-join :favorite_models
                      [:and
                       [:= :models.id :favorite_models.model_id]
                       [:= :favorite_models.user_id user-id]])))

(defn favorited?
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request}
   _
   value]
  (-> (sql/select
        (sql/call :exists
                  (-> (sql/select true)
                      (sql/from :favorite_models)
                      (sql/where [:and
                                  [:= :model_id (:id value)]
                                  [:= :user_id user-id]]))))
      sql/format
      (->> (jdbc/query tx))
      first
      :exists))

(defn merge-availability-if-selects-fields
  [models context args value]
  (cond-> models
    (executor/selects-field? context :Model/availableQuantityInDateRange)
    (merge-available-quantities
      context
      (->> context
           executor/selections-seq2
           (filter #(= (:name %) :Model/availableQuantityInDateRange))
           first
           :args
           (transform-keys csk/->kebab-case))
      value)))


(defn post-process [models context args value]
  (merge-availability-if-selects-fields models context args value))

(defn get-connection [context args value]
  (connections/wrap get-multiple-sqlmap
                    context
                    args
                    value
                    #(post-process % context args value)))

(defn get-favorites-connection [context args value]
  (connections/wrap get-favorites-sqlmap
                    context
                    args
                    value
                    #(post-process % context args value)))

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one [{{:keys [tx]} :request} {:keys [id]} value]
  (-> base-sqlmap
    (sql/where [:= :id (or id (:model-id value))])
      sql/format
      (->> (jdbc/query tx))
      first))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
