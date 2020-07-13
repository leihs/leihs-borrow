(ns leihs.borrow.features.favorite-models.events
  #_(:require-macros [leihs.borrow.macros :refer [spy]])
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]))

(rf/reg-event-fx
 ::favorite-model
 (fn [_ [_ model-id]]
   {:dispatch
    [::re-graph/mutate
     (rc/inline "leihs/borrow/features/favorite_models/setModelFavorite.gql")
     {:modelId model-id :isFav true}
     [::on-mutation-result]]}))

(rf/reg-event-fx
 ::unfavorite-model
 (fn [_ [_ model-id]]
   {:dispatch
    [::re-graph/mutate
     (rc/inline "leihs/borrow/features/favorite_models/setModelFavorite.gql")
     {:modelId model-id :isFav false}
     [::on-mutation-result]]}))

(rf/reg-event-fx
 ::on-mutation-result
 (fn [{:keys [_db]} [_ {:keys [_data errors]}]]
   (when errors
     {:alert (str "FAIL! " (pr-str errors))})))
