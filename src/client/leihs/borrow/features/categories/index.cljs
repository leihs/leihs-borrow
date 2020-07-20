(ns leihs.borrow.features.categories.index
  #_(:require-macros [leihs.borrow.macros :refer [spy]])
  (:require
    #_[reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    #_[clojure.string :refer [join split replace-first]]
    #_[leihs.borrow.features.models.core :as models]
    #_[leihs.borrow.lib.routing :as routing]
    #_[leihs.borrow.lib.pagination :as pagination]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.features.categories.core :as categories]))

; is kicked off from router when this view is loaded
(reg-event-fx ::routes/categories-index
              categories/dispatch-fetch-index-handler)

(defn view []
  (fn []
    (let [cats @(subscribe [::categories/categories-index])]
      [:<>

       [:div.mx-4.mt-6
        [:h2.font-extrabold.text-3xl "Categories"]]

       [:div.pb-8
        (categories/categories-list cats)]])))