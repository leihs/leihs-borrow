-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name descendent-ids :? :*
-- :doc Get ids of all descendents of a given category-id

WITH RECURSIVE :snip:category-tree-snip

SELECT DISTINCT t1.child_id AS id
FROM category_tree AS t1
WHERE :category-id = ANY(PATH)
