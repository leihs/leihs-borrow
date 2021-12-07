(ns leihs.borrow.features.delegations.show
  (:require ["autolinker" :as autolinker]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [re-graph.core :as re-graph]
            [shadow.resource :as rc]
            [leihs.borrow.components :as ui]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch]]
            [leihs.borrow.lib.localstorage :as ls]
            [leihs.borrow.lib.translate :refer [t]]
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.client.routes :as routes]
            ["/leihs-ui-client-side-external-react" :as UI]))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/delegations-show
 (fn-traced [_ [_ args]]
   (let [delegation-id (get-in args [:route-params :delegation-id])]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/features/delegations/show.gql")
                 {:id delegation-id}
                 [::on-fetched-data delegation-id]]})))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ delegation-id {:keys [data errors]}]]
   (-> db
       (update-in [::data delegation-id] (fnil identity {}))
       (cond->
        errors
         (assoc-in [::errors delegation-id] errors))
       (assoc-in [::data delegation-id] (:delegation data)))))

(reg-sub ::delegation
         (fn [db [_ id]]
           (get-in db [::data id])))

(reg-sub ::errors
         (fn [db [_ id]]
           (get-in db [::errors id])))

(defn fullname [user]
  (str (:firstname user) " " (:lastname user)))

(defn responsible [user]
  [:<>
   [:> UI/Components.Design.Section
    {:title (t :borrow.delegations/responsible)
     :collapsible true}
    [:> UI/Components.Design.ListCard.Stack
     [:> UI/Components.Design.ListCard
      [:> UI/Components.Design.ListCard.Title
       (fullname user)]
      (if-let [email (:email user)]
        [:> UI/Components.Design.ListCard.Body
         [:a {:href (str "mailto:" email)}
          email]])]]]])

(defn members-list [members]
  [:<>
   [:> UI/Components.Design.Section
    {:title (t :borrow.delegations/members)
     :collapsible true}
    [:> UI/Components.Design.ListCard.Stack
     (doall
      (for [member members]
        [:> UI/Components.Design.ListCard {:key (:id member)}
         [:> UI/Components.Design.ListCard.Title
          (fullname member)]]))]]])

(defn view []
  (let [routing @(subscribe [:routing/routing])
        delegation-id (get-in routing [:bidi-match :route-params :delegation-id])
        delegation @(subscribe [::delegation delegation-id])
        errors @(subscribe [::errors delegation-id])
        is-loading? (not (or delegation errors))]
    [:<>
     [:> UI/Components.Design.PageLayout.Header
      {:title (:name delegation)}]
     (cond
       is-loading? [ui/loading (t :borrow.delegations/loading)]
       errors [ui/error-view errors]
       :else
       [:> UI/Components.Design.Stack {:space 5}
        [responsible (:responsible delegation)]
        [members-list (:members delegation)]])]))
