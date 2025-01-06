-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name reservable-child-categories :? :*
-- :doc Get all reservable child categories (directly, or via any descendent) by user-id and parent category-id and possibly limit by pool-ids

WITH RECURSIVE
:snip:all-reservable-categories-snip ,
:snip:category-tree-snip

SELECT t1.child_id AS id, COALESCE(t1.label, t1.name) AS name
FROM category_tree AS t1
WHERE t1.parent_id = :category-id
  AND (
    t1.child_id IN (SELECT id FROM all_reservable_categories)
    OR
    EXISTS (
      SELECT 1
      FROM category_tree AS t2
      WHERE t1.child_id = ANY(PATH)
      AND t2.child_id IN (SELECT id FROM all_reservable_categories)
    )
  )
ORDER BY COALESCE(t1.label, t1.name) ASC