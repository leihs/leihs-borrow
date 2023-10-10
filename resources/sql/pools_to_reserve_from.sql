-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name to-reserve-from :? :*
-- :doc Get all pools which are relevant for making a reservation
SELECT inventory_pools.*,
       workdays.monday,
       workdays.tuesday,
       workdays.wednesday,
       workdays.thursday,
       workdays.friday,
       workdays.saturday,
       workdays.sunday,
       workdays.reservation_advance_days,
       workdays.max_visits
FROM inventory_pools
INNER JOIN access_rights ON access_rights.inventory_pool_id = inventory_pools.id
INNER JOIN workdays ON workdays.inventory_pool_id = inventory_pools.id
WHERE inventory_pools.is_active = TRUE
AND access_rights.user_id = CAST(:user-id AS uuid)
AND NOT EXISTS (
  SELECT 1
  FROM suspensions
  WHERE suspensions.inventory_pool_id = inventory_pools.id
    AND suspensions.user_id = :user-id
    AND CURRENT_DATE <= suspensions.suspended_until
)
AND (CAST(:start-date AS date) - CURRENT_DATE) >= workdays.reservation_advance_days
-- start_date does not fall on holiday
AND NOT EXISTS
    (SELECT TRUE
     FROM holidays
     WHERE holidays.inventory_pool_id = inventory_pools.id
       AND CAST(:start-date AS date) BETWEEN holidays.start_date AND holidays.end_date )
-- end_date does not fall on holiday
AND NOT EXISTS
    (SELECT TRUE
     FROM holidays
     WHERE holidays.inventory_pool_id = inventory_pools.id
       AND CAST(:end-date AS date) BETWEEN holidays.start_date AND holidays.end_date )
-- pool is open on the start_date
AND (array[workdays.monday,
           workdays.tuesday,
           workdays.wednesday,
           workdays.thursday,
           workdays.friday,
           workdays.saturday,
           workdays.sunday])[to_char(CAST(:start-date AS date), 'ID')::int]
-- pool is open on the end_date
AND (array[workdays.monday,
           workdays.tuesday,
           workdays.wednesday,
           workdays.thursday,
           workdays.friday,
           workdays.saturday,
          workdays.sunday])[to_char(CAST(:end-date AS date), 'ID')::int]
-- max amount of visits is not exceeded yet for the start date and pool
AND
    (SELECT count(*)
     FROM visits
     WHERE visits.inventory_pool_id = inventory_pools.id
       AND visits.date = CAST(:start-date AS date) )
    <
    coalesce(
      (max_visits->>(EXTRACT(DOW FROM CAST(:start-date AS date))::int)::text)::integer,
      2147483647
    )
-- max amount of visits is not exceeded yet for the end-date and pool
AND
    (SELECT count(*)
     FROM visits
     WHERE visits.inventory_pool_id = inventory_pools.id
       AND visits.date = CAST(:end-date AS date) )
    <
    coalesce(
      (max_visits->>(EXTRACT(DOW FROM CAST(:end-date AS date))::int)::text)::integer,
      2147483647
    )
