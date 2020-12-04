-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name reservable-root-categories :? :*
-- :doc Get all reservable root categories by user-id and possibly limit by pool-ids

:snip:with-all-reservable-categories
SELECT model_groups.id, model_groups.name
FROM model_groups
WHERE NOT EXISTS (
  SELECT TRUE
  FROM model_group_links
  WHERE model_group_links.child_id = model_groups.id
)
AND ARRAY(
  WITH RECURSIVE category_tree(parent_id, child_id, PATH) AS
  (SELECT parent_id, child_id, ARRAY[parent_id]
    FROM model_group_links
    WHERE parent_id = model_groups.id
    UNION ALL
    SELECT mgl.parent_id, mgl.child_id, PATH || mgl.parent_id
    FROM category_tree
    INNER JOIN model_group_links mgl ON mgl.parent_id = category_tree.child_id
    WHERE NOT mgl.child_id = any(PATH))
  SELECT DISTINCT(category_tree.child_id) AS id
  FROM category_tree
  UNION SELECT model_groups.id
) &&
ARRAY(
  SELECT id from all_reservable_categories
)
:sql:limit
ORDER BY name ASC
