(ns leihs.borrow.features.delegations.show
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require ["autolinker" :as autolinker]
            [re-frame.core :as rf]
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
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.client.routes :as routes]))

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/delegations-show
  (fn [_ [_ args]]
    (let [delegation-id (get-in args [:route-params :delegation-id])]
      {:dispatch [::re-graph/query
                  (rc/inline "leihs/borrow/features/delegations/show.gql")
                  {:id delegation-id}
                  [::on-fetched-data delegation-id]]})))

(reg-event-db
  ::on-fetched-data
  (fn [db [_ delegation-id {:keys [data errors]}]]
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
   [:h3 "Responsible:"]
   [:div (fullname user)]
   (if-let [email (:email user)]
     [:a {:href (str "mailto:" email)}
      email])])

(defn members-list [members]
  [:<>
   [:h3 "Members:"]
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
    [:section.mx-3.my-4
     (cond
       is-loading? [:div
                    [:div [ui/spinner-clock]]
                    [:pre "loading delegation" [:samp (:id delegation)] "…"]]
       errors [ui/error-view errors]
       :else [:<>
              [:header.d-flex.items-stretch
               [:h1.text-3xl.font-extrabold.leading-none (:name delegation)]] 
              [:br]
              [responsible (:responsible delegation)]
              [:br]
              [members-list (:members delegation)]
              #_[:p.debug (pr-str delegation)]])]))
