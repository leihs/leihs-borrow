(ns leihs.borrow.features.shopping-cart.timeout
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require [re-frame.core :as rf]
            [re-graph.core :as re-graph]
            [shadow.resource :as rc]
            [leihs.borrow.features.shopping-cart.core :as cart]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch]]
            [leihs.borrow.lib.routing :as routing]))

(reg-event-fx
  ::routing/on-change-view
  (fn [_ _]
    {:dispatch [::refresh]}))

(reg-event-fx
  ::refresh
  (fn [_ _]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/shopping_cart/refreshTimeout.gql")
                nil
                [::on-refresh]]}))

(reg-event-db
  ::on-refresh
  (fn [db [_ {:keys [data errors]}]]
    (if errors
      (js/console.log "timeout refresh errors: " errors)
      (do (js/console.log "timeout refresh success")
          (assoc-in db
                    [::cart/data :valid-until]
                    (-> data :refresh-timeout :unsubmitted-order :valid-until))))))
