-- :snip where-ids-snip
AND model_groups.id IN (:v*:ids)

-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name reservable-categories :? :*
-- :doc Get all reservable categories by user-id and possibly limit by ids and/or pool-ids

:snip:with-all-reservable-categories
SELECT model_groups.id, model_groups.name
FROM model_groups
WHERE ARRAY( :snip:category-tree-snip ) && ARRAY( SELECT id from all_reservable_categories )
--~ (when (:where-ids params) ":snip:where-ids")
ORDER BY model_groups.name ASC
