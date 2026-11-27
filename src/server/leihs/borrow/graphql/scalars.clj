(ns leihs.borrow.graphql.scalars
  (:require
   [java-time.api :as java-time])
  (:import [java.util UUID]
           [java.time.format DateTimeFormatter]
           [java.time ZoneOffset]))

(defn date-time-parse [x]
  (try (->> x
            (java-time/instant DateTimeFormatter/ISO_INSTANT)
            (java-time/format DateTimeFormatter/ISO_INSTANT))
       (catch Throwable _
         nil)))

(defn date-time-serialize [x]
  (try (->> x
            .toLocalDateTime
            (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss'Z'")))
       (catch Throwable _
         nil)))

(defn date-parse [x]
  (when-let [local-date (some #(try (java-time/local-date % x)
                                    (catch Throwable _ nil))
                              [DateTimeFormatter/ISO_LOCAL_DATE
                               DateTimeFormatter/ISO_ZONED_DATE_TIME])]
    (.toString local-date)))

(defn date-serialize [x]
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
   :date-time-parse date-time-parse
   :date-time-serialize date-time-serialize
   :non-empty-text-parse validate-non-empty-text
   :non-empty-text-serialize validate-non-empty-text})
