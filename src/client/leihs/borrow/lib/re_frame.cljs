(ns leihs.borrow.lib.re-frame
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    [camel-snake-kebab.core :as csk]
    [clojure.walk :refer [postwalk]]
    [leihs.borrow.lib.localstorage :refer [localstorage-interceptor]]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [re-frame.std-interceptors :refer [path]]
    [shadow.resource :as rc]))

(defn- kebab-case-keys [m]
  (postwalk #(cond-> %
               (and (keyword? %)
                    (not (qualified-keyword? %)))
               csk/->kebab-case)
            m))

(def kebab-case-data-and-errors-interceptor
  (rf/->interceptor
    :id :kebab-case-data-and-errors
    :before
    (fn [ctx]
      (let [event (rf/get-coeffect (spy ctx) :event)]
        (rf/assoc-coeffect ctx
                           :event
                           (map #(-> %
                                     (cond-> (-> % :errors map?)
                                       (update :errors kebab-case-keys))
                                     (cond-> (-> % :data map?)
                                       (update :data kebab-case-keys)))
                                (spy event)))))))

(def base-interceptors [localstorage-interceptor
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

(def reg-sub rf/reg-sub)
(def reg-fx rf/reg-fx)
(def subscribe rf/subscribe)
(def dispatch rf/dispatch)
