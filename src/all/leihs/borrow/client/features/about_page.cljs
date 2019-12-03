(ns leihs.borrow.client.features.about-page
  (:require
   [clojure.string :as string]
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   #_[leihs.borrow.client.components :as ui]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/about-page
 (fn [_ [_ _]] {}))

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
  [:section.m-3
   [:h1.text-xl.font-black "ABOUT"]
   [:hr.mt-2.mb-4]
   [ui/tmp-nav]
   [:h2.sr-only "Links"]
   [:ul.mb-4.list-disc.list-inside
    [:li [:a {:href "/borrow/graphiql/index.html"} "Graph" [:i "i"] "QL API console"]]]
   [:h2 "Debug Info"]
   [:p.font-mono.text-xs
    [:table>tbody
     (doall
      (for [[ix [k v]] (map-indexed vector (get-about-page-data))]
        [:tr {:key ix}
         [:td.pr-2.pb-1 k]
         [:td.pl-2.pb-1 v]]))]]])
    