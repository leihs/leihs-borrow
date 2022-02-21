(ns leihs.borrow.features.current-user.profile-switch
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.core.core :refer [dissoc-in]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx reg-sub]]
   [leihs.borrow.features.current-user.core :as current-user]))

(reg-event-fx
 ::change-profile
 (fn-traced [{:keys [db]} [_ id]]
   (if (= id (current-user/get-current-user-id db))

     ; change to user
     {:dispatch [::current-user/clear-current-delegation]
      :dispatch-later {:ms 50 :dispatch [:routing/refresh-page]}}

     ; change to delegation
     {:db (-> db
              (assoc-in [::data :changing-to-id] id))
      :dispatch [::load-delegation id]})))

(reg-event-fx
 ::load-delegation
 (fn-traced [_ [_ id]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/current_user/getDelegation.gql")
               {:id id}
               [::on-load-delegation]]}))

(reg-event-fx
 ::on-load-delegation
 (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
   (let [delegation (:delegation data)]
     (if errors
       {:db (-> db
                (dissoc-in [::data :changing-to-id])
                (assoc-in [::errors] errors))}
       {:db (-> db
                (dissoc-in [::data :changing-to-id]))
        :dispatch [::current-user/set-current-delegation delegation]
        :dispatch-later {:ms 50 :dispatch [:routing/refresh-page]}}))))

(reg-sub
 ::changing-to-id
 (fn [db] (get-in db [::data :changing-to-id])))

(reg-sub
 ::errors
 (fn [db] (get-in db [::errors])))
