(ns leihs.borrow.client.features.categories.index
  #_(:require-macros [leihs.borrow.client.macros :refer [spy]])
  (:require
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [clojure.string :refer [join split replace-first]]
   #_[leihs.borrow.client.features.search-models.core :as search-models]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   #_[leihs.borrow.client.components :as ui]
   [leihs.borrow.client.features.categories.core :as categories]))

(def query
  (rc/inline "leihs/borrow/client/features/categories/getCategories.gql"))

; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/categories-index
 (fn [_ [_ ]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/client/features/categories/getCategories.gql")
               {}
               [::on-fetched-categories-index]]}))

(rf/reg-event-fx
 ::on-fetched-categories-index
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:categories :index] (get-in data [:categories]))})))


(defn view []
  (fn []
    (let [cats @(rf/subscribe [::categories/categories-index])]
      [:<>

      [:div.mx-4.mt-6
       [:h2.font-extrabold.text-3xl "Categories"]]

       [:div.pb-8
        (categories/categories-list cats)]])))
