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
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.helpers :as h]
   ["/leihs-ui-client-side-external-react" :as UI]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.features.current-user.core :as core]
   [leihs.borrow.client.routes :as routes]))

(set-default-translate-path :borrow.current-user)

(reg-event-fx
 ::routes/current-user-show
 (fn-traced [_ _]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/current_user/show.gql")
               {}
               [::on-fetched-data]]}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc ::data (or data {})))))

(reg-sub ::data
         (fn [db _] (::data db)))

(reg-sub ::errors
         (fn [db _] (::errors db)))

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])]

    (cond
      (not data) [ui/loading]
      errors [ui/error-view errors]
      :else
      (let [current-user (:current-user data)
            user (:user current-user)
            delegations
            (doall
             (for [delegation (get-in user [:delegations])]
               {:id (:id delegation)
                :name (:name delegation)
                :responsible-name (str (-> delegation :responsible :firstname) " " (-> delegation :responsible :lastname))
                :href (routing/path-for ::routes/delegations-show
                                        :delegation-id
                                        (:id delegation))}))
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
                  :noContracts (t :no-contracts)}})]

         [:> UI/Components.Design.PageLayout.MetadataWithDetails
          {:summary (t :metadata-summary {:userId (:id (:user current-user))})
           :details (clj->js (h/camel-case-keys current-user))}]]))))
