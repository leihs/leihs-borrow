-- :snip where-ids-snip
WHERE all_reservable_categories.id IN (:v*:ids)

-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name reservable-categories :? :*
-- :doc Get all reservable categories by user-id and possibly limit by ids and/or pool-ids

:snip:with-all-reservable-categories
SELECT all_reservable_categories.*
FROM all_reservable_categories
--~ (when (:where-ids params) ":snip:where-ids")
ORDER BY all_reservable_categories.name ASC
