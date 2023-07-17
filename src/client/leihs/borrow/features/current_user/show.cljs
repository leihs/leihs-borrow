(ns leihs.borrow.features.current-user.show
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [re-frame.std-interceptors :refer [path]]
   [shadow.resource :as rc]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.helpers :as h]
   ["/borrow-ui" :as UI]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.features.current-user.core :as core]
   [leihs.borrow.client.routes :as routes]))

(set-default-translate-path :borrow.current-user)

(reg-event-fx
 ::routes/current-user-show
 (fn-traced [{:keys [db]} _]
   {:db (dissoc db ::errors)
    :dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/current_user/show.gql")
               {}
               [::on-fetched-data]]}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc ::data data))))

(reg-sub ::data
         (fn [db _] (::data db)))

(reg-sub ::errors
         (fn [db _] (::errors db)))

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])]
    [:> UI/Components.Design.PageLayout.ContentContainer
     (cond
       (not (or errors data)) [ui/loading]
       errors
       [ui/error-view errors]
       data
       (let [current-user (:current-user data)
             user (:user current-user)
             delegations
             (doall
              (for [delegation (get-in user [:delegations])]
                {:id (:id delegation)
                 :name (:name delegation)
                 :responsible-name (str (-> delegation :responsible :firstname) " " (-> delegation :responsible :lastname))
                 :responsible-email (-> delegation :responsible :email)}))
             contracts
             (doall
              (for [edge (get-in current-user [:user :contracts :edges])]
                (let [c (:node edge)]
                  {:id (:id c)
                   :download-url (:print-url c)
                   :display-name (t :!borrow.phrases.contract-display-name
                                    {:ID (:compact-id c)
                                     :date (js/Date. (:created-at c))
                                     :poolName (get-in c [:inventory-pool :name])})})))]

         [:<>
          [:> UI/Components.UserProfilePage
           (h/camel-case-keys
            {:user user
             :delegations delegations
             :contracts contracts
             :txt {:pageTitle (t :title)
                   :sectionUserData (t :user-data)
                   :sectionContracts (t :!borrow.terms.contracts)
                   :sectionDelegations (t :!borrow.terms.delegations)
                   :logout (t :!borrow.logout)
                   :noContracts (t :no-contracts)}})]]))]))
