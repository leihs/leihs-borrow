-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name descendent-ids :? :*
-- :doc Get ids of all descendents of a given category-id

WITH RECURSIVE category_tree(parent_id, child_id, PATH) AS
  (SELECT parent_id,
          child_id, ARRAY[parent_id]
   FROM model_group_links
   WHERE parent_id = :category-id
   UNION ALL SELECT mgl.parent_id,
                    mgl.child_id,
                    PATH || mgl.parent_id
   FROM category_tree
   INNER JOIN model_group_links mgl ON mgl.parent_id = category_tree.child_id
   WHERE NOT mgl.child_id = any(PATH))
SELECT distinct(category_tree.child_id) AS id
FROM category_tree
