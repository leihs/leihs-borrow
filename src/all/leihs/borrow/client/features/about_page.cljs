(ns leihs.borrow.client.features.about-page
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   #_[leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   #_[leihs.borrow.client.components :as ui]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/about-page
 (fn [_ [_ _]] {}))

(defn- get-about-page-data []
  [["browser time" (js/String (js/Date.))]
   ["browser language" (.-language js/navigator)]])

(defn view []
  [:section.m-3
   [:h1.text-xl.font-black "ABOUT & DEBUG "]
   [:hr.mt-2.mb-4]
   [:h2 "Links"]
   [:ul.mb-4.list-disc.list-inside
    [:li [:a {:href "/borrow/graphiql/index.html"} "Graph" [:i "i"] "QL API console"]]]
   [:h2 "Data"]
   [:p.font-mono.text-xs {:style {:white-space "pre-wrap"
                                  :overflow-wrap "break-word"
                                  :word-break "break-all"}}
    [:table>tbody
     (doall
      (for [[ix [k v]] (map-indexed vector (get-about-page-data))]
        [:tr {:key ix}
         [:td.pr-2.pb-1 k]
         [:td.pl-2.pb-1 v]]))]]])
    