(ns leihs.borrow.features.visits.pickups
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
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
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.features.current-user.core :as current-user]))

(set-default-translate-path :borrow.visits)

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/pickups-index
  (fn-traced [{:keys [db]} [_ _]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/visits/pickups.gql")
                {}
                [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ {:keys [data errors]}]]
    (-> db
        (cond-> errors (assoc ::errors errors))
        (assoc-in [:ls ::data] (:pickups data)))))

(reg-sub ::data (fn [db _] (-> db :ls ::data)))

(reg-sub ::errors (fn [db _] (::errors db)))

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])
        is-loading? (not (or data errors))]
    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header.mb-3
         [:h1.text-3xl.font-extrabold.leading-tight (t :pickups/title)]]
        [:pre.text-xs {:style {:white-space :pre-wrap}}
         (js/JSON.stringify (clj->js data) 0 2)]])]))
