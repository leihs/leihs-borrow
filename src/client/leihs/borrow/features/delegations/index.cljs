(ns leihs.borrow.features.delegations.index
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [re-frame.std-interceptors :refer [path]]
    [shadow.resource :as rc]
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.client.routes :as routes]
    ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.delegations)

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/delegations-index
  (fn-traced [_ _]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/delegations/index.gql")
                {}
                [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ {:keys [data errors]}]]
    (-> db
        (cond-> errors (assoc ::errors errors))
        (assoc ::data (-> data :current-user :user :delegations)))))

(reg-sub ::data
         (fn [db _] (::data db)))

(reg-sub ::errors
         (fn [db _] (::errors db)))

(defn delegations-list [delegations]
  [:ul.list-group
   (doall
     (for [delegation delegations]
       [:a.list-group-item.d-flex.justify-content-between.align-items-center
        {:key (:id delegation),
         :href (routing/path-for ::routes/delegations-show
                                 :delegation-id
                                 (:id delegation))}
        (:name delegation)]))])

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])]
    [:> UI/Components.AppLayout.Page
     {:title (t :title)}
     (cond
       (not data) [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>
        (when-not (empty? data)
          [:div.mt-3
           [delegations-list data]])])]))
