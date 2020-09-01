(ns leihs.borrow.graphql.scalars
  (:require java-time
            [clojure.tools.logging :as log]
            [leihs.core.core :refer [spy-with]])
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
  (try (-> x
           (->> (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE))
           .atStartOfDay
           (.toInstant ZoneOffset/UTC)
           .toString)
       (catch Throwable _ nil)))

(def scalars
  {:uuid-parse #(UUID/fromString %)
   :uuid-serialize str
   :date-parse date-parse
   :date-serialize date-serialize
   :date-time-parse date-time
   :date-time-serialize date-time})

(comment
  (->> "2011-12-03T10:15:30Z"
       (java-time/instant DateTimeFormatter/ISO_INSTANT)
       (java-time/format DateTimeFormatter/ISO_INSTANT))
  (->> "2011-12-03"
       (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE)
       (java-time/format DateTimeFormatter/ISO_LOCAL_DATE))
  (->> "2020-08-31T16:09:56.564Z"
       (java-time/local-date DateTimeFormatter/ISO_ZONED_DATE_TIME)
       .toString))
