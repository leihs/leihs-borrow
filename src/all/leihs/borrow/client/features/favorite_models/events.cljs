(ns leihs.borrow.client.features.favorite-models.events
  #_(:require-macros [leihs.borrow.client.macros :refer [spy]])
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
     (rc/inline "leihs/borrow/client/features/favorite_models/getFavoriteModels.gql")
     {:modelId model-id}
     [::on-mutation-result]]}))

(rf/reg-event-fx
 ::unfavorite-model
 (fn [_ [_ model-id]]
   {:dispatch
    [::re-graph/mutate
     (rc/inline "leihs/borrow/client/features/favorite_models/getFavoriteModels.gql")
     {:modelId model-id}
     [::on-mutation-result]]}))

(rf/reg-event-fx
 ::on-favorite-model-result
 (fn [{:keys [_db]} [_ {:keys [_data errors]}]]
   (when errors
     {:alert (str "FAIL! " (pr-str errors))})))