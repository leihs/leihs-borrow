(ns leihs.borrow.lib.requests
  (:require [re-graph.core :as re-graph]
            [re-frame.core :as rf]
            [leihs.core.core :refer [flip]]
            [leihs.borrow.lib.helpers :refer [spy log]]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch
                                               dispatch-sync]]))

(def re-graph-requests-db-path
  [:re-graph :re-graph.internals/default :http :requests])

(reg-event-fx
  ::abort-running-queries
  (fn [{:keys [db]} _]
    (let [req-ids (as-> db <>
                    (get-in <> re-graph-requests-db-path)
                    (keys <>)
                    (remove (-> db ::running-mutations-ids set) <>))
          events (->> req-ids
                      (map (partial vector ::re-graph/abort)))]
      {:dispatch-n events})))

(reg-sub ::running-mutations-ids
         (fn [db _] (::running-mutations-ids db)))

(reg-sub ::running
         (fn [db _] (get-in db re-graph-requests-db-path)))

(rf/reg-global-interceptor
  (rf/->interceptor
    :id :track-mutations-ids
    :after (fn [ctx]
             (let [[ev-id :as ev] (-> ctx :coeffects :event)
                   path [:effects :db ::running-mutations-ids]]
               (case ev-id
                 ::re-graph/mutate
                 (update-in ctx path conj (-> ctx
                                              :effects
                                              :re-graph.internals/send-http
                                              (nth 1)))
                 :re-graph.internals/http-complete
                 (update-in ctx path (flip remove) #{(nth ev 2)})
                 ctx)))))
