(ns leihs.borrow.lib.browser-storage
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.re-frame-storage :refer [persist-db]]
   [re-frame.core :as rf]
   [re-frame.std-interceptors :refer [path]]))

(def local-storage-interceptor (persist-db :borrow-local-storage :ls2 false))

(def session-storage-interceptor (persist-db :borrow-session-storage :ls true))

(rf/reg-event-db ::clear-local-storage
                 [local-storage-interceptor (path :ls2)]
                 (fn-traced [_ _] {}))

(rf/reg-event-db ::clear-session-storage
                 [session-storage-interceptor (path :ls)]
                 (fn-traced [_ _] {}))