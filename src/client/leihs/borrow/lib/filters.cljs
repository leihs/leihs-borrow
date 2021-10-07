(ns leihs.borrow.lib.filters
  (:refer-clojure :exclude [key])
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
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
   [leihs.core.core :refer [flip presence]]
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.lib.helpers :refer [spy spy-with log]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]))

(def BOOLEANS #{:only-available})
(def INTEGERS #{:quantity})

(defn massage-values [m]
  (->> m
       (map (fn [[k v]]
              [k (cond (BOOLEANS k) (js/JSON.parse v)
                       (INTEGERS k) (js/Number v)
                       :else v)]))
       (into {})))

(defn remove-blanks [m]
  (->> m
       (filter (fn [[_ v]] (presence v)))
       (into {})))

(def filters-gql
  (rc/inline "leihs/borrow/lib/getFilters.gql"))

(def current-path [:ls ::data :current])

(def DEFAULTS {:quantity 1})

(defn get-defaults [db]
  (merge DEFAULTS
         {:user-id (-> db current-user/data :user :id)}))

(reg-event-db
 ::on-fetched
 (fn-traced [db [_ {:keys [data errors]}]]
   (if errors
     (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)
     (let [defaults (get-defaults db)]
       (update-in db
                  current-path
                  (flip merge)
                  defaults)))))

(defn clear-current [db]
  (let [defaults (get-defaults db)]
    (assoc-in db current-path defaults)))

(reg-event-db
 ::clear-current
 (fn-traced [db _] (clear-current db)))

(reg-event-db
 ::set-multiple
 (fn-traced [db [_ filters]]
   (let [defaults (get-defaults db)]
     (update-in db
                current-path
                #(merge defaults
                        %
                        (some-> filters
                                not-empty
                                massage-values
                                remove-blanks))))))

(reg-event-db
 ::set-one
 (fn-traced [db [_ key value]]
   (assoc-in db (conj current-path key) value)))

(reg-event-db
 ::set-pool-id
 [(path current-path)]
 (fn-traced [old [_ value]]
   (if (#{"all" nil} value)
     (dissoc old :pool-id)
     (assoc old :pool-id value))))

(defn get-from-current [db k]
  (get-in db (conj current-path k)))

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
 ::only-available
 (fn [db _] (boolean (get-from-current db :only-available))))

(reg-sub
 ::quantity
 (fn [db _] (or (get-from-current db :quantity) 1)))

(reg-sub
 ::user-id
 (fn [db _] (get-from-current db :user-id)))

(reg-sub
 ::pool-id
 (fn [db _] (get-from-current db :pool-id)))

(defn current [db] (get-in db current-path nil))
(defn term [db] (get-in db (conj current-path :term)))
(defn start-date [db] (get-in db (conj current-path :start-date)))
(defn end-date [db] (get-in db (conj current-path :end-date)))
(defn user-id [db] (get-in db (conj current-path :user-id)))
(defn pool-id [db] (get-in db (conj current-path :pool-id)))
