(ns leihs.borrow.features.templates.show
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.helpers :refer [spy]]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.models.core :as models]
   [leihs.borrow.features.models.filter-modal :as filter-modal]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.templates)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/templates-show
 (fn-traced [{:keys [db]} [_ args]]
   (let [template-id (get-in args [:route-params :template-id])]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/features/templates/show.gql")
                 {:id template-id}
                 [::on-fetched-data template-id]]})))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ template-id {:keys [data errors]}]]
   (-> db
       (update-in , [::data template-id] (fnil identity {}))
       (cond-> errors (assoc-in , [::errors template-id] errors))
       (assoc-in , [:ls ::data template-id] (:template data)))))

(reg-event-fx
 ::apply
 (fn-traced [_ [_ template-id start-date end-date]]
   {:dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/features/templates/apply.gql")
               {:id template-id
                :startDate start-date
                :endDate end-date}
               [::on-mutation-result]]}))

(reg-event-fx
 ::on-mutation-result
 (fn-traced [_ [_ {:keys [data errors]}]]
   (if errors
     {:dispatch [::errors/add-many errors]}
     {:alert (str "OK! " (pr-str data))})))

(reg-sub ::data
         (fn [db [_ id]] (get-in db [:ls ::data id])))

(reg-sub ::errors
         (fn [db [_ id]] (get-in db [::errors id])))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        template-id (get-in routing [:bidi-match :route-params :template-id])
        template @(subscribe [::data template-id])
        some-not-reservable? (->> template
                                  :lines
                                  (map #(-> % :model :is-reservable))
                                  (not-every? identity))
        opts @(subscribe [::filter-modal/options])
        start-date (:start-date opts)
        end-date (:end-date opts)
        errors @(subscribe [::errors template-id])
        is-loading? (not (or template errors))]
    [:<>
     [:> UI/Components.Design.PageLayout.Header
      {:title (or (:name template "…"))}
      [:a {:href (routing/path-for ::routes/templates-index) :class "text-decoration-underline"} "All Templates"]]
     (cond
       is-loading? [ui/loading]
       errors [ui/error-view errors]
       :else
       [:> UI/Components.Design.Stack {:space 4}
        (when some-not-reservable?
          [:<>
           [:div {:class "text-danger"}
            [:b (t :some-not-reservable)]]
           [:br]])
        [:div.form-group
         [models/form-line :start-date (t :!borrow.filter/from)
          {:type :date
           :required true
           :value start-date}]]
        [:div.form-group
         [models/form-line :end-date (t :!borrow.filter/until)
          {:type :date
           :required true
           :min start-date
           :value end-date}]]
        [:> UI/Components.Design.ActionButtonGroup
         [:button.btn.btn-primary
          {:on-click #(dispatch [::apply template-id start-date end-date])}
          "Apply"]]
        [:br]
        [:h2 "Template Data"]
        [:pre {:style {:white-space :pre-wrap}}
         (js/JSON.stringify (clj->js template) 0 2)]])]))
