(ns leihs.borrow.client.lib.localstorage
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:refer-clojure :exclude [get get-in])
  (:require
    [akiroz.re-frame.storage :refer [persist-db]]
    [re-frame.core :as rf]
    [re-frame.std-interceptors :refer [path enrich]]))

(def persist-interceptor (persist-db :leihs-borrow :ls))
(def base-interceptors [persist-interceptor (path :ls)])

(defn reg-event-ls
  ([event-id handler]
   (rf/reg-event-db event-id
                    base-interceptors
                    handler))
  ([event-id interceptors handler]
   (rf/reg-event-db event-id
                    (concat base-interceptors interceptors)
                    handler)))

(defn reg-event-fx-ls
  ([id handler]
   (reg-event-fx-ls id [] handler))
  ([id interceptors handler]
   (rf/reg-event-fx
     id
     (into [persist-interceptor]
           interceptors)
     handler)))

(defn reg-sub-ls [id handler]
  (rf/reg-sub id (fn [db] (handler (:ls db)))))

(defn get
  ([db k] (get db [:ls k] nil))
  ([db k d] (clojure.core/get-in db [:ls k] d)))
(defn get-in
  ([db ks] (get-in db ks nil))
  ([db ks d] (clojure.core/get-in db (into [:ls] ks) d)))
