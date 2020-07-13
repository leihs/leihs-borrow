(ns leihs.borrow.time
  (:require [leihs.borrow.resources.settings :as settings]
            java-time))

(defn now [tx]
  (let [time-zone (-> tx
                      settings/get
                      :time_zone
                      java.util.TimeZone/getTimeZone)]
    (java-time/local-date-time (java-time/instant) time-zone)))
