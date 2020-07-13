(ns leihs.borrow.resources.entitlements
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(def all-sql
  "NOTE: As long as legacy lives, taken literally from:
     https://github.com/leihs/leihs_legacy/blob/master/app/models/entitlement.rb#L53"

  "
  SELECT model_id,
         entitlement_groups.inventory_pool_id,
         entitlement_group_id,
         quantity
  FROM entitlements
  INNER JOIN entitlement_groups
    ON entitlements.entitlement_group_id = entitlement_groups.id

  UNION

  SELECT model_id,
         inventory_pool_id,
         NULL AS entitlement_group_id,
         (count(i.id) - coalesce(
           (SELECT sum(quantity)
            FROM entitlements AS es
            INNER JOIN entitlement_groups
              ON entitlement_groups.id = es.entitlement_group_id
            WHERE es.model_id = i.model_id
              AND entitlement_groups.inventory_pool_id = i.inventory_pool_id
            GROUP BY entitlement_groups.inventory_pool_id, es.model_id),
           0
         )) AS quantity
  FROM items AS i
  WHERE i.retired IS NULL
    AND i.is_borrowable = TRUE
    AND i.parent_id IS NULL
  GROUP BY i.inventory_pool_id,
           i.model_id
  ")

(comment 
  (->> [all-sql] (jdbc/query (leihs.core.ds/get-ds))))
