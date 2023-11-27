(ns leihs.borrow.resources.legacy-availability.booking-calendar
  (:refer-clojure :exclude [get])
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [hugsql.core :as hugsql]
            [leihs.core.db :as db]
            [leihs.core.settings :refer [settings!]]
            [leihs.borrow.resources.legacy-availability.changes :as ch]
            [leihs.borrow.resources.legacy-availability.core :as c]
            [java-time :as t]))

(hugsql/def-sqlvec-fns "sql/booking_calendar_visits.sql")

(comment
  (->> {:inventory-pool-id "8bd16d45-056d-5590-bc7f-12849f034351"
        :start-date (str (ch/local-date))
        :end-date (str (t/plus (ch/local-date) (t/days 30)))}
       booking-calendar-visits-sqlvec
       (jdbc-query (db/get-ds-next))))

(defn get-visits-counts [tx start-date end-date pool-id]
  (->> {:inventory-pool-id pool-id, :start-date start-date, :end-date end-date}
       booking-calendar-visits-sqlvec
       (jdbc-query tx)))

(defn get [tx start-date end-date pool-id user-id model-id exclude-res-ids]
  (let [today (ch/local-date)
        start-date-jt (ch/local-date start-date)
        start-date-jt* (if (t/before? start-date-jt today) today start-date-jt)
        end-date-jt (ch/local-date end-date)
        changes (ch/main tx model-id pool-id exclude-res-ids)
        changes-dates (sort (map first changes))
        changes-dates-between-start-and-end-date (filter #(and (t/before? start-date-jt* %)
                                                               (t/before? % end-date-jt))
                                                         changes-dates)
        dates-pairs (as-> changes-dates-between-start-and-end-date <> ; [3 5]
                      (cons start-date-jt* <>) ; [1 3 5]
                      (vec <>)
                      (conj <> end-date-jt) ; [1 3 5 7]
                      (mapv #(vector %1 %2) <> (drop 1 (cycle <>))) ; [[1 3] [3 5] [5 7] [7 1]]
                      (butlast <>) ; [[1 3] [3 5] [5 7]]
                      (map (fn [[d1 d2]]
                             [d1 (if (= end-date-jt d2)
                                   d2
                                   (t/minus d2 (t/days 1)))])
                           <>)) ; [[1 2] [3 4] [5 6]]
        result-1 (->> dates-pairs
                      (map (fn [[from-date to-date]]
                             (let [quantity (c/maximum-available-in-pool-and-period-summed-for-groups
                                             tx
                                             model-id
                                             user-id
                                             from-date
                                             to-date
                                             pool-id
                                             exclude-res-ids)]
                               (->> (ch/explode-date-range from-date to-date)
                                    (map #(hash-map :date (str %) :quantity quantity :visits_count 0))))))
                      flatten)
        result-2 (if (not= start-date-jt start-date-jt*)
                   (concat (->> (ch/explode-date-range start-date-jt (t/minus today (t/days 1)))
                                (map #(hash-map :date (str %) :quantity 0 :visits_count 0)))
                           result-1)
                   result-1)
        visits-count (get-visits-counts tx start-date end-date pool-id)
        result-3 (mapv merge result-2 visits-count)]
    {:dates result-3}))

(comment
  (= (ch/local-date "2023-05-10") (ch/local-date "2023-05-10"))
  (t/before? (ch/local-date "2023-05-10") (ch/local-date "2023-05-11"))
  (t/minus (ch/local-date) (t/days 1))
  (str (ch/local-date))
  (mapv #(vector %1 %2) [1 2 3] (drop 1 (cycle [1 2 3])))
  (let [model-id "804a50c1-2329-5d5b-9884-340f43833514"
        pool-id "8bd16d45-056d-5590-bc7f-12849f034351"
        tx (db/get-ds-next)
        user-id "c0777d74-668b-5e01-abb5-f8277baa0ea8"
        start-date (str (ch/local-date))
        end-date (str (t/plus (ch/local-date) (t/days 30)))]
   ; (get-visits-counts tx start-date end-date pool-id)
    (get tx start-date end-date pool-id user-id model-id nil)))
