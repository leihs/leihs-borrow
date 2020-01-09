(ns leihs.borrow.client.lib.timeout
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require [re-frame.core :as rf]
            [re-graph.core :as re-graph]
            [leihs.borrow.client.features.shopping-cart.core :as cart]
            [leihs.borrow.client.lib.routing :as routing]))

(rf/reg-event-fx
  ::routing/on-change-view
  (fn [_ _]
    {:dispatch [::refresh]}))

(rf/reg-event-fx
  ::refresh
  (fn [_ _]
    {:dispatch [::re-graph/mutate
                "mutation { refreshTimeout } "
                nil
                [::on-refresh]]}))

(rf/reg-event-db
  ::on-refresh
  (fn [db [_ {:keys [data errors]}]]
    (if errors
      (js/console.log "timeout refresh errors: " errors)
      (do (js/console.log "timeout refresh success")
          (assoc-in db
                    [::cart/current-order :data :valid-until]
                    (:refreshTimeout data))))))
