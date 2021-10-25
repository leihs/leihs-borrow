(ns leihs.borrow.features.templates.index
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
   [leihs.borrow.features.current-user.core :as current-user]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.templates)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/templates-index
 (fn-traced [{:keys [db]} [_ _]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/templates/index.gql")
               {}
               [::on-fetched-data]]}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc-in [:ls ::data] (:templates data)))))

(reg-sub ::data (fn [db _] (-> db :ls ::data)))

(reg-sub ::errors (fn [db _] (::errors db)))

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])
        is-loading? (not (or data errors))]
    [:<>
     [:> UI/Components.Design.PageLayout.Header {:title (t :title)}]

     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:pre.text-xs {:style {:white-space :pre-wrap}}
         (js/JSON.stringify (clj->js data) 0 2)]])]))
