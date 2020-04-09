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

(def filters-gql
  (rc/inline "leihs/borrow/client/lib/getFilters.gql"))

(def current-path [::filters ::current])

(rf/reg-event-fx
  ::init
  (fn [_ _]
    {:dispatch [::re-graph/query
                filters-gql
                {}
                [::on-fetched]]}))

(ls/reg-event-fx-ls
  ::on-fetched
  (fn [{:keys [db]} [_ {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:db (assoc-in db [:ls ::filters ::available] data)})))

(ls/reg-event-ls
  ::set-all
  [(path current-path)]
  (fn [_ [_ filters]] filters))

(ls/reg-event-ls
  ::set-one
  (fn [ls [_ key value]]
    (assoc-in ls (conj current-path key) value)))

(ls/reg-event-ls
  ::clear-current
  [(path current-path)]
  (fn [_ _] nil))

(ls/reg-sub-ls
  ::available
  (fn [ls _] (get-in ls [::filters ::available] nil)))

(ls/reg-sub-ls
  ::current
  (fn [ls _] (get-in ls current-path nil)))

(ls/reg-sub-ls
  ::term
  (fn [ls _] (get-in ls (conj current-path :term))))

(ls/reg-sub-ls
  ::start-date
  (fn [ls _] (get-in ls (conj current-path :start-date))))

(ls/reg-sub-ls
  ::end-date
  (fn [ls _] (get-in ls (conj current-path :end-date))))

(defn current [db] (ls/get-in db current-path nil))
(defn term [db] (ls/get-in db (conj current-path :term)))
(defn start-date [db] (ls/get-in db (conj current-path :start-date)))
(defn end-date [db] (ls/get-in db (conj current-path :end-date)))
