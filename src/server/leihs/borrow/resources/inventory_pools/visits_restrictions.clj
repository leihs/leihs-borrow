(ns leihs.borrow.resources.inventory-pools.visits-restrictions
  (:refer-clojure :exclude [range])
  (:require [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [leihs.borrow.resources.holidays :as holidays]
            [leihs.borrow.resources.inventory-pools :as pools]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [java-time :as jt]
            [taoensso.timbre :as timbre :refer [debug info spy]]))

(defn range [start end]
  (->> (jt/iterate jt/plus start (jt/days 1))
       (take-while #(or (jt/before? % end) (= % end)))))

(comment (range (jt/local-date) (jt/local-date "2025-02-28")))

(defn holiday? [date pool]
  (some #(->> (range (jt/local-date (:start_date %))
                     (jt/local-date (:end_date %)))
              (some #{date}))
        (:holidays pool)))

(defn close-time? [date pool]
  (let [date* (jt/local-date date)]
    (or (not (pools/working-day? date* pool))
        (holiday? date* pool))))

(defn earliest-possible-pickup-date [pool]
  (let [start (jt/local-date)
        limit (jt/plus start (jt/years 100))]
    (loop [date start, in-advance 0]
      (cond (= date limit)
            (throw (ex-info "No possible pickup date found" {:pool pool}))

            (close-time? date pool)
            (recur (jt/plus date (jt/days 1)) in-advance)

            (and (:borrow_reservation_advance_days pool)
                 (< in-advance (:borrow_reservation_advance_days pool)))
            (recur (jt/plus date (jt/days 1)) (inc in-advance))

            :else date))))

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

(defn start-date-restriction [date-with-avail pool]
  (cond (close-time? (:date date-with-avail) pool)
        :CLOSE_TIME
        (jt/before? (jt/local-date (:date date-with-avail))
                    (:earliest-possible-pickup-date pool))
        :BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE
        (visits-capacity-reached? (:date date-with-avail)
                                  (:visits_count date-with-avail)
                                  pool)
        :VISITS_CAPACITY_REACHED))

(defn end-date-restriction [date-with-avail pool]
  (cond (close-time? (:date date-with-avail) pool)
        :CLOSE_TIME
        (visits-capacity-reached? (:date date-with-avail)
                                  (:visits_count date-with-avail)
                                  pool)
        :VISITS_CAPACITY_REACHED))

(defn validate-single-date [date-with-avail pool]
  (assoc date-with-avail
         :start-date-restriction
         (start-date-restriction date-with-avail pool)
         :end-date-restriction
         (end-date-restriction date-with-avail pool)))

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

(comment
  (require '[leihs.core.db :as db])
  (let [tx (db/get-ds)
        pool (pools/get-by-id tx #uuid "ab61cf01-08ce-4d9b-97d3-8dcd8360605a")
        holidays (holidays/get-by-pool-id tx (:id pool))
        pool* (assoc pool :holidays holidays)]
    holidays
    #_(earliest-possible-pickup-date pool*)))

