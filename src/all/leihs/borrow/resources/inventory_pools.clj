(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.sql :as sql]))

(def base-sqlmap
  (-> (sql/select :inventory_pools.*)
      (sql/modifiers :distinct)
      (sql/from :inventory_pools)
      (sql/merge-where [:= :inventory_pools.is_active true])))

(defn get-multiple [context {:keys [order-by]} value]
  (let [user-id (-> value :user :id)]
    (-> base-sqlmap
        (cond-> user-id
          (-> (sql/merge-join :access_rights
                              [:=
                               :access_rights.inventory_pool_id
                               :inventory_pools.id])
              (sql/merge-where [:= :access_rights.user_id user-id])
              (sql/merge-where [:= :access_rights.deleted_at nil])))
        (cond-> (seq order-by)
          (-> (sql/order-by (helpers/treat-order-arg order-by))
              (sql/merge-order-by [:inventory_pools.name :asc])))
        sql/format
        (->> (jdbc/query (-> context :request :tx))))))

(defn get-by-id [tx id]
  (-> base-sqlmap
      (sql/merge-where [:= :inventory_pools.id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one [context _ value]
  (get-by-id (-> context :request :tx)
             (:inventory-pool-id value)))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
