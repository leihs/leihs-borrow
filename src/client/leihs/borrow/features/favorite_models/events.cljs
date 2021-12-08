(ns leihs.borrow.features.favorite-models.events
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-graph.core :as re-graph]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.features.favorite-models.core :refer [fav-filter]]
   [leihs.borrow.features.models.core :as models]
   [leihs.core.core :refer [dissoc-in]]
   [shadow.resource :as rc]))

(reg-event-db
 ::invalidate-cache
 (fn-traced [db _]
   (let [cache-key (models/get-cache-key {} fav-filter)]
     (dissoc-in db [:ls ::models/data cache-key]))))

(reg-event-fx
 ::favorite-model
 (fn-traced [_ [_ model-id]]
   {:dispatch
    [::re-graph/mutate
     (rc/inline "leihs/borrow/features/favorite_models/setModelFavorite.gql")
     {:modelId model-id :isFav true}
     [::on-mutation-result]]}))

(reg-event-fx
 ::unfavorite-model
 (fn-traced [_ [_ model-id]]
   {:dispatch
    [::re-graph/mutate
     (rc/inline "leihs/borrow/features/favorite_models/setModelFavorite.gql")
     {:modelId model-id :isFav false}
     [::on-mutation-result]]}))

(reg-event-fx
 ::on-mutation-result
 (fn-traced [{:keys [_db]} [_ {:keys [_data errors]}]]
   (when errors
     {:alert (str "FAIL! " (pr-str errors))})))
