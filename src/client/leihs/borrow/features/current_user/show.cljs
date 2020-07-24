(ns leihs.borrow.features.current-user.show
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
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
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.features.current-user.core :as core]
    [leihs.borrow.client.routes :as routes]))

(reg-event-fx
  ::routes/current-user-show
  (fn [_ _]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/current_user/show.gql")
                {}
                [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn [db [_ {:keys [data errors]}]]
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
    [:section.mx-3.my-4
     (cond
       (not data) [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else [:<>
              [:header.mb-3
               [:h1.text-3xl.font-extrabold.leading-tight "Current User"]
               [:pre.text-xs {:style {:white-space :pre-wrap}}
                (js/JSON.stringify (clj->js data) 0 2)]]])]))
