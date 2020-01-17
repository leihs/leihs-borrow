(ns leihs.borrow.client.lib.filters
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:refer-clojure :exclude [key])
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.features.favorite-models.events :as favs]))

(def filters-gql
  (rc/inline "leihs/borrow/client/lib/getFilters.gql"))

(rf/reg-event-fx
  ::init
  (fn [_ _]
    {:dispatch [::re-graph/query
                filters-gql
                {}
                [::on-fetched]]}))

(rf/reg-event-fx
  ::on-fetched
  (fn [{:keys [db]} [_ {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:db (assoc-in db [::filters ::available] data)})))

(rf/reg-event-db
  ::set-all
  (fn [db [_ filters]]
    (assoc-in db [::filters ::current] filters)))

(rf/reg-event-db
  ::set-one
  (fn [db [_ key value]]
    (assoc-in db [::filters ::current key] value)))

(rf/reg-sub
  ::available
  (fn [db] (get-in db [::filters ::available] nil)))

(defn current [db]
  (get-in db [::filters ::current] nil))

(rf/reg-sub ::current (fn [db] (current db)))

(rf/reg-sub
  ::term
  (fn [db] (get-in db [::filters ::current ::term])))

(rf/reg-sub
  ::start-date
  (fn [db] (get-in db [::filters ::current ::start-date])))

(rf/reg-sub
  ::end-date
  (fn [db] (get-in db [::filters ::current ::end-date])))
