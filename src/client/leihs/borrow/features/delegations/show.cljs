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
        (update-in [::data delegation-id ] (fnil identity {}))
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
   [:h3 (t :borrow.delegations/responsible)]
   [:div (fullname user)]
   (if-let [email (:email user)]
     [:a {:href (str "mailto:" email)}
      email])])

(defn members-list [members]
  [:<>
   [:h3 (t :borrow.delegations/members)]
   [:ul
    (doall
      (for [member members]
        [:li {:key (:id member)} (fullname member)]))]])

(defn view []
  (let [routing @(subscribe [:routing/routing])
        delegation-id (get-in routing [:bidi-match :route-params :delegation-id])
        delegation @(subscribe [::delegation delegation-id])
        errors @(subscribe [::errors delegation-id])
        is-loading? (not (or delegation errors))]
    [:> UI/Components.AppLayout.Page
     {:title (when delegation (:name delegation) "…")}
     (cond
       is-loading? [:div
                    [:div [ui/spinner-clock]]
                    [:pre (t :borrow.delegations/loading) [:samp (:id delegation)] "…"]]
       errors [ui/error-view errors]
       :else [:<>
              [responsible (:responsible delegation)]
              [:br]
              [members-list (:members delegation)]
              #_[:p.debug (pr-str delegation)]])]))
