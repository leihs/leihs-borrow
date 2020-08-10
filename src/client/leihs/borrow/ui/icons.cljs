(ns leihs.borrow.ui.icons
  (:require
    #_[reagent.core :as reagent]))

(def menu-icon [:span.ui-icon.ui-menu-icon "☰"])
(def menu-icon-open [:span.ui-icon.ui-menu-icon "✖"])

(def trash-icon [:span.ui-icon.ui-icon-colored.ui-trash-icon "🗑️"])
(def shopping-cart-icon [:span.ui-icon.ui-icon-colored.ui-shopping-cart-icon "🛒"])

(def -heart-icon-styles {:display "inline-block" :text-align "center" :width "1em" :height "1em" :vertical-align "middle" :line-height 1})
(def favorite-yes-icon [:span.ui-icon.ui-favorite-yes-icon
                        [:span {:style -heart-icon-styles} "♥"]])
(def favorite-no-icon [:span.ui-icon.favorite-no-icon
                       [:span {:style (merge -heart-icon-styles {:line-height "0.85em"})}
                        [:span {:style {:font-size "75%"}} "♡"]]])
