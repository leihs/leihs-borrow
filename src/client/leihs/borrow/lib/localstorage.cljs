(ns leihs.borrow.lib.localstorage
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [akiroz.re-frame.storage :refer [persist-db]]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :as rf]
    [re-frame.std-interceptors :refer [path enrich]]))

(def localstorage-interceptor (persist-db :leihs-borrow :ls))

(rf/reg-event-db ::clear
                 [localstorage-interceptor (path :ls)]
                 (fn-traced [ls _] {}))
