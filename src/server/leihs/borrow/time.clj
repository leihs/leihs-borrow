(ns leihs.borrow.time
  (:require [leihs.core.settings :refer [settings]]
            [java-time.api :as java-time])
  (:import [java.time.format DateTimeFormatter]))

(defn time-zone [tx]
  (-> (settings tx [:time_zone])
      :time_zone
      java.util.TimeZone/getTimeZone))

(defn now [tx]
  (java-time/local-date-time (java-time/instant) (time-zone tx)))

(defn past-date? [date]
  (java-time/after?
   (java-time/local-date)
   (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE (.toString date))))
