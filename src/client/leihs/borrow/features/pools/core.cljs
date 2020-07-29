(ns leihs.borrow.features.pools.core
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [better-cond.core :as bc]
    [re-frame.core :as rf]
    [leihs.borrow.lib.re-frame :refer [subscribe]]
    [leihs.borrow.features.current-user.core :as current-user]))

(defn badge [pool]
  (bc/cond (some #(= (get-in % [:inventory-pool :id]) (:id pool))
                 @(subscribe [::current-user/suspensions]))
           [:div.badge.badge-danger.badge-pill "Your access is suspended"]

           let [has-reservable-items? (:has-reservable-items pool)]

           (not has-reservable-items?)
           [:div.badge.badge-secondary.badge-pill "No reservable items"]

           let [max-res-time (:maximum-reservation-time pool)]

           max-res-time
           [:div.badge.badge-warning.badge-pill
            (str "Maximum reservation of " max-res-time " days")]))
