(ns leihs.borrow.lib.helpers
  (:require ["date-fns" :as datefn]))

(defn date-format-day [date]
  (datefn/format date "yyyy-MM-dd"))
