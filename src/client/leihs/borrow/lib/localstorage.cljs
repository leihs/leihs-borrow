(ns leihs.borrow.lib.localstorage
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    [akiroz.re-frame.storage :refer [persist-db]]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
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

(reg-event-db ::clear
              [(path :ls)]
              (fn-traced [ls _] {}))
