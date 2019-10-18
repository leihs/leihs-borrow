(ns leihs.borrow.resources.models
  (:refer-clojure :exclude [first])
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
  (-> (sql/select :models.id
                  [(sql/call :concat_ws
                             " "
                             :models.product
                             :models.version)
                   :name])
      (sql/modifiers :distinct)
      (sql/from :models)))

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

(defn merge-availability [models context args]
  (spec/assert (spec/keys :req-un [::availability/startDate
                                   ::availability/endDate
                                   ::availability/inventoryPoolIds])
               args)
  (map (fn [model]
         (assoc model
                :availability
                (map (fn [pool-id]
                       {:inventory-pool
                          (inventory-pools/get-one (-> context :request :tx)
                                                   pool-id)
                        :dates
                          (availability/get
                            context
                            (assoc args
                                   :inventoryPoolId pool-id
                                   :modelId (:id model))
                            nil)})
                     (:inventoryPoolIds args))))
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

(defn get-multiple-sqlmap [{{:keys [tx authenticated-entity]} :request
                        :as context}
                       {:keys [limit offset],
                        direct-only :directOnly
                        order-by :orderBy
                        search-term :searchTerm
                        :as args}
                       value]
  (-> base-sqlmap
      (cond-> (= (::lacinia/container-type-name context) :Model)
        (from-compatibles value))
      (cond-> (= (::lacinia/container-type-name context) :Category)
        (merge-categories-conditions tx value direct-only))
      (merge-reservable-conditions (:id authenticated-entity))
      (cond-> search-term
        (merge-search-conditions search-term))
      (cond-> (seq order-by)
        (-> (sql/order-by (helpers/treat-order-arg order-by))
            (sql/merge-order-by [:name :asc])))
      (cond-> limit
        (sql/limit limit))
      (cond-> offset
        (sql/offset offset))))

(defn get-multiple [{{:keys [tx authenticated-entity]} :request
                     :as context}
                    {end-date :endDate
                     inventory-pool-ids :inventoryPoolIds
                     start-date :startDate
                     :as args}
                    value]
  (-> (get-multiple-sqlmap context args value)
      sql/format
      (->> (jdbc/query tx))
      (cond->
        (some some? [start-date end-date inventory-pool-ids])
        (merge-availability context args))))

(defn post-process [models
                    context
                    {start-date :startDate
                     end-date :endDate
                     inventory-pool-ids :inventoryPoolIds
                     :as args}
                    value]
  (cond-> models
    (some some? [start-date end-date inventory-pool-ids])
    (merge-availability context args)))

(defn get-connection [context args value]
  (connections/wrap get-multiple-sqlmap
                    context
                    args
                    value
                    post-process)) 

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
