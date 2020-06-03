(ns leihs.borrow.client.features.favorite-models.core
  #_(:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.filters :as filters]
   [leihs.borrow.client.lib.localstorage :as ls]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui] 
   [leihs.borrow.client.features.models.core :as models]))

(def EXTRA-PARAMS {:isFavorited true})

;-; EVENTS 
(rf/reg-event-fx
  ::routes/models-favorites
  (fn [_ _]
    {:dispatch [::models/get-models EXTRA-PARAMS]}))

(rf/reg-event-fx
  ::clear
  (fn [_ _]
    {:dispatch-n (list [::filters/clear-current]
                       [::clear-results]
                       [:routing/navigate [::routes/models-favorites]])}))

(defn view []
  (let [models @(rf/subscribe [::models/results])]
    [:<>
     [:header.mx-3.my-4
      [:h1.text-3xl.font-extrabold.leading-none
       "Favorites"]]
     [models/search-and-list
      #(rf/dispatch [:routing/navigate
                     [::routes/models-favorites {:query-params %}]])
      #(rf/dispatch [::clear])
      EXTRA-PARAMS]]))
