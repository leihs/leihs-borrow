(ns leihs.borrow.resources.inventory-pools.visits-restrictions
  (:require
   [java-time :as jt]
   [leihs.borrow.resources.holidays :as holidays]
   [leihs.borrow.resources.workdays :as workdays]
   [leihs.core.availability.pool :as pool]
   [taoensso.timbre :as timbre :refer [debug spy]]))

(defn earliest-possible-pickup-date
  ([pool] (earliest-possible-pickup-date pool false))
  ([pool alternative-pickup-location?]
   (let [start-date (jt/local-date)
         transfer-buffer (:transfer_buffer_before_pick_up pool)
         before-pick-up-days (if alternative-pickup-location?
                               (or transfer-buffer 0)
                               0)
         reservation-advance-days (max (or (:reservation_advance_days pool) 0)
                                       before-pick-up-days)
         #_#_limit (jt/plus start-date (jt/years 1))]
     (if (and (-> pool :holidays empty?)
              (-> pool workdays/closed-days empty?)
              (zero? reservation-advance-days))
       start-date
       (when (-> pool workdays/open-days empty? not)
         (loop [date start-date, in-advance 0]
           (if (or (and (not (zero? reservation-advance-days))
                        (< in-advance reservation-advance-days))
                   (pool/close-time? date pool))
             (recur (jt/plus date (jt/days 1))
                    (cond-> in-advance
                      (pool/orders-processing? date pool)
                      inc))
             date)))))))

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

(defn validate-dates
  ([tx dates-with-avail pool] (validate-dates tx dates-with-avail pool false))
  ([tx dates-with-avail pool alternative-pickup-location?]
   (let [pool* (as-> pool <>
                 (assoc <>
                        :holidays
                        (holidays/get-by-pool-id tx (:id <>)))
                 (assoc <>
                        :earliest-possible-pickup-date
                        (earliest-possible-pickup-date
                         <> alternative-pickup-location?)))]
     {:dates (map #(validate-single-date % pool*) dates-with-avail)
      :earliest-possible-pickup-date (:earliest-possible-pickup-date pool*)})))
