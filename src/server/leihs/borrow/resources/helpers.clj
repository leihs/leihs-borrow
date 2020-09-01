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

(def date-format "YYYY-MM-DD")
(def date-time-format "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"")

(defmacro def-attribute-override-fn [fn-name attr format-str]
  `(defn ~fn-name
     ([] (~fn-name nil))
     ([q#]
      [(sql/call :to_char
                 (if q#
                   (sql/qualify q# ~attr)
                   ~attr)
                 ~format-str)
       ~attr])))

(def-attribute-override-fn date :date date-format)
(def-attribute-override-fn date-start-date :start_date date-format)
(def-attribute-override-fn date-end-date :end_date date-format)
(def-attribute-override-fn date-time-created-at :created_at date-time-format)
(def-attribute-override-fn date-time-updated-at :updated_at date-time-format)
