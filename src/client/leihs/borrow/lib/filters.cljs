(ns leihs.borrow.lib.filters
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:refer-clojure :exclude [key])
  (:require
    [akiroz.re-frame.storage :refer [persist-db]]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-frame.std-interceptors :refer [path]]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.components :as ui]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.components :as ui]
    [leihs.borrow.features.favorite-models.events :as favs]))

(def BOOLEANS #{:available-between?})

(defn massage-values [fs]
  (->> fs
       (map (fn [[k v]]
              [k (cond-> v (BOOLEANS k) js/JSON.parse)]))
       (into {})))

(def filters-gql
  (rc/inline "leihs/borrow/lib/getFilters.gql"))

(def current-path [:ls ::data :current])

(reg-event-fx
  ::init
  (fn-traced [_ _]
             {:dispatch [::re-graph/query
                         filters-gql
                         {}
                         [::on-fetched]]}))

(reg-event-fx
  ::on-fetched
  (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
             (if errors
               {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
               {:db (update-in db
                               current-path
                               (fnil merge {}) 
                               {:quantity 1})})))

(reg-event-db
  ::set-multiple
  [(path current-path)]
  (fn [old [_ filters]] (merge old (massage-values filters))))

(reg-event-db
  ::set-one
  (fn [db [_ key value]]
    (assoc-in db (conj current-path key) value)))

(reg-event-db
  ::clear-current
  [(path current-path)]
  (fn [_ _] nil))

(defn get-from-current [db k]
  (get-in db (conj current-path k)))

(reg-sub
  ::available
  (fn [db _] (get-in db [:ls ::filters :available])))

(reg-sub
  ::current
  (fn [db _] (get-in db current-path)))

(reg-sub
  ::term
  (fn [db _] (get-from-current db :term)))

(reg-sub
  ::start-date
  (fn [db _] (get-from-current db :start-date)))

(reg-sub
  ::end-date
  (fn [db _] (get-from-current db :end-date)))

(reg-sub
  ::available-between?
  (fn [db _] (boolean (get-from-current db :available-between?))))

(reg-sub
  ::quantity
  (fn [db _] (get-from-current db :quantity)))

(defn current [db] (get-in db current-path nil))
(defn term [db] (get-in db (conj current-path :term)))
(defn start-date [db] (get-in db (conj current-path :start-date)))
(defn end-date [db] (get-in db (conj current-path :end-date)))
