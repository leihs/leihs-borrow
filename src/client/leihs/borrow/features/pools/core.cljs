(ns leihs.borrow.features.pools.core
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [better-cond.core :as bc]
    [re-frame.core :as rf]
    [leihs.borrow.lib.re-frame :refer [subscribe]]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.features.current-user.core :as current-user]))

(set-default-translate-path :borrow.pools)

(defn badge [pool]
  (bc/cond (some #(= (get-in % [:inventory-pool :id]) (:id pool))
                 @(subscribe [::current-user/suspensions]))
           [:div.badge.badge-danger.badge-pill (t :access-suspended)]

           let [has-reservable-items? (:has-reservable-items pool)]

           (not has-reservable-items?)
           [:div.badge.badge-secondary.badge-pill (t :no-reservable-models)]

           let [max-res-time (:maximum-reservation-time pool)]

           max-res-time
           [:div.badge.badge-warning.badge-pill
            (str (t :maximum-reservation-pre)
                 max-res-time
                 (t :maximum-reservation-post))]))
