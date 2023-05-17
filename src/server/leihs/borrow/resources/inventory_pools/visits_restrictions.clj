(ns leihs.borrow.resources.inventory-pools.visits-restrictions
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [java-time :refer [local-date before?] :as jt]
            [leihs.core.sql :as sql]))

(defn holiday? [tx date pool]
  (let [date* (sql/call :cast date :date)]
    (-> (sql/select true)
        (sql/from :holidays)
        (sql/where [:= :inventory_pool_id (:id pool)])
        (sql/merge-where [:>= date* :start_date])
        (sql/merge-where [:<= date* :end_date])
        sql/format
        (->> (jdbc/query tx))
        empty?
        not)))

(defn not-a-working-day? [date pool]
  (let [day-of-week (-> date
                        local-date
                        .getDayOfWeek
                        .toString
                        .toLowerCase
                        keyword)]
    (not (day-of-week pool))))

(defn close-time? [tx date pool]
  (or (not-a-working-day? date pool)
      (holiday? tx date pool)))

(defn before-earliest-possible-pick-up-date? [date pool]
  (and (:reservation_advance_days pool)
       (< (jt/time-between (local-date)
                           (local-date date)
                           :days)
          (:reservation_advance_days pool))))

(defn visits-capacity-reached? [date visits-count pool]
  (let [index (-> date
                  local-date
                  .getDayOfWeek
                  .getValue
                  (#(if (= % 7) 0 %)) ; convert from 1-based mon-sun to 0-based sun-sat
                  str
                  keyword)
        max_visits (some-> pool :max_visits index Integer.)]
    (and max_visits (>= visits-count max_visits))))

(defn start-date-restriction [tx date-with-avail pool]
  (cond (close-time? tx (:date date-with-avail) pool)
        :CLOSE_TIME
        (before-earliest-possible-pick-up-date? (:date date-with-avail) pool)
        :BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE
        (visits-capacity-reached? (:date date-with-avail)
                                  (:visits_count date-with-avail)
                                  pool)
        :VISITS_CAPACITY_REACHED))

(defn end-date-restriction [tx date-with-avail pool]
  (debug date-with-avail)
  (cond (close-time? tx (:date date-with-avail) pool)
        :CLOSE_TIME
        (visits-capacity-reached? (:date date-with-avail)
                                  (:visits_count date-with-avail)
                                  pool)
        :VISITS_CAPACITY_REACHED))

(def past-date? #(before? (local-date %) (local-date)))

(defn validate-date-with-avail [tx date-with-avail pool]
  (assoc date-with-avail
         :start-date-restriction
         (start-date-restriction tx date-with-avail pool)
         :end-date-restriction
         (end-date-restriction tx date-with-avail pool)))
