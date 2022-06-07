(ns leihs.borrow.lib.errors
  (:require
   [leihs.borrow.lib.re-frame :refer [reg-event-db reg-sub]]))

(defn add [db error]
  (update-in db [:meta :app :errors] (fnil conj []) error))

(defn add-many [db errors]
  (update-in db [:meta :app :errors] (fnil into []) errors))

(defn clear [db]
  (assoc-in db [:meta :app :errors] nil))

(defn update-list [db f]
  (update-in db [:meta :app :errors] f))

(reg-event-db
 ::add
 (fn [db [_ error]] (add db error)))

(reg-event-db
 ::add-many
 (fn [db [_ errors]] (add-many db errors)))

(reg-event-db
 ::clear
 (fn [db] (clear db)))

(reg-sub
 ::errors
 (fn [db] (get-in db [:meta :app :errors])))
