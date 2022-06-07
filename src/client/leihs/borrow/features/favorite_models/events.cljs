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
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.favorite-models.core :refer [fav-filter]]
   [leihs.borrow.features.models.core :as models]
   [leihs.core.core :refer [dissoc-in]]
   [shadow.resource :as rc]))

(reg-event-db
 ::invalidate-cache
 (fn-traced [db _]
   (let [query-vars (models/get-query-vars {} fav-filter (current-user/get-current-profile-id db))
         cache-key (models/get-cache-key query-vars)]
     (dissoc-in db [:ls ::models/data cache-key]))))

(reg-event-fx
 ::favorite-model
 (fn-traced [{:keys [db]} [_ model-id db-path]]
   {:db (assoc-in db db-path true)
    :dispatch
    [::re-graph/mutate
     (rc/inline "leihs/borrow/features/favorite_models/setModelFavorite.gql")
     {:modelId model-id :isFav true :userId (current-user/get-current-profile-id db)}
     [::on-mutation-result db-path true]]}))

(reg-event-fx
 ::unfavorite-model
 (fn-traced [{:keys [db]} [_ model-id db-path]]
   {:db (assoc-in db db-path false)
    :dispatch
    [::re-graph/mutate
     (rc/inline "leihs/borrow/features/favorite_models/setModelFavorite.gql")
     {:modelId model-id :isFav false :userId (current-user/get-current-profile-id db)}
     [::on-mutation-result db-path false]]}))

(reg-event-fx
 ::on-mutation-result
 (fn-traced [{:keys [db]} [_ db-path flag {:keys [_data errors]}]]
   (if errors
     {:dispatch [::errors/add-many errors]
      :db (assoc-in db db-path (not flag))}
     {:dispatch [::invalidate-cache]})))
