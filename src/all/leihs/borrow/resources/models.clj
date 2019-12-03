(ns leihs.borrow.resources.models
  (:require [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.executor :as executor]
            [leihs.core.sql :as sql]
            [leihs.borrow.connections :refer [row-cursor cursored-sqlmap] :as connections]
            [leihs.borrow.resources.availability :as availability]
            [leihs.borrow.resources.entitlements :as entitlements]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.inventory-pools :as inventory-pools]
            [leihs.borrow.resources.categories.descendents :as descendents]))

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
      (sql/merge-where [:= :access_rights.deleted_at nil])
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

(defn merge-available-quantities
  [models context {:keys [start-date end-date]} value]
  (let [pool-ids (->> (inventory-pools/get-multiple context {} nil)
                      (map :id))
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
    (map #(assoc %
                 :available-quantity-in-date-range
                 (-> % :id str model-quantitites))
         models)))

(defn merge-availability [models context args _]
  (spec/assert (spec/keys :req-un [::availability/start-date
                                   ::availability/end-date
                                   #_::availability/inventory-pool-ids])
               args)
  (map (fn [model]
         (assoc model
                :availability
                (map (fn [pool-id]
                       (let [avail (availability/get
                                     context
                                     (assoc args
                                            :inventory-pool-id pool-id
                                            :model-id (:id model))
                                     nil)]
                         (assoc avail
                                :inventory-pool
                                (inventory-pools/get-by-id
                                  (-> context :request :tx)
                                  pool-id))))
                     (:inventory-pool-ids args))))
       models))

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
   {:keys [ids limit offset direct-only order-by search-term unscope-reservable]}
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

(defn merge-availability-if-selects-fields [models context args value]
  (cond-> models
    (executor/selects-field? context :Model/availability)
    (merge-availability context args value)
    (executor/selects-field? context :Model/availableQuantityInDateRange)
    (merge-available-quantities context args value)))

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
