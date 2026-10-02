(ns leihs.borrow.resources.legacy-availability.booking-calendar
  (:refer-clojure :exclude [get])
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [hugsql.core :as hugsql]
            [leihs.core.db :as db]
            [leihs.core.settings :refer [settings]]
            [leihs.core.availability.changes :as ch]
            [leihs.core.availability.core :as c]
            [leihs.core.availability.queries :as q]
            [java-time :as t]))

(hugsql/def-sqlvec-fns "sql/booking_calendar_visits.sql")

(comment
  (->> {:inventory-pool-id "8bd16d45-056d-5590-bc7f-12849f034351"
        :start-date (str (ch/local-date))
        :end-date (str (t/plus (ch/local-date) (t/days 30)))}
       booking-calendar-visits-sqlvec
       (jdbc-query (db/get-ds))))

(defn get-visits-counts [tx start-date end-date pool-id]
  (->> {:inventory-pool-id pool-id, :start-date start-date, :end-date end-date}
       booking-calendar-visits-sqlvec
       (jdbc-query tx)))

(defn get [tx start-date end-date pool-id user-id model-id exclude-res-ids]
  (let [changes (ch/main tx model-id pool-id exclude-res-ids)
        group-ids (cons :general (q/get-user-group-ids tx user-id))
        quantities (->> (c/booking-calendar changes
                                            group-ids
                                            (ch/local-date start-date)
                                            (ch/local-date end-date))
                        (map (fn [{:keys [date quantity]}]
                               {:date (str date)
                                :quantity (max 0 quantity)
                                :visits_count 0})))
        visits-count (get-visits-counts tx start-date end-date pool-id)]
    {:dates (mapv merge quantities visits-count)}))

(comment
  (= (ch/local-date "2023-05-10") (ch/local-date "2023-05-10"))
  (t/before? (ch/local-date "2023-05-10") (ch/local-date "2023-05-11"))
  (t/minus (ch/local-date) (t/days 1))
  (str (ch/local-date))
  (mapv #(vector %1 %2) [1 2 3] (drop 1 (cycle [1 2 3])))
  (let [model-id #uuid "041d82c9-c02f-4f6e-bf15-805c49796911"
        pool-id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"
        tx (db/get-ds)
        user-id #uuid "c0777d74-668b-5e01-abb5-f8277baa0ea8"
        start-date (str (ch/local-date))
        end-date (str (t/plus (ch/local-date) (t/days 30)))]
   ; (get-visits-counts tx start-date end-date pool-id)
    (get tx start-date end-date pool-id user-id model-id nil)))
