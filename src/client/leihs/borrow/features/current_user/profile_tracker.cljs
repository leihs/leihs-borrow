(ns leihs.borrow.features.current-user.profile-tracker
  (:require
   [goog.events :as events]
   [re-frame.db :as db]
   [leihs.borrow.features.current-user.core :as current-user]))

; Tracks the last selected profile (delegation or own user) in localStorage each time 
; the window is focussed or clicked, in order to be able to initialize newly opened tabs/windows 
; with this selection (instead of always falling back to the "personal" profile).

(def ls-key "leihs.borrow.last-delegation-id")

(defn- persist-last-delegation-id []
  (let [db @db/app-db
        user-id (current-user/get-current-user-id db)
        delegation-id (current-user/get-current-delegation-id db)
        last-delegation-id (when user-id delegation-id)]
    (if (boolean last-delegation-id)
      (.setItem js/localStorage ls-key last-delegation-id)
      (.removeItem js/localStorage ls-key))))

(defn track-last-delegation-id []
  (events/listen js/window "focus" persist-last-delegation-id)
  (events/listen js/window "click" persist-last-delegation-id)
  (events/listen js/window "contextmenu" persist-last-delegation-id))

(defn get-last-delegation-id []
  (let [last-delegation-id (.getItem js/localStorage ls-key)]
    last-delegation-id))
