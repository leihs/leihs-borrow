(ns leihs.borrow.features.shopping-cart.timeout
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require [re-frame.core :as rf]
            [re-graph.core :as re-graph]
            [shadow.resource :as rc]
            [leihs.borrow.features.shopping-cart.core :as cart]
            [leihs.borrow.lib.routing :as routing]))

(rf/reg-event-fx
  ::routing/on-change-view
  (fn [_ _]
    {:dispatch [::refresh]}))

(rf/reg-event-fx
  ::refresh
  (fn [_ _]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/shopping_cart/refreshTimeout.gql")
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
                    (-> data :refreshTimeout :unsubmittedOrder :validUntil))))))
