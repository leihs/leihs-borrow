(ns leihs.borrow.resources.entitlement-groups
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.db :as db]
            [leihs.core.sql :as sql]))

(defn get-inventory-pool-and-model-group-ids [tx model-id pool-id]
  (-> (sql/select :*)
      (sql/from :entitlements)
      (sql/merge-join :entitlement_groups
                      [:= :entitlement_groups.id :entitlements.entitlement_group_id])
      (sql/merge-where [:= :entitlements.model_id model-id])
      (sql/merge-where [:= :entitlement_groups.inventory_pool_id pool-id])
      (sql/order-by [:entitlement_groups.name :asc])
      sql/format
      (->> (jdbc/query tx)
           (map :entitlement_group_id))))

(defn get-user-group-ids [tx user-id]
  (-> (sql/select :*)
      (sql/from :entitlement_groups_users_unified)
      (sql/merge-where [:= :user_id user-id])
      sql/format
      (->> (jdbc/query tx)
           (map :entitlement_group_id))))

(defn get-one-by-id [tx id]
  (-> (sql/select :*)
      (sql/from :entitlement_groups)
      (sql/where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(comment
 (get-inventory-pool-and-model-group-ids (db/get-ds)
                                         "804a50c1-2329-5d5b-9884-340f43833514"
                                         "8bd16d45-056d-5590-bc7f-12849f034351")
 (get-user-group-ids (db/get-ds) "c0777d74-668b-5e01-abb5-f8277baa0ea8"))
