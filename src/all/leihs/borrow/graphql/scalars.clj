(ns leihs.borrow.graphql.scalars
  (:require java-time)
  (:import [java.util UUID]
           [java.time.format DateTimeFormatter]))

(defn iso8601 [x]
  (try (->> x
            (java-time/instant DateTimeFormatter/ISO_INSTANT)
            (java-time/format DateTimeFormatter/ISO_INSTANT))
       (catch Throwable _
         nil)))

(def scalars
  {:uuid-parse #(UUID/fromString %)
   :uuid-serialize str
   :iso8601-parse iso8601
   :iso8601-serialize iso8601})

(comment
  (->> "2011-12-03T10:15:30Z"
       (java-time/instant DateTimeFormatter/ISO_INSTANT)
       (java-time/format DateTimeFormatter/ISO_INSTANT)))
