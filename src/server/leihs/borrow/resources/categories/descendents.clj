(ns leihs.borrow.resources.categories.descendents
  (:require [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(defn descendent-ids [tx parent-id]
  (assert (uuid? parent-id))
  (let [query
        (str "WITH RECURSIVE category_tree(parent_id, child_id, path) AS
                (SELECT parent_id, child_id, ARRAY[parent_id]
                 FROM model_group_links
                 WHERE parent_id = '" parent-id "' "
                "UNION ALL
                 SELECT mgl.parent_id,
                        mgl.child_id,
                        path || mgl.parent_id
                 FROM category_tree
                 INNER JOIN model_group_links mgl
                   ON mgl.parent_id = category_tree.child_id
                 WHERE NOT mgl.child_id = ANY(path))
              SELECT DISTINCT(category_tree.child_id) AS id
              FROM category_tree")]
    (->> [query] (jdbc-query tx) (map :id))))

(comment
  (descendent-ids (leihs.core.db/get-ds-next)
                  "9a1dc177-a2b2-4a16-8fbf-6552b5313f38"))
