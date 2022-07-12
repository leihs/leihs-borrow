(ns leihs.borrow.lib.re-frame
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [camel-snake-kebab.core :as csk]
   [leihs.borrow.lib.browser-storage :refer [local-storage-interceptor session-storage-interceptor]]
   [leihs.borrow.lib.helpers :as help]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [re-frame.std-interceptors :refer [path]]
   [shadow.resource :as rc]))

(def kebab-case-data-and-errors-interceptor
  (rf/->interceptor
   :id :kebab-case-data-and-errors
   :before
   (fn [ctx]
     (let [event (rf/get-coeffect ctx :event)]
       (rf/assoc-coeffect ctx
                          :event
                          (map #(-> %
                                    (cond-> (-> % :errors map?)
                                      (update :errors help/kebab-case-keys))
                                    (cond-> (-> % :data map?)
                                      (update :data help/kebab-case-keys)))
                               event))))))

(def base-interceptors [local-storage-interceptor
                        session-storage-interceptor
                        kebab-case-data-and-errors-interceptor])

(defn reg-event-db
  ([event-id event-handler]
   (rf/reg-event-db event-id
                    base-interceptors
                    event-handler))
  ([event-id interceptors event-handler]
   (rf/reg-event-db event-id
                    (concat base-interceptors interceptors)
                    event-handler)))

(defn reg-event-fx
  ([event-id event-handler]
   (rf/reg-event-fx event-id
                    base-interceptors
                    event-handler))
  ([event-id interceptors event-handler]
   (rf/reg-event-fx event-id
                    (concat base-interceptors interceptors)
                    event-handler)))

(defn reg-event-ctx
  ([event-id event-handler]
   (rf/reg-event-ctx event-id
                     base-interceptors
                     event-handler))
  ([event-id interceptors event-handler]
   (rf/reg-event-ctx event-id
                     (concat base-interceptors interceptors)
                     event-handler)))

(def reg-sub rf/reg-sub)
(def reg-fx rf/reg-fx)
(def subscribe rf/subscribe)
(def dispatch rf/dispatch)
(def dispatch-sync rf/dispatch-sync)
