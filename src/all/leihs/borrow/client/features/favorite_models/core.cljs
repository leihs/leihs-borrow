(ns leihs.borrow.client.features.favorite-models.core
  #_(:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui] 
   [leihs.borrow.client.features.search-models.core :as search-models]))

(def query
  (rc/inline "leihs/borrow/client/features/favorite_models/getFavoriteModels.gql"))

;-; EVENTS 
(rf/reg-event-fx
 ::routes/models-favorites
 (fn [_ _]
   {:dispatch [::re-graph/query
               query
               nil
               [::on-fetched-models-favorites]]}))

(rf/reg-event-fx
 ::on-fetched-models-favorites
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (merge db data)})))

(rf/reg-sub ::favorite-models (fn [db] (:models db)))


(defn view []
  (let [models @(rf/subscribe [::favorite-models])]
    [:<>
     [:header.mx-3.my-4
      [:h1.text-3xl.font-extrabold.leading-none
       "Favorites"]]
     #_[:p (pr-str models)]
     (cond
       (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
       (empty? models) [:p.p-6.w-full.text-center "nothing found!"]
       :else
       [:<>
        (search-models/models-list (:edges models))
        [:hr]
        [:div.p-3.text-center
         [:button.border.border-black.p-2.rounded
          {:on-click #(rf/dispatch [::pagination/get-more
                                    query
                                    {}
                                    [:models]
                                    [:models]])}
          "LOAD MORE"]]])]))
