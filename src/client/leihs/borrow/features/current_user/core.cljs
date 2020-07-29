(ns leihs.borrow.features.current-user.core
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [re-frame.std-interceptors :refer [path]]
    [shadow.resource :as rc]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.client.routes :as routes]))

(reg-event-fx
  ::fetch
  (fn-traced [_ [_ _]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/current_user/core.gql")
                {}
                [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ {:keys [data errors]}]]
    (if errors
      (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)
      (assoc-in db [::data] (:current-user data)))))

(reg-sub ::data
         (fn [db _] (get-in db [::data])))

(reg-sub ::pools
         :<- [::data]
         (fn [cu _] (:inventory-pools cu)))

(reg-sub ::suspensions
         :<- [::data]
         (fn [cu _] (:suspensions cu)))
