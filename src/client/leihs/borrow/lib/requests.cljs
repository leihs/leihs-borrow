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

(reg-event-fx
  ::abort-all
  (fn [{:keys [db]} _]
    (let [req-ids (-> db
                      :re-graph
                      :re-graph.internals/default
                      :http
                      :requests
                      keys)
          events (->> req-ids
                      (map (partial vector ::re-graph/abort)))]
      {:dispatch-n events})))

(rf/reg-global-interceptor
  (rf/->interceptor
    :id :track-mutation-ids
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
