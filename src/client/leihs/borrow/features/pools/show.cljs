(ns leihs.borrow.features.pools.show
  (:require ["autolinker" :as autolinker]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [re-graph.core :as re-graph]
            [shadow.resource :as rc]
            [leihs.borrow.components :as ui]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch]]
            [leihs.borrow.lib.localstorage :as ls]
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
            [leihs.borrow.client.routes :as routes]
            ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.pool-show)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/inventory-pools-show
 (fn-traced [{:keys [db]} [_ args]]
   (let [pool-id (get-in args [:route-params :inventory-pool-id])]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/features/pools/show.gql")
                 {:id pool-id}
                 [::on-fetched-data pool-id]]
      :db (-> db (assoc-in [::errors pool-id] nil))})))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ pool-id {:keys [data errors]}]]
   (-> db
       (update-in [:ls ::data pool-id] (fnil identity {}))
       (cond->
        errors
         (assoc-in [::errors pool-id] errors))
       (assoc-in [:ls ::data pool-id] (:inventory-pool data)))))

(reg-sub ::pool
         (fn [db [_ id]]
           (get-in db [:ls ::data id])))

(reg-sub ::errors
         (fn [db [_ id]]
           (get-in db [::errors id])))

(reg-sub ::suspensions
         :<- [::current-user/current-profile]
         (fn [current-profile _]
           (:suspensions current-profile)))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        pool-id (get-in routing [:bidi-match :route-params :inventory-pool-id])
        pool @(subscribe [::pool pool-id])
        suspensions (filter #(= (get-in % [:inventory-pool :id]) (:id pool)) @(subscribe [::suspensions]))
        errors @(subscribe [::errors pool-id])
        is-loading? (not (or pool errors))]
    [:<>
     [:> UI/Components.Design.PageLayout.Header
      {:title (cond (:name pool) (:name pool) :else "…")
       :sub-title (when (seq suspensions)
                    (r/as-element [:> UI/Components.Design.Warning (t :!borrow.pools.access-suspended)]))}]
     (cond
       is-loading? [ui/loading]
       errors [ui/error-view errors]
       :else
       [:> UI/Components.Design.Stack {:space 5}

        (cond
          (-> pool :has-reservable-items not)
          [:div  (t :!borrow.pools.no-reservable-models)]
          (-> pool :maximum-reservation-time)
          [:div  (t :!borrow.pools.maximum-reservation-time {:days (-> pool :maximum-reservation-time)})])

        (when-let [email (:email pool)]
          [:> UI/Components.Design.Section {:collapsible true :title (t :email)}
           [:a {:href (str "mailto:" email)}
            email]])

        (when-let [description (some-> pool :description autolinker/link)]
          [:> UI/Components.Design.Section {:collapsible true :title (t :description)}
           [:div {:class "preserve-linebreaks text-break" :dangerouslySetInnerHTML {:__html description}}]])])]))
