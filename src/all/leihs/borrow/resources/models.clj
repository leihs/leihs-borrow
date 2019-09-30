(ns leihs.borrow.resources.models
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [leihs.borrow.resources.entitlements :as entitlements]
            [leihs.borrow.resources.helpers :as helpers]
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

(defn get-multiple [context
                    {order-by :orderBy,
                     user-id :userId,
                     direct-only :directOnly}
                    value]
  (let [tx (-> context :request :tx)
        value-id (:id value)
        category-ids (if direct-only
                       [value-id]
                       (as-> value-id <>
                         (descendents/descendent-ids tx <>)
                         (conj <> value-id)))]
    (-> base-sqlmap
        (cond-> user-id (merge-reservable-conditions user-id))
        (cond-> (seq category-ids)
          (-> (sql/merge-join :model_links
                              [:=
                               :models.id
                               :model_links.model_id])
              (sql/merge-where [:in
                                :model_links.model_group_id
                                category-ids])))
        (cond-> (seq order-by)
          (-> (sql/order-by (helpers/treat-order-arg order-by))
              (sql/merge-order-by [:name :asc])))
        sql/format
        log/spy
        (->> (jdbc/query tx)))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
