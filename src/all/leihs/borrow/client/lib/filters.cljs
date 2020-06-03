(ns leihs.borrow.client.lib.filters
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:refer-clojure :exclude [key])
  (:require
    [akiroz.re-frame.storage :refer [persist-db]]
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-frame.std-interceptors :refer [path]]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.client.lib.localstorage :as ls]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.features.favorite-models.events :as favs]))

(def BOOLEANS #{:available-between?})

(defn massage-values [fs]
  (->> fs
       (map (fn [[k v]]
              [k (cond-> v (BOOLEANS k) js/JSON.parse)]))
       (into {})))

(def filters-gql
  (rc/inline "leihs/borrow/client/lib/getFilters.gql"))

(def current-path [:ls ::filters ::current])

(rf/reg-event-fx
  ::init
  (fn [_ _]
    {:dispatch [::re-graph/query
                filters-gql
                {}
                [::on-fetched]]}))

(ls/reg-event-fx
  ::on-fetched
  (fn [{:keys [db]} [_ {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:db (assoc-in db [:ls ::filters] {::available data
                                         ::current {:quantity 1}})})))

(ls/reg-event-db
  ::set-multiple
  [(path current-path)]
  (fn [old [_ filters]] (merge old (massage-values filters))))

(ls/reg-event-db
  ::set-one
  (fn [db [_ key value]]
    (assoc-in db (conj current-path key) value)))

(ls/reg-event-db
  ::clear-current
  [(path current-path)]
  (fn [_ _] nil))

(defn get-from-current [db k]
  (get-in db (conj current-path k)))

(rf/reg-sub
  ::available
  (fn [db _] (get-in db [:ls ::filters ::available])))

(rf/reg-sub
  ::current
  (fn [db _] (get-in db current-path)))

(rf/reg-sub
  ::term
  (fn [db _] (get-from-current db :term)))

(rf/reg-sub
  ::start-date
  (fn [db _] (get-from-current db :start-date)))

(rf/reg-sub
  ::end-date
  (fn [db _] (get-from-current db :end-date)))

(rf/reg-sub
  ::available-between?
  (fn [db _] (boolean (get-from-current db :available-between?))))

(rf/reg-sub
  ::quantity
  (fn [db _] (get-from-current db :quantity)))

(defn current [db] (get-in db current-path nil))
(defn term [db] (get-in db (conj current-path :term)))
(defn start-date [db] (get-in db (conj current-path :start-date)))
(defn end-date [db] (get-in db (conj current-path :end-date)))
