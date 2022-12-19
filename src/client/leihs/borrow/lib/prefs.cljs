(ns leihs.borrow.lib.prefs
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.re-frame :refer [reg-event-db reg-sub]]))

(reg-event-db
 ::set-show-day-quants
 (fn-traced [db [_ flag]]
   (assoc-in db [:ls2 ::show-day-quants] flag)))

(reg-sub
 ::show-day-quants
 (fn [db] (get-in db [:ls2 ::show-day-quants])))