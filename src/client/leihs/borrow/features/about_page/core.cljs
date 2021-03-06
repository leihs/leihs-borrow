(ns leihs.borrow.features.about-page.core
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [clojure.string :as string]
    #_[reagent.core :as r]
    [re-frame.core :as rf]
    #_[re-graph.core :as re-graph]
    #_[shadow.resource :as rc]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.lib.translate :as translate]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.components :as ui]
    [leihs.borrow.client.routes :as routes]
    ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.about-page)

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/about-page
  (fn-traced [_ [_ _]] {}))

(defn matches-media-query? [media-query]
  (-> (js/window.matchMedia media-query) .-matches))

(defn deco-bool [bool] (if bool "yes" "no"))

(defn- get-about-page-data []
  [["browser time" (js/String (js/Date.))]
   ["browser timezone" (-> (js/Intl.DateTimeFormat. :default) .resolvedOptions .-timeZone)]
   ["browser language(s)" (string/join ", " (or (.-languages js/navigator) [(.-language js/navigator)]))]
   ["CSS dark mode" (deco-bool (matches-media-query? "(prefers-color-scheme: dark)"))]
   ["CSS reduced motion" (deco-bool (matches-media-query? "(prefers-reduced-motion: reduce)"))]
   ["CSS pointer support" (cond
                            (matches-media-query? "(pointer: coarse)") "coarse (e.g. touch)"
                            (matches-media-query? "(pointer: fine)") "fine (e.g mouse)"
                            :else "unknown")]])

(defn view []
  [:> UI/Components.AppLayout.Page
   {:title (t :title)}

   [ui/tmp-nav]
   [ui/dev-nav]

   [:button.btn.btn-secondary.dont-invert.mx-1.mb-4
    {:type :button
     :on-click #(dispatch [::ls/clear])
     :class :mt-2}
    "Clear :ls"]
   [:hr.mt-2.mb-4]
   [:h2 "Debug Info"]
   [:div.text-monospace.text-xs
    [:table>tbody
     (doall
       (for [[ix [k v]] (map-indexed vector (get-about-page-data))]
         [:tr {:key ix}
          [:td.pr-2.pb-1 k]
          [:td.pl-2.pb-1 v]]))]]])

