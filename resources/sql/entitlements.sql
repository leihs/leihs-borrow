-- ==================================== SNIPPETS=============================================

-- :snip and-model-ids-1-snip
AND model_id IN (:v*:model-ids)
-- :snip and-pool-ids-1-snip
AND entitlement_groups.inventory_pool_id IN (:v*:pool-ids)
-- :snip and-model-ids-2-snip
AND i.model_id IN (:v*:model-ids)
-- :snip and-pool-ids-2-snip
AND i.inventory_pool_id IN (:v*:pool-ids)
-- :snip where-user-id-snip
WHERE pwg.entitlement_group_id IN (
  SELECT egu.entitlement_group_id
  FROM entitlement_groups_users AS egu
  WHERE egu.user_id = :user-id
) OR pwg.entitlement_group_id IS NULL

-- ==================================== QUERY ===============================================

-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name all-entitlements :? :*
-- :doc Get entitlements including the general group (for user, models and pools)
SELECT *
FROM (
  SELECT model_id,
         entitlement_groups.inventory_pool_id,
         entitlement_group_id,
         quantity
  FROM entitlements
  INNER JOIN entitlement_groups ON entitlements.entitlement_group_id = entitlement_groups.id
  WHERE TRUE
  --~ (when (:and-model-ids-1 params) ":snip:and-model-ids-1")
  --~ (when (:and-pool-ids-1 params) ":snip:and-pool-ids-1")
  UNION
  SELECT model_id,
         inventory_pool_id,
         NULL AS entitlement_group_id,
         (count(i.id) - coalesce(
           (SELECT sum(quantity)
            FROM entitlements AS es
            INNER JOIN entitlement_groups ON entitlement_groups.id = es.entitlement_group_id
            WHERE es.model_id = i.model_id
              AND entitlement_groups.inventory_pool_id = i.inventory_pool_id
            GROUP BY entitlement_groups.inventory_pool_id, es.model_id),
          0
        )) AS quantity
  FROM items AS i
  WHERE i.retired IS NULL
    AND i.is_borrowable = TRUE
    AND i.parent_id IS NULL
    --~ (when (:and-model-ids-2 params) ":snip:and-model-ids-2")
    --~ (when (:and-pool-ids-2 params) ":snip:and-pool-ids-2")
  GROUP BY i.inventory_pool_id,
           i.model_id
) AS pwg
--~ (when (:where-user-id params) ":snip:where-user-id")
