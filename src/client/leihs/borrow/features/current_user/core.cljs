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
   [leihs.core.core :refer [dissoc-in]]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch
                                      dispatch-sync]]
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.lib.helpers :as h]
   leihs.borrow.lib.re-graph
   [leihs.borrow.client.routes :as routes]
   [leihs.core.core :refer [presence]]))

(def query (rc/inline "leihs/borrow/features/current_user/core.gql"))

(def last-fetched (atom nil))

(defn fetch-and-save [callback]
  (let [db @db/app-db
        user-id (get-in db [:ls ::data :user :id])
        delegation-id (get-in db [:ls ::data :current-delegation :id])]
    (if (and user-id
             (datefn/isBefore (js/Date.)
                              (datefn/addHours @last-fetched 1)))
      (callback)
      (POST (str js/window.location.origin
                 (-> leihs.borrow.lib.re-graph/config :http :url))
        {:params {:query query
                  :variables {:includeDelegation (boolean delegation-id)
                              :delegationId delegation-id}}
         :headers leihs.borrow.lib.re-graph/headers
         :format :json
         :handler #(do
                     (dispatch-sync [::on-fetched-data (h/keywordize-keys %)])
                     (if @last-fetched
                       (swap! last-fetched datefn/addHours 1)
                       (reset! last-fetched (js/Date.)))
                     (callback))}))))

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
                        current-delegation (:delegation data)
                        session-id (:session-id current-user-data)
                        ls-session-id (-> db :ls ::data :session-id)]
                    (list (when (not= session-id ls-session-id)
                            [::ls/clear])
                          [::set (merge current-user-data
                                        (when (and current-delegation
                                                   (some #(= (:id %) (:id current-delegation))
                                                         (-> current-user-data :user :delegations)))
                                          {:current-delegation current-delegation}))]))})))

(reg-event-db
 ::set
 (fn-traced [db [_ user-data]]
   (assoc-in db [:ls ::data] user-data)))

(reg-event-db
 ::set-current-delegation
 (fn-traced [db [_ delegation]]
   (-> db
       (update-in [:ls] #(select-keys % [::data]))
       (assoc-in [:ls ::data :current-delegation] delegation))))

(reg-event-db
 ::clear-current-delegation
 (fn-traced [db [_ _]]
   (-> db
       (update-in [:ls] #(select-keys % [::data]))
       (dissoc-in [:ls ::data :current-delegation]))))

(defn get-current-user-id [db] (get-in db [:ls ::data :user :id]))
(defn get-current-delegation-id [db] (get-in db [:ls ::data :current-delegation :id]))
(defn get-current-profile-id [db] (or (get-current-delegation-id db) (get-current-user-id db)))

(reg-sub ::data
         (fn [db _] (get-in db [:ls ::data])))

(reg-sub ::user-data
         :<- [::data]
         (fn [cu _] (:user cu)))

(reg-sub ::user-id
         :<- [::user-data]
         (fn [ud _] (:id ud)))

(reg-sub ::current-delegation
         (fn [db] (get-in db [:ls ::data :current-delegation])))

(reg-sub ::current-profile
         :<- [::user-data]
         :<- [::current-delegation]
         (fn [[user-data current-delegation] _]
           (or current-delegation user-data)))

(reg-sub ::current-profile-id
         :<- [::current-profile]
         (fn [profile _]
           (:id profile)))

(reg-sub ::locale
         :<- [::data]
         (fn [dat _] (-> dat :language-to-use :locale)))

(reg-sub ::delegations
         :<- [::user-data]
         (fn [u _] (:delegations u)))

(reg-sub ::can-change-profile?
         :<- [::delegations]
         (fn [delegations _]
           (seq delegations)))

(reg-sub ::user-nav
         :<- [::data]
         (fn [dat _] (-> dat :nav)))

