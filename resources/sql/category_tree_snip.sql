-- :snip category-tree-snip
category_tree(parent_id, child_id, name, label, PATH) AS
  (SELECT NULL::uuid, mg.id, mg.name, NULL::text, ARRAY[]::uuid[]
   FROM model_groups AS mg
   WHERE NOT EXISTS (SELECT 1
                     FROM model_group_links AS mgl
                     WHERE mgl.child_id = mg.id)
   UNION ALL
   SELECT mgl.parent_id, mgl.child_id, mg2.name, mgl.label, PATH || mgl.parent_id
   FROM category_tree
   INNER JOIN model_group_links AS mgl ON mgl.parent_id = category_tree.child_id
   INNER JOIN model_groups AS mg2 ON mgl.child_id = mg2.id
   WHERE NOT mgl.child_id = any(PATH))
