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
AND ARRAY( :snip:category-tree-snip ) && ARRAY( SELECT id from all_reservable_categories )
:sql:limit
ORDER BY model_groups.name ASC
