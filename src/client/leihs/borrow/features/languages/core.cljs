(ns leihs.borrow.features.languages.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.re-frame :refer [reg-event-db
                                      reg-sub]]))

(reg-sub ::data (fn [db _] (get-in db [:ls ::data])))

(reg-event-db ::set-languages
              (fn-traced [db [_ languages]]
                (assoc-in db [:ls ::data] languages)))
