(ns leihs.borrow.resources.categories
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.borrow.resources.entitlements :as entitlements]
            [leihs.core.sql :as sql]))

(def base-sqlmap
  (-> (sql/select :model_groups.id [:model_groups.name :name])
      (sql/modifiers :distinct)
      (sql/from :model_groups)
      (sql/merge-where [:= :model_groups.type "Category"])))

(defn extend-with-reservable-models-for-user [sqlmap user-id]
  (-> sqlmap
      (sql/merge-join :model_links
                      [:= :model_groups.id :model_links.model_group_id])
      (sql/merge-join :models
                      [:= :model_links.model_id :models.id])
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

(defn extend-based-on-args [sqlmap {:keys [limit offset id],
                                    root-only :rootOnly,
                                    user-id :userId}]
  (-> sqlmap
      (cond-> root-only
        (sql/merge-where
          [:not
           [:exists
            (-> (sql/select true)
                (sql/from :model_group_links)
                (sql/merge-where [:=
                                  :model_groups.id
                                  :model_group_links.child_id]))]]))
      (cond-> user-id (extend-with-reservable-models-for-user user-id))
      (cond-> limit (sql/limit limit))
      (cond-> offset (sql/offset offset))
      (cond-> (seq id)
        (sql/merge-where [:in :model_groups.id id]))))

(defn get-multiple
  [context args value]
  (-> base-sqlmap
      (extend-based-on-args args)
      (cond-> value
        (-> (sql/select :model_groups.id
                        [(sql/call :coalesce
                                   :model_group_links.label
                                   :model_groups.name) :name])
            (sql/merge-join :model_group_links
                            [:=
                             :model_groups.id
                             :model_group_links.child_id])
            (sql/merge-where [:=
                              :model_group_links.parent_id
                              (:id value)])))
      sql/format
      (->> (jdbc/query (-> context :request :tx)))))

(defn descendent-ids [tx parent-id]
  (let [query
        (str "WITH RECURSIVE category_tree(parent_id, child_id, path) AS
                (SELECT parent_id, child_id, ARRAY[]::uuid[]
                 FROM model_group_links
                 WHERE parent_id = '" parent-id "'"
                "UNION ALL
                 SELECT mgl.parent_id,
                        mgl.child_id,
                        path || mgl.parent_id
                 FROM category_tree
                 INNER JOIN model_group_links mgl
                   ON mgl.parent_id = category_tree.child_id
                 WHERE NOT mgl.parent_id = any(path))
              SELECT DISTINCT(category_tree.child_id) AS id
              FROM category_tree")]
    (->> [query] (jdbc/query tx) (map :id))))

(comment
  (descendent-ids (leihs.core.ds/get-ds)
                  "b279bb7f-314c-55d1-a407-0de794c2c25e")
  (def tmp-query (-> base-sqlmap
                     (extend-based-on-args {:userId #uuid "c0777d74-668b-5e01-abb5-f8277baa0ea8"})
                     sql/format
                     ; first
                     (->> (jdbc/query (leihs.core.ds/get-ds))))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
