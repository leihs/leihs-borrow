-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name to-reserve-from :? :*
-- :doc Get all pools which are relevant for making a reservation

WITH accessible_pools AS (
  SELECT DISTINCT inventory_pools.*,
                  workdays.monday,
                  workdays.tuesday,
                  workdays.wednesday,
                  workdays.thursday,
                  workdays.friday,
                  workdays.saturday,
                  workdays.sunday,
                  workdays.monday_orders_processing,
                  workdays.tuesday_orders_processing,
                  workdays.wednesday_orders_processing,
                  workdays.thursday_orders_processing,
                  workdays.friday_orders_processing,
                  workdays.saturday_orders_processing,
                  workdays.sunday_orders_processing,
                  workdays.max_visits
  FROM inventory_pools
  INNER JOIN access_rights ON access_rights.inventory_pool_id = inventory_pools.id
  INNER JOIN workdays ON workdays.inventory_pool_id = inventory_pools.id
  WHERE inventory_pools.is_active = TRUE
    AND access_rights.user_id = :user-id
    AND NOT EXISTS (
      SELECT 1
      FROM suspensions
      WHERE suspensions.inventory_pool_id = inventory_pools.id
      AND suspensions.user_id = :user-id
      AND CURRENT_DATE <= suspensions.suspended_until
    )
),
date_range AS (
  SELECT generate_series(CURRENT_DATE, :start-date::date, '1 day'::interval) AS val
)

SELECT accessible_pools.*
FROM accessible_pools

-- the duration of the reservation is less than or equal to the maximum reservation duration
WHERE (
  accessible_pools.borrow_maximum_reservation_duration IS NULL OR
  EXTRACT(DAY FROM age(:end-date::date, :start-date::date)) < accessible_pools.borrow_maximum_reservation_duration
)

-- the number of open days is at least `borrow_reservation_advance_days`
AND (
  SELECT count(*)
  FROM date_range
  WHERE (
    array[
      accessible_pools.monday_orders_processing,
      accessible_pools.tuesday_orders_processing,
      accessible_pools.wednesday_orders_processing,
      accessible_pools.thursday_orders_processing,
      accessible_pools.friday_orders_processing,
      accessible_pools.saturday_orders_processing,
      accessible_pools.sunday_orders_processing
    ]
  )[to_char(date_range.val, 'ID')::int]
    AND NOT EXISTS (
      SELECT 1
      FROM holidays
      WHERE holidays.inventory_pool_id = accessible_pools.id
        AND date_range.val BETWEEN holidays.start_date AND holidays.end_date
        AND NOT holidays.orders_processing
  )
) > accessible_pools.borrow_reservation_advance_days

-- start_date does not fall on holiday
AND NOT EXISTS
    (SELECT TRUE
     FROM holidays
     WHERE holidays.inventory_pool_id = accessible_pools.id
       AND CAST(:start-date AS date) BETWEEN holidays.start_date AND holidays.end_date )

-- end_date does not fall on holiday
AND NOT EXISTS
    (SELECT TRUE
     FROM holidays
     WHERE holidays.inventory_pool_id = accessible_pools.id
       AND CAST(:end-date AS date) BETWEEN holidays.start_date AND holidays.end_date )

-- pool is open on the start_date
AND (array[accessible_pools.monday,
           accessible_pools.tuesday,
           accessible_pools.wednesday,
           accessible_pools.thursday,
           accessible_pools.friday,
           accessible_pools.saturday,
           accessible_pools.sunday])[to_char(CAST(:start-date AS date), 'ID')::int]

-- pool is open on the end_date
AND (array[accessible_pools.monday,
           accessible_pools.tuesday,
           accessible_pools.wednesday,
           accessible_pools.thursday,
           accessible_pools.friday,
           accessible_pools.saturday,
           accessible_pools.sunday])[to_char(CAST(:end-date AS date), 'ID')::int]

-- max amount of visits is not exceeded yet for the start date and pool
AND
    (SELECT count(*)
     FROM visits
     WHERE visits.inventory_pool_id = accessible_pools.id
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
     WHERE visits.inventory_pool_id = accessible_pools.id
       AND visits.date = CAST(:end-date AS date) )
    <
    coalesce(
      (max_visits->>(EXTRACT(DOW FROM CAST(:end-date AS date))::int)::text)::integer,
      2147483647
    )
