(ns leihs.borrow.features.current-user.core
  (:require
    [ajax.core :refer [GET POST]]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    ["date-fns" :as datefn]
    [re-frame.core :as rf]
    [re-frame.db :as db]
    [re-graph.core :as re-graph]
    [re-frame.std-interceptors :refer [path]]
    [shadow.resource :as rc]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch
                                       dispatch-sync]]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.lib.helpers :as help :refer [spy]]
    leihs.borrow.lib.re-graph
    [leihs.borrow.client.routes :as routes]))

(def query (rc/inline "leihs/borrow/features/current_user/core.gql"))

(def last-fetched (atom nil))

(defn fetch-and-save [callback]
  (if (and (get-in @db/app-db [:ls ::data :user :id])
           (datefn/isBefore (js/Date.)
                            (datefn/addHours @last-fetched 1)))
    (callback)
    (POST (str js/window.location.origin
               (-> leihs.borrow.lib.re-graph/config :http :url))
          {:params {:query query}
           :headers leihs.borrow.lib.re-graph/headers
           :format :json
           :handler #(do
                       (dispatch-sync [::on-fetched-data (help/keywordize-keys %)])
                       (if @last-fetched
                         (swap! last-fetched datefn/addHours 1)
                         (reset! last-fetched (js/Date.)))
                       (callback))})))

(reg-event-fx
  ::fetch
  (fn-traced [_ [_ _]]
    {:dispatch [::re-graph/query query {} [::on-fetched-data]]}))

(reg-event-fx
  ::on-fetched-data
  (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil into []) errors)}
      {:dispatch-n (let [current-user-data (:current-user data)
                         response-user-id (-> current-user-data :user :id)
                         ls-user-id  (-> db :ls ::data :user :id)]
                     (list (when (not= response-user-id ls-user-id) [::ls/clear])
                           [::set current-user-data]))})))

(reg-event-db
  ::set
  (fn-traced [db [_ user-data]]
    (assoc-in db [:ls ::data] user-data)))

(defn data [db]
  (-> db :ls ::data))

(reg-sub ::data
         (fn [db _] (get-in db [:ls ::data])))

(reg-sub ::user-data
         :<- [::data]
         (fn [cu _] (:user cu)))

(reg-sub ::pools
         :<- [::user-data]
         (fn [u _] (:inventory-pools u)))

(reg-sub ::delegations
         :<- [::user-data]
         (fn [u _] (:delegations u)))

(reg-sub ::target-users
         :<- [::user-data]
         (fn [user-data [_ suffix]]
           (let [user (cond-> user-data
                        suffix
                        (update :name str suffix))]
             (concat [user] (:delegations user-data)))))

(reg-sub ::suspensions
         :<- [::user-data]
         (fn [u _] (:suspensions u)))
