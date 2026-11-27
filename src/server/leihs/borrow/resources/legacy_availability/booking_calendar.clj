(ns leihs.borrow.resources.legacy-availability.booking-calendar
  (:refer-clojure :exclude [get])
  (:require [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [hugsql.core :as hugsql]
            [leihs.core.availability.changes :as ch]
            [leihs.core.availability.core :as c]
            [leihs.core.time-zone :as tz]
            [java-time.api :as t]))

(hugsql/def-sqlvec-fns "sql/booking_calendar_visits.sql")

(defn get-visits-counts [tx start-date end-date pool-id]
  (->> {:inventory-pool-id pool-id, :start-date start-date, :end-date end-date}
       booking-calendar-visits-sqlvec
       (jdbc-query tx)))

(defn- prospective-days
  "Per-date quantity of each date as a prospective pickup-location booking
  day. Dates before today get 0."
  [tx context start end]
  (let [today (tz/today tx)]
    (->> (ch/explode-date-range start end)
         (map (fn [date]
                {:date date
                 :quantity (if (t/before? date today)
                             0
                             (c/available-quantity-for-prospective-day
                              context date))})))))

(defn get [tx start-date end-date pool-id user-id model-id exclude-res-ids
           for-pickup-locations]
  (let [start (ch/local-date start-date)
        end (ch/local-date end-date)
        {:keys [changes group-ids] :as context}
        (c/fetch-context tx model-id user-id pool-id exclude-res-ids
                         for-pickup-locations)
        quantities (->> (if for-pickup-locations
                          (prospective-days tx context start end)
                          (c/booking-calendar changes group-ids start end))
                        (map (fn [{:keys [date quantity]}]
                               {:date (str date)
                                :quantity (max 0 quantity)
                                :visits_count 0})))
        visits-count (get-visits-counts tx start-date end-date pool-id)]
    {:dates (mapv merge quantities visits-count)}))
