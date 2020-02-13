(ns leihs.borrow.client.features.home-page.core
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.lib.filters :as filters]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.features.search-models.core :as search-models]
   [leihs.borrow.client.features.categories.core :as categories]))

; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/home
 (fn [_ [_ _]] 
   {:dispatch-n (list 
                 [::filters/init]
                 [::categories/fetch-index 4])}))

(defn view []
  (fn []
    (let [cats @(rf/subscribe [::categories/categories-index])]
      [:<>
       
       [search-models/search-panel]
       [:hr]
       
       [:div
        [:div.mt-2.mx-3.d-flex.align-items-baseline.justify-content-between
         [:h2.font-bold.text-2xl "Categories"]
         [:a.font-semibold.text-l {:href (routing/path-for ::routes/categories-index)} 
          "All"]]
        (categories/categories-list (take 4 cats))]
       
       [:hr]])))
