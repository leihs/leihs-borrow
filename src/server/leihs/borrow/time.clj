(ns leihs.borrow.time
  (:require [leihs.borrow.resources.settings :as settings]
            java-time)
  (:import [java.time.format DateTimeFormatter]))

(defn now [tx]
  (let [time-zone (-> tx
                      settings/get
                      :time_zone
                      java.util.TimeZone/getTimeZone)]
    (java-time/local-date-time (java-time/instant) time-zone)))

(defn past? [date]
  (java-time/after?
   (java-time/local-date)
   (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE date)))
