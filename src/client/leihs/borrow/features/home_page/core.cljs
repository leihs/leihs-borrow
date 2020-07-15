(ns leihs.borrow.features.home-page.core
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    #_[reagent.core :as r]
    [re-frame.core :as rf]
    #_[re-graph.core :as re-graph]
    #_[shadow.resource :as rc]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.features.models.core :as models]
    [leihs.borrow.features.categories.core :as categories]))

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/home
  (fn [_ [_ {:keys [query-params]}]] 
    {:dispatch-n (-> (list [::categories/fetch-index 4])
                     (cond-> (seq query-params)
                       (conj [::filters/set-multiple query-params]))
                     (conj [::filters/init]))}))

(defn view []
  (fn []
    (let [cats @(subscribe [::categories/categories-index])]
      [:<>

       [models/search-panel #(dispatch [:routing/navigate [::routes/models {:query-params %}]])]
       [:hr]

       [:div
        [:div.mt-2.mx-3.d-flex.align-items-baseline.justify-content-between
         [:h2.font-bold.text-2xl "Categories"]
         [:a.font-semibold.text-l {:href (routing/path-for ::routes/categories-index)} 
          "All"]]
        (categories/categories-list (take 4 cats))]

       [:hr]])))
