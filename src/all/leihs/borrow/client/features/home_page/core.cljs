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
   [leihs.borrow.client.features.search-models.core :as search-models]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/home
 (fn [_ [_ _]] 
   {:dispatch-n (list [::search-models/fetch-search-filters])}))

(defn category-grid-item [category]
  (let [href (routing/path-for ::routes/categories-show
                               :categories-path (:id category))]
    [:div.ui-category-grid-item.max-w-sm.rounded.overflow-hidden.bg-white.px-2.mb-3
     {:style {:opacity 1}}
     [ui/image-square-thumb (get-in category [:images 0]) href]
     [:div.mx-0.mt-1.leading-snug
      [:a {:href href}
       [:span.block.truncate.font-bold (:label category)]]]]))

(defn categories-list [categories]
  (let
   [debug? @(rf/subscribe [:is-debug?])]
    [:div.mx-1.mt-2
     [:div.w-full.px-0
      [:div.ui-models-list.flex.flex-wrap
       (doall
        (for [category categories]
          [:div {:class "w-1/2 min-h-16" :key (:id category)}
           [category-grid-item category]]))]]
     (when debug? [:p (pr-str @(rf/subscribe [::search-results]))])]))

(defn view []
  (fn []
    (let [{categories :mainCategories :as _available-filters}
          @(rf/subscribe [::search-models/available-filters])]
      [:<>
       [search-models/search-panel]
       [:hr.border-b-2]
       
       [:div
        [:div.mt-2.mx-3.flex.items-baseline.justify-between
         [:h2.font-bold.text-2xl "Categories"]
         #_[:a.font-semibold.text-l {:href "TODO"} "All"]]
        (categories-list categories)]
       
       [:hr.border-b-2]
       #_[:p (pr-str _available-filters)]])))
