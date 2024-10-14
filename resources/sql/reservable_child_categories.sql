-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name reservable-child-categories :? :*
-- :doc Get all reservable child categories by user-id and parent category-id and possibly limit by pool-ids

:snip:with-all-reservable-categories
SELECT model_groups.id, model_groups.name
FROM model_groups
JOIN model_group_links ON model_group_links.child_id = model_groups.id
WHERE model_group_links.parent_id = :category-id
AND ARRAY( :snip:category-tree-snip ) && ARRAY( SELECT id from all_reservable_categories )
ORDER BY model_groups.name ASC
