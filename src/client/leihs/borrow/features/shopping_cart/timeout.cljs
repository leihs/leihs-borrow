(ns leihs.borrow.features.shopping-cart.timeout
  (:require [re-frame.core :as rf]
            [re-graph.core :as re-graph]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [shadow.resource :as rc]
            [leihs.borrow.features.shopping-cart.core :as cart]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch]]
            [leihs.borrow.lib.routing :as routing]))

(reg-event-fx
  ::routing/on-change-view
  (fn-traced [_ _]
    {:dispatch [::refresh]}))

(reg-event-fx
  ::refresh
  (fn-traced [_ _]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/shopping_cart/refreshTimeout.gql")
                nil
                [::on-refresh]]}))

(reg-event-db
  ::on-refresh
  (fn-traced [db [_ {:keys [data errors]}]]
    (if errors
      (js/console.log "timeout refresh errors: " (clj->js errors))
      (assoc-in db
                [::cart/data :valid-until]
                (-> data :refresh-timeout :unsubmitted-order :valid-until)))))
