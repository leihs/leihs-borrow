-- A ":result" value of ":*" specifies a vector of records
-- (as hashmaps) will be returned
-- :name booking-calendar-visits :? :*
-- :doc Get visits count between start_date and end_date per date

WITH dates AS (
  SELECT "date"::date
  FROM generate_series(:start-date::date, :end-date::date, '1 day'::interval) AS "date"
)
SELECT dates.date::text,
       (SELECT count(hand_overs.*)
        FROM (SELECT dates.date,
              reservations.user_id,
              reservations.start_date
              FROM reservations
              WHERE inventory_pool_id = :inventory-pool-id
              AND status IN ('submitted', 'approved')
              AND start_date = dates.date
              GROUP BY user_id, start_date) AS hand_overs)
       +
       (SELECT count(take_backs.*)
        FROM (SELECT dates.date,
                     reservations.user_id,
                     reservations.end_date
              FROM reservations
              WHERE inventory_pool_id = :inventory-pool-id
              AND status = 'signed'
              AND end_date = dates.date
              GROUP BY user_id, end_date) AS take_backs) AS visits_count
FROM dates
