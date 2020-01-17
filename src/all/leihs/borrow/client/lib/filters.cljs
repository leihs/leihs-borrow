(ns leihs.borrow.client.lib.filters
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.features.favorite-models.events :as favs]))

(def search-filters-gql
  (rc/inline "leihs/borrow/client/lib/getSearchFilters.gql"))

(rf/reg-event-fx
  ::fetch
  (fn [_ [_ _]]
    {:dispatch [::re-graph/query
                search-filters-gql
                {}
                [::on-fetched]]}))

(rf/reg-event-fx
  ::on-fetched
  (fn [{:keys [db]} [_ {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:db (assoc-in db [:search :filters :available] data)})))

(rf/reg-event-db
  ::set-all
  (fn [db [_ filters]]
    (assoc-in db [:search :filters :current] filters)))

(rf/reg-event-db
  ::set-one
  (fn [db [_ key value]]
    (assoc-in db [:search :filters :current key] value)))

(rf/reg-sub
  ::available
  (fn [db] (get-in db [:search :filters :available] nil)))

(rf/reg-sub
  ::current
  (fn [db] (get-in db [:search :filters :current] nil)))
