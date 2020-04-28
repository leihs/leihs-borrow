(ns leihs.borrow.client.lib.localstorage
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
    [akiroz.re-frame.storage :refer [persist-db]]
    [re-frame.core :as rf]
    [re-frame.std-interceptors :refer [path enrich]]))

(def interceptor (persist-db :leihs-borrow :ls))

(defn reg-event-db
  ([event-id handler]
   (rf/reg-event-db event-id
                    [interceptor]
                    handler))
  ([event-id interceptors handler]
   (rf/reg-event-db event-id
                    (concat [interceptor] interceptors)
                    handler)))

(defn reg-event-fx
  ([id handler]
   (reg-event-fx id [] handler))
  ([id interceptors handler]
   (rf/reg-event-fx
     id
     (concat [interceptor] interceptors)
     handler)))

(reg-event-db ::clear (constantly {}))
