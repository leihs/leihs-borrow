(ns leihs.borrow.client.features.home-page.core
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   #_[leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.features.search-models.core :as search-models]
   [leihs.borrow.client.features.categories.core :as categories]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/home
 (fn [_ [_ _]] 
   {:dispatch-n (list 
                 [::search-models/fetch-search-filters]
                 [::categories/fetch-index 4])}))

(defn view []
  (fn []
    (let [cats @(rf/subscribe [::categories/categories-index])]
      [:<>
       [search-models/search-panel]
       [:hr.border-b-2]
       
       [:div
        [:div.mt-2.mx-3.flex.items-baseline.justify-between
         [:h2.font-bold.text-2xl "Categories"]
         [:a.font-semibold.text-l {:href (routing/path-for ::routes/categories-index)} 
          "All"]]
        (categories/categories-list (take 4 cats))]
       
       [:hr.border-b-2]
       #_[:p (pr-str _available-filters)]])))
