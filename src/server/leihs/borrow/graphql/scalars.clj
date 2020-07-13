(ns leihs.borrow.graphql.scalars
  (:require java-time)
  (:import [java.util UUID]
           [java.time.format DateTimeFormatter]))

(defn date-time [x]
  (try (->> x
            (java-time/instant DateTimeFormatter/ISO_INSTANT)
            (java-time/format DateTimeFormatter/ISO_INSTANT))
       (catch Throwable _
         nil)))

(defn date-parse [x]
  (try (->> x
            (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE)
            (java-time/format DateTimeFormatter/ISO_LOCAL_DATE))
       (catch Throwable _
         nil)))

(def scalars
  {:uuid-parse #(UUID/fromString %)
   :uuid-serialize str
   :date-parse date-parse
   :date-serialize str
   :date-time-parse date-time
   :date-time-serialize date-time})

(comment
  (->> "2011-12-03T10:15:30Z"
       (java-time/instant DateTimeFormatter/ISO_INSTANT)
       (java-time/format DateTimeFormatter/ISO_INSTANT))
  (->> "2011-12-03"
       (java-time/local-date DateTimeFormatter/ISO_LOCAL_DATE)
       (java-time/format DateTimeFormatter/ISO_LOCAL_DATE)))
