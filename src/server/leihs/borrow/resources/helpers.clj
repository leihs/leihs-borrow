(ns leihs.borrow.resources.helpers
  (:require [clojure.string :as string]
            [leihs.core.sql :as sql]))

(defn treat-order-arg
  ([order] (treat-order-arg order nil))
  ([order table]
   (map #(as-> % <>
           (into (sorted-map) <>)
           (update <>
                   :attribute
                   (comp (partial sql/qualify table)
                         string/lower-case
                         name))
           (vals <>))
        order)))

(def date-time-format "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"")

(defn date-time-created-at
  ([] (date-time-created-at nil))
  ([table]
   [(sql/call :to_char
              (if table
                (sql/qualify table :created_at)
                :created_at)
              date-time-format)
    :created_at]))

(defn date-time-updated-at
  ([] (date-time-updated-at nil))
  ([table]
   [(sql/call :to_char
              (if table
                (sql/qualify table :updated_at)
                :updated_at)
              date-time-format)
    :updated_at]))
