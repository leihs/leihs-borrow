(ns leihs.borrow.features.shopping-cart.timeout
  (:require [re-frame.core :as rf]
            [re-graph.core :as re-graph]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [shadow.resource :as rc]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch]]
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.lib.helpers :refer [log]]))

(reg-event-fx
 ::routing/on-change-view
 (fn-traced [_ _]
   {:dispatch [::refresh]}))

(reg-event-fx
 ::refresh
 (fn-traced [{:keys [db]} [_ _]]
   (let [user-id (current-user/get-current-profile-id db)]
     {:dispatch [::re-graph/mutate
                 (str
                  (rc/inline "leihs/borrow/features/shopping_cart/refreshTimeout.gql") "\n"
                  (rc/inline "leihs/borrow/features/shopping_cart/fragment_unsubmittedOrderProps.gql"))
                 {:userId user-id}
                 [::on-refresh]]})))

(reg-event-db
 ::on-refresh
 (fn-traced [db [_ {:keys [data errors]}]]
   (if errors
     (js/console.log "timeout refresh errors: " (clj->js errors))
     (assoc-in db
               [:ls :leihs.borrow.features.shopping-cart.core/data]
               (-> data :refresh-timeout :unsubmitted-order)))))
