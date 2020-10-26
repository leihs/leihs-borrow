(ns leihs.borrow.features.pools.show
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
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.features.pools.core :refer [badge]]
            [leihs.borrow.client.routes :as routes]
            ["/leihs-ui-client-side-external-react" :as UI]))

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/inventory-pools-show
  (fn-traced [_ [_ args]]
    (let [pool-id (get-in args [:route-params :inventory-pool-id])]
      {:dispatch [::re-graph/query
                  (rc/inline "leihs/borrow/features/pools/show.gql")
                  {:id pool-id}
                  [::on-fetched-data pool-id]]})))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ pool-id {:keys [data errors]}]]
    (-> db
        (update-in [:ls ::data pool-id ] (fnil identity {}))
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

(defn view []
  (let [routing @(subscribe [:routing/routing])
        pool-id (get-in routing [:bidi-match :route-params :inventory-pool-id])
        pool @(subscribe [::pool pool-id])
        errors @(subscribe [::errors pool-id])
        is-loading? (not (or pool errors))]
    [:> UI/Components.AppLayout.Page
     {:title (cond (:name pool) (:name pool) :else "…")}
     (cond
       is-loading? [:div [:div [ui/spinner-clock]] [:pre "loading pool" [:samp (:id pool)] "…"]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:div (badge pool)]

        (if-let [email (:email pool)]
          [:a {:href (str "mailto:" email)}
           email])

        [:p.py-4.border-b-2.border-gray-300.text-base.preserve-linebreaks
         (if-let [description (some-> pool :description autolinker/link)]
           {:dangerouslySetInnerHTML {:__html description}}
           [:i "No description provided for this pool."])]

        #_[:p.debug (pr-str pool)]])]))
