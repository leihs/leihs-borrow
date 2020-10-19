(ns leihs.borrow.lib.requests
  (:require [re-graph.core :as re-graph]
            [re-frame.core :as rf]
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
  (rf/->interceptor :after
                    (fn [ctx]
                      (let [[ev-id :as ev] (-> ctx :coeffects :event)]
                        (if (= ev-id ::re-graph/mutate)
                          (update-in ctx
                                     [:effects :db ::running-mutations-ids]
                                     conj
                                     (-> ctx
                                         :effects
                                         :re-graph.internals/send-http
                                         spy
                                         (nth 1)))
                          ctx)))))
