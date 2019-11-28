(ns leihs.borrow.client.features.home-page
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   #_[leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   #_[leihs.borrow.client.components :as ui]
   [leihs.borrow.client.features.search-models :as search-models]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/home
 (fn [_ [_ _]] 
   {:dispatch-n (list [::search-models/fetch-search-filters])}))

(defn view []
  (fn []
    (let [available-filters @(rf/subscribe [::search-models/available-filters])]
      [:<>
       [search-models/search-panel]
       [:hr.border-b-2]
       [:p (pr-str available-filters)]])))