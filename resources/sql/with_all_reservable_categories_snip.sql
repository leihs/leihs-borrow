-- :snip and-pool-ids-snip
AND inventory_pools.id IN (:v*:pool-ids)

-- :snip with-all-reservable-categories-snip
WITH all_reservable_categories AS (
  SELECT DISTINCT model_groups.id, model_groups.name
  FROM model_groups
  INNER JOIN model_links ON model_groups.id = model_links.model_group_id
  INNER JOIN models ON model_links.model_id = models.id
  INNER JOIN items ON models.id = items.model_id
  INNER JOIN inventory_pools ON items.inventory_pool_id = inventory_pools.id
  INNER JOIN access_rights ON inventory_pools.id = access_rights.inventory_pool_id
  INNER JOIN
    (SELECT model_id,
            entitlement_groups.inventory_pool_id,
            entitlement_group_id,
            quantity
     FROM entitlements
     INNER JOIN entitlement_groups ON entitlements.entitlement_group_id = entitlement_groups.id
     UNION SELECT model_id,
                  inventory_pool_id,
                  NULL AS entitlement_group_id,
                  (count(i.id) - coalesce(
                                            (SELECT sum(quantity)
                                             FROM entitlements AS es
                                             INNER JOIN entitlement_groups ON entitlement_groups.id = es.entitlement_group_id
                                             WHERE es.model_id = i.model_id
                                               AND entitlement_groups.inventory_pool_id = i.inventory_pool_id
                                             GROUP BY entitlement_groups.inventory_pool_id, es.model_id), 0)) AS quantity
     FROM items AS i
     WHERE i.retired IS NULL
       AND i.is_borrowable = TRUE
       AND i.parent_id IS NULL
     GROUP BY i.inventory_pool_id,
              i.model_id) AS pwg ON models.id = pwg.model_id
  AND inventory_pools.id = pwg.inventory_pool_id
  AND pwg.quantity > 0
  AND (pwg.entitlement_group_id IN
         (SELECT entitlement_group_id
          FROM entitlement_groups_users
          WHERE user_id = :user-id)
       OR pwg.entitlement_group_id IS NULL)
  WHERE inventory_pools.is_active = 't'
    AND access_rights.user_id = :user-id
    AND model_groups.type = 'Category'
    AND items.retired IS NULL
    AND items.is_borrowable = 't'
    AND items.parent_id IS NULL
    --~ (when (:and-pool-ids params) ":snip:and-pool-ids")
  )
