(ns leihs.borrow.features.customer-orders.current-lendings-status
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.re-frame :refer [reg-event-db reg-sub]]))

;; global unfiltered list of current lendings, status fields only

(reg-sub ::current-lendings
         (fn [db _] (get-in db [:ls ::data])))

(reg-event-db ::set-current-lendings
              (fn-traced [db [_ current-lendings]]
                (assoc-in db [:ls ::data] current-lendings)))
