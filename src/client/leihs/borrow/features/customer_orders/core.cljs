(ns leihs.borrow.features.customer-orders.core
  (:require
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   #_[leihs.borrow.features.customer-orders.orders-index :as index]
   #_[leihs.borrow.features.customer-orders.orders-show :as show]))

; NOTE: re-exporting confuses hot-reloading (we want `alias` but does not exist in cljs)
; (def index-view index/view)
; (def show-view show/view)

(set-default-translate-path :borrow.rentals)

(defn rental-summary-text [rental]
  (let [is-open (= (:state rental) "OPEN")
        total-quantity (:total-quantity rental)]
    (t (if is-open :summary-line.open :summary-line.closed)
       {:itemCount total-quantity
        :totalDays (:total-days rental)
        :fromDate (js/Date. (:from-date rental))
        :untilDate (js/Date. (:until-date rental))})))