(ns leihs.borrow.resources.helpers
  (:require [clojure.string :as string]))

(defn treat-order-arg [order]
  (map #(as-> % <>
          (into (sorted-map) <>)
          (update <> 
                  :attribute
                  (comp keyword string/lower-case name))
          (vals <>))
       order))

