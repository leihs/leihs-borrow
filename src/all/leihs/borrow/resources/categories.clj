(ns leihs.borrow.resources.categories
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn descendent-ids [tx parent-id]
  (assert (uuid? parent-id))
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

(comment (descendent-ids (leihs.core.ds/get-ds)
                         "b279bb7f-314c-55d1-a407-0de794c2c25e"))

(defn base-sqlmap [{:keys [limit offset id] root-only :rootOnly}]
  (-> (sql/select :model_groups.id :name)
      (sql/from :model_groups)
      (sql/merge-where [:= :model_groups.type "Category"])
      (cond-> root-only
        (sql/merge-where
          [:not
           [:exists
            (-> (sql/select true)
                (sql/from :model_group_links)
                (sql/merge-where [:=
                                  :model_groups.id
                                  :model_group_links.child_id]))]]))
      (cond-> limit (sql/limit limit))
      (cond-> offset (sql/offset offset))
      (cond-> (seq id)
        (sql/merge-where [:in :model_groups.id id]))))

(defn get-multiple
  [context args value]
  (-> (base-sqlmap args)
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

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
