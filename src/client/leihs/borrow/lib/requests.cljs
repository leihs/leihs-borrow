(ns leihs.borrow.lib.requests
  (:require [re-graph.core :as re-graph]
            [leihs.borrow.lib.helpers :refer [spy]]
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
