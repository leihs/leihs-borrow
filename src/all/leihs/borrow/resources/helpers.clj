(ns leihs.borrow.resources.helpers
  (:require [clojure.string :as string]))

(defn treat-order-arg [order]
  (map #(-> %
            (update :attribute
                    (comp keyword string/lower-case name))
            vals)
       order))

