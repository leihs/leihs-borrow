(ns leihs.borrow.features.current-user.core
  (:require
   ["date-fns" :as datefn]
   [ajax.core :refer [POST]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.features.customer-orders.current-lendings-status :as current-lendings-status]
   [leihs.borrow.features.languages.core :as languages]
   [leihs.borrow.lib.browser-storage :as browser-storage]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [dispatch-sync reg-event-db reg-event-fx
                                      reg-sub]]
   leihs.borrow.lib.re-graph
   [leihs.core.core :refer [dissoc-in]]
   [re-frame.db :as db]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]))

(def query (rc/inline "leihs/borrow/features/current_user/core.gql"))

(def last-fetched (atom nil))

(defn fetch-and-save [callback last-delegation-id]
  (let [db @db/app-db
        user-id (get-in db [:ls ::data :user :id])]
    (if (and user-id
             (datefn/isBefore (js/Date.)
                              (datefn/addHours @last-fetched 1)))
      (callback)
      (let [current-delegation-id (get-in db [:ls ::data :current-delegation :id])
            delegation-id (if user-id current-delegation-id last-delegation-id)]
        (POST (str js/window.location.origin
                   (-> leihs.borrow.lib.re-graph/config :http :url))
          {:params {:query query
                    :variables {:includeDelegation (boolean delegation-id)
                                :delegationId delegation-id
                                :includeLanguages (nil? @last-fetched)}}
           :headers leihs.borrow.lib.re-graph/headers
           :format :json
           :handler #(do
                       (dispatch-sync [::on-fetched-data (h/keywordize-keys %)])
                       (if @last-fetched
                         (swap! last-fetched datefn/addHours 1)
                         (reset! last-fetched (js/Date.)))
                       (callback))})))))

(reg-event-fx
 ::fetch
 (fn-traced [_ [_ _]]
   {:dispatch [::re-graph/query query {} [::on-fetched-data]]}))

(reg-event-fx
 ::on-fetched-data
 (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:dispatch [::errors/add-many errors]}
     {:dispatch-n (let [current-user-data (:current-user data)
                        current-delegation (:delegation data)
                        session-id (:session-id current-user-data)
                        ls-session-id (-> db :ls ::data :session-id)
                        languages-data (:languages data)
                        cart-data (-> current-user-data :user :unsubmitted-order)
                        current-lendings (:current-lendings data)]
                    (list (when (not= session-id ls-session-id)
                            [::browser-storage/clear-session-storage])
                          [::set (merge current-user-data
                                        (when (and current-delegation
                                                   (some #(= (:id %) (:id current-delegation))
                                                         (-> current-user-data :user :delegations)))
                                          {:current-delegation current-delegation}))]
                          [:leihs.borrow.features.shopping-cart.core/set cart-data]
                          (when (seq languages-data)
                            [::languages/set-languages languages-data])
                          [::current-lendings-status/set-current-lendings current-lendings]))})))

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

(defn get-locale-to-use [db]
  (-> db (get-in [:ls ::data :language-to-use :locale]) keyword))

(reg-sub ::locale-to-use
         (fn [db _] (get-locale-to-use db)))

(reg-event-db ::set-locale-to-use
              (fn-traced [db [_ user]]
                (assoc-in db [:ls ::data :language-to-use :locale] (:language_locale user))))
