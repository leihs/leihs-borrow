(ns leihs.borrow.features.favorite-models.core
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    #_[reagent.core :as r]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    #_[leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.lib.pagination :as pagination]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.components :as ui] 
    ["/leihs-ui-client-side-external-react" :as UI]
    [leihs.borrow.features.models.core :as models]))

(set-default-translate-path :borrow.favorite-models)

(def EXTRA-PARAMS {:isFavorited true})

;-; EVENTS 
(reg-event-fx
  ::routes/models-favorites
  (fn-traced [_ _]
    {:dispatch [::models/get-models EXTRA-PARAMS]}))

(reg-event-fx
  ::clear
  (fn-traced [_ _]
    {:dispatch-n (list [::filters/clear-current]
                       [::clear-results]
                       [:routing/navigate [::routes/models-favorites]])}))

(defn view []
  (let [models @(subscribe [::models/data])]
    [:> UI/Components.AppLayout.Page
     {:title (t :title)}
     
     [models/search-and-list
      #(dispatch [:routing/navigate
                  [::routes/models-favorites {:query-params %}]])
      #(dispatch [::clear])
      EXTRA-PARAMS]]))
