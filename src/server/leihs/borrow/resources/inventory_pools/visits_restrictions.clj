(ns leihs.borrow.resources.inventory-pools.visits-restrictions
  (:require [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [leihs.borrow.resources.holidays :as holidays]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.workdays :as workdays]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [java-time :as jt]
            [taoensso.timbre :as timbre :refer [debug info spy]]))

(defn dates-range [start end]
  (->> (jt/iterate jt/plus start (jt/days 1))
       (take-while #(or (jt/before? % end) (= % end)))))

(comment (dates-range (jt/local-date) (jt/local-date "2025-02-28")))

(defn holiday? [date pool]
  (some #(->> (dates-range (jt/local-date (:start_date %))
                           (jt/local-date (:end_date %)))
              (some #{date}))
        (:holidays pool)))

(defn close-time? [date pool]
  (let [date* (jt/local-date date)]
    (or (not (pools/working-day? date* pool))
        (holiday? date* pool))))

(defn earliest-possible-pickup-date [pool]
  (let [start-date (jt/local-date)
        reservation-advance-days (or (:reservation_advance_days pool) 0)
        limit (jt/plus start-date (jt/days 10))]
    (if (and (-> pool :holidays empty?)
             (-> pool workdays/closed-days empty?)
             (zero? reservation-advance-days))
      start-date
      (when (-> pool workdays/open-days empty? not)
        (loop [date start-date, in-advance 0]
          (cond (close-time? date pool)
                (recur (jt/plus date (jt/days 1))
                       in-advance)
                (and (not (zero? reservation-advance-days))
                     (< in-advance reservation-advance-days))
                (recur (jt/plus date (jt/days 1))
                       (inc in-advance))
                :else date))))))

(comment
  (require '[leihs.core.db :as db])
  (let [tx (db/get-ds)
        pool (pools/get-by-id tx #uuid "37f689af-458b-4173-a3c5-cb6ca7f29a2f")
        holidays (holidays/get-by-pool-id tx (:id pool))
        pool* (assoc pool :holidays holidays)]
    #_pool*
    (earliest-possible-pickup-date pool*)))

(defn visits-capacity-reached? [date visits-count pool]
  (let [index (-> date
                  jt/local-date
                  .getDayOfWeek
                  .getValue
                  (#(if (= % 7) 0 %)) ; convert from 1-based mon-sun to 0-based sun-sat
                  str
                  keyword)
        max_visits (some-> pool :max_visits index Integer.)]
    (and max_visits (>= visits-count max_visits))))

(defn start-date-restrictions [date-with-avail pool]
  (cond-> nil
    (-> date-with-avail :date jt/local-date
        (pools/working-day? pool)
        not)
    (conj :NON_WORKDAY)

    (-> date-with-avail :date jt/local-date (holiday? pool))
    (conj :HOLIDAY)

    (when-let [eppd (:earliest-possible-pickup-date pool)]
      (jt/before? (jt/local-date (:date date-with-avail)) eppd))
    (conj :BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE)

    (visits-capacity-reached? (:date date-with-avail)
                              (:visits_count date-with-avail)
                              pool)
    (conj :VISITS_CAPACITY_REACHED)))

(defn end-date-restrictions [date-with-avail pool]
  (cond-> nil
    (-> date-with-avail :date jt/local-date
        (pools/working-day? pool)
        not)
    (conj :NON_WORKDAY)

    (-> date-with-avail :date jt/local-date (holiday? pool))
    (conj :HOLIDAY)

    (visits-capacity-reached? (:date date-with-avail)
                              (:visits_count date-with-avail)
                              pool)
    (conj :VISITS_CAPACITY_REACHED)))

(defn validate-single-date [date-with-avail pool]
  (assoc date-with-avail
         :start-date-restrictions
         (start-date-restrictions date-with-avail pool)
         :end-date-restrictions
         (end-date-restrictions date-with-avail pool)))

(defn validate-dates [tx dates-with-avail pool]
  (let [pool* (as-> pool <>
                (assoc <>
                       :holidays
                       (holidays/get-by-pool-id tx (:id <>)))
                (assoc <>
                       :earliest-possible-pickup-date
                       (earliest-possible-pickup-date <>)))]
    (map #(validate-single-date % pool*)
         dates-with-avail)))
