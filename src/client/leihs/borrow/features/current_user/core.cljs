(ns leihs.borrow.features.current-user.core
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [re-frame.std-interceptors :refer [path]]
   [shadow.resource :as rc]
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]))

(rf/reg-event-fx
  ::fetch
  (fn [_ [_ _]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/current_user/core.gql")
                {}
                [::on-fetched-data]]}))

(ls/reg-event-db
  ::on-fetched-data
  (fn [db [_ {:keys [data errors]}]]
     (if errors
       (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)
       (assoc-in db [:ls ::data] (:currentUser data)))))

(rf/reg-sub ::data
            (fn [db _] (get-in db [:ls ::data])))

(rf/reg-sub ::pools
            :<- [::data]
            (fn [cu _] (:inventoryPools cu)))

(rf/reg-sub ::suspensions
            :<- [::data]
            (fn [cu _] (:suspensions cu)))

