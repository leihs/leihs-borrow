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
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]
   ["/borrow-ui" :as UI]))

(set-default-translate-path :borrow.templates.index)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/templates-index
 (fn-traced [{:keys [db]} [_ _]]
   {:db (-> db
            (assoc-in [::data :loading?] true))
    :dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/templates/index.gql")
               {:userId (current-user/get-current-profile-id db)}
               [::on-fetched-data]]}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc-in [:ls ::data] (:templates data))
       (assoc-in [::data :loading?] false))))

(reg-sub ::data (fn [db _] (-> db :ls ::data)))

(reg-sub ::errors (fn [db _] (::errors db)))

(reg-sub ::loading (fn [db _] (get-in db [::data :loading?])))

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])
        is-loading? @(subscribe [::loading])]
    [:> UI/Components.Design.PageLayout.ContentContainer
     [:> UI/Components.Design.PageLayout.Header {:title (t :title)}
      (when (and (not is-loading?) (not errors) (empty? data))
        [:div (t :no-templates-for-current-profile)])]
     (cond
       is-loading? [ui/loading]
       errors [ui/error-view errors]
       :else
       [:<>
        [:> UI/Components.Design.ListCard.Stack
         (doall
          (for [template data]
            (let [id (:id template)
                  href (routing/path-for ::routes/templates-show :template-id id)]
              [:> UI/Components.Design.ListCard {:key id :href href}
               [:> UI/Components.Design.ListCard.Title (:name template)]
               [:> UI/Components.Design.ListCard.Body (-> template :inventory-pool :name)]])))]])]))
