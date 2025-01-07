-- :snip category-tree-snip
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
