(ns leihs.borrow.graphql.scalars
  (:require
    [java-time]
    [clojure.tools.logging :as log]
    [leihs.core.core :refer [spy-with presence raise]]
    [taoensso.timbre :refer [debug info warn error spy]]
    )
  (:import [java.util UUID]
           [java.time.format DateTimeFormatter]
           [java.time ZoneOffset]))

(defn date-time [x]
  (try (->> x
            (java-time/instant DateTimeFormatter/ISO_INSTANT)
            (java-time/format DateTimeFormatter/ISO_INSTANT))
       (catch Throwable _
         nil)))

(defn date-parse [x]
  (if-let [local-date (some #(try (java-time/local-date % x)
                                  (catch Throwable _ nil))
                            [DateTimeFormatter/ISO_LOCAL_DATE
                             DateTimeFormatter/ISO_ZONED_DATE_TIME])]
    (.toString local-date)))

(defn date-serialize [x]
  (debug 'date-serialize {:value x
                          :type (type x)})
  (try (-> x ; expected type of java.lang.String
           .toString ; for type of java.sql.Date
           (->> (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE))
           .atStartOfDay
           (.toInstant ZoneOffset/UTC)
           .toString)
       (catch Throwable _ nil)))

(defn validate-non-empty-text [x]
  (when-not (re-matches #"^\s*$" x) x))

(def scalars
  {:uuid-parse #(UUID/fromString %)
   :uuid-serialize str
   :date-parse date-parse
   :date-serialize date-serialize
   :date-time-parse date-time
   :date-time-serialize date-time
   :non-empty-text-parse validate-non-empty-text
   :non-empty-text-serialize validate-non-empty-text})

(comment
  (re-matches #"^\s*$" "     ")
  (->> "2011-12-03T10:15:30Z"
       (java-time/instant DateTimeFormatter/ISO_INSTANT)
       (java-time/format DateTimeFormatter/ISO_INSTANT))
  (->> "2011-12-03"
       (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE)
       (java-time/format DateTimeFormatter/ISO_LOCAL_DATE))
  (->> "2020-08-31T16:09:56.564Z"
       (java-time/local-date DateTimeFormatter/ISO_ZONED_DATE_TIME)
       .toString))
