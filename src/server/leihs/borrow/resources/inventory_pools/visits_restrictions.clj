(ns leihs.borrow.resources.inventory-pools.visits-restrictions
  (:require
   [java-time :as jt]
   [leihs.borrow.resources.holidays :as holidays]
   [leihs.borrow.resources.workdays :as workdays]
   [leihs.core.availability.pool :as pool]
   [taoensso.timbre :as timbre :refer [debug spy]]))

(defn orders-processing-day? [date pool]
  (let [orders-processing-day (-> date
                                  .getDayOfWeek
                                  .toString
                                  .toLowerCase
                                  (str "_orders_processing")
                                  keyword)]
    (orders-processing-day pool)))

(defn orders-processing? [date pool]
  (let [date* (jt/local-date date)]
    (and (orders-processing-day? date* pool)
         (if-let [holiday (pool/get-holiday date* pool)]
           (:orders_processing holiday)
           true))))

(defn earliest-possible-pickup-date [pool]
  (let [start-date (jt/local-date)
        reservation-advance-days (or (:reservation_advance_days pool) 0)
        #_#_limit (jt/plus start-date (jt/years 1))]
    (if (and (-> pool :holidays empty?)
             (-> pool workdays/closed-days empty?)
             (zero? reservation-advance-days))
      start-date
      (when (-> pool workdays/open-days empty? not)
        (loop [date start-date, in-advance 0]
          (cond (pool/close-time? date pool)
                (recur (jt/plus date (jt/days 1))
                       (cond-> in-advance
                         (orders-processing? date pool)
                         inc))
                (and (not (zero? reservation-advance-days))
                     (< in-advance reservation-advance-days))
                (recur (jt/plus date (jt/days 1))
                       (inc in-advance))
                :else date))))))

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
        (pool/working-day? pool)
        not)
    (conj :NON_WORKDAY)

    (-> date-with-avail :date jt/local-date (pool/get-holiday pool))
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
        (pool/working-day? pool)
        not)
    (conj :NON_WORKDAY)

    (-> date-with-avail :date jt/local-date (pool/get-holiday pool))
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
