(ns leihs.borrow.resources.helpers
  (:require [clojure.string :as string]
            [leihs.core.sql :as sql]))

(defn treat-order-arg [order]
  (map #(as-> % <>
          (into (sorted-map) <>)
          (update <> 
                  :attribute
                  (comp keyword string/lower-case name))
          (vals <>))
       order))

(def format-string "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"")

(defn iso8601-created-at
  ([] (iso8601-created-at nil))
  ([q]
   [(sql/call :to_char
             (if q
               (sql/qualify q :created_at)
               :created_at)
             format-string)
    :created_at]))

(defn iso8601-updated-at
  ([] (iso8601-updated-at nil))
  ([q]
   [(sql/call :to_char
             (if q
               (sql/qualify q :updated_at)
               :updated_at)
             format-string)
    :updated_at]))

