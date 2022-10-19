(ns leihs.borrow.time
  (:require [leihs.core.settings :refer [settings!]]
            java-time)
  (:import [java.time.format DateTimeFormatter]))

(defn now [tx]
  (let [time-zone (-> (settings! tx [:time_zone])
                      :time_zone
                      java.util.TimeZone/getTimeZone)]
    (java-time/local-date-time (java-time/instant) time-zone)))

(defn past? [date]
  (java-time/after?
   (java-time/local-date)
   (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE (.toString date))))
