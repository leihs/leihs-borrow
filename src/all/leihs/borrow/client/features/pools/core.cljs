(ns leihs.borrow.client.features.pools.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
    [better-cond.core :as bc]
    [re-frame.core :as rf]
    [leihs.borrow.client.features.current-user.core :as current-user]))

(defn badge [pool]
  (bc/cond (some #(= (get-in % [:inventoryPool :id]) (:id pool))
                 @(rf/subscribe [::current-user/suspensions]))
           [:div.badge.badge-danger.badge-pill "Your access is suspended"]

           let [has-reservable-items? (:hasReservableItems pool)]

           (not has-reservable-items?)
           [:div.badge.badge-secondary.badge-pill "No reservable items"]

           let [max-res-time (:maximumReservationTime pool)]

           max-res-time
           [:div.badge.badge-warning.badge-pill
            (str "Maximum reservation of " max-res-time " days")]))
