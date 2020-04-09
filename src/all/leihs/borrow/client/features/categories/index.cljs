(ns leihs.borrow.client.features.categories.index
  #_(:require-macros [leihs.borrow.client.macros :refer [spy]])
  (:require
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[clojure.string :refer [join split replace-first]]
   #_[leihs.borrow.client.features.search-models.core :as search-models]
   #_[leihs.borrow.client.lib.routing :as routing]
   #_[leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.lib.localstorage :as ls]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.features.categories.core :as categories]))

; is kicked off from router when this view is loaded
(rf/reg-event-fx ::routes/categories-index
                 categories/dispatch-fetch-index-handler)

(defn view []
  (fn []
    (let [cats @(rf/subscribe [::categories/categories-index])]
      [:<>

       [:div.mx-4.mt-6
        [:h2.font-extrabold.text-3xl "Categories"]]
       
       [:div.pb-8
          (categories/categories-list cats)]])))
