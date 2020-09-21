(ns leihs.borrow.features.templates.show
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.helpers :refer [spy]]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.features.models.core :as models]))

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
        (update-in , [::data template-id ] (fnil identity {}))
        (cond-> errors (assoc-in , [::errors template-id] errors))
        (assoc-in , [:ls ::data template-id] (:template data)))))

(reg-event-fx
  ::apply
  (fn-traced [_ [_ template-id start-date end-date]]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/templates/apply.gql")
                {:id template-id,
                 :startDate start-date,
                 :endDate end-date}
                [::on-mutation-result]]}))

(reg-event-fx
  ::on-mutation-result
  (fn-traced [_ [_ {:keys [data errors]}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:alert (str "OK! " (pr-str data))})))

(reg-sub ::data
         (fn [db [_ id]] (get-in db [:ls ::data id])))

(reg-sub ::errors
         (fn [db _] (::errors db)))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        template-id (get-in routing [:bidi-match :route-params :template-id])
        template @(subscribe [::data template-id])
        some-not-reservable? (->> template
                                  :lines
                                  (map #(-> % :model :is-reservable))
                                  (not-every? identity))
        start-date @(subscribe [::filters/start-date])
        end-date @(subscribe [::filters/end-date])
        errors @(subscribe [::errors template-id])
        is-loading? (not (or template errors))]
    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header.mb-3
         [:h1.text-3xl.font-extrabold.leading-tight (:name template)]
         [:p.mt-2.text-color-muted.text-sm
          [:a {:href (routing/path-for ::routes/templates-index)}
           "← all templates"]]]
        (when some-not-reservable?
          [:<>
           [:div {:class "text-danger"}
            [:b (t :some-not-reservable)]]
           [:br]])
        [:div.form-group
         [models/form-line :start-date (t :!borrow.filter/from)
          {:type :date
           :required true
           :value start-date
           :on-change #(dispatch [::filters/set-one :start-date (-> % .-target .-value)])}]]
        [:div.form-group
         [models/form-line :end-date (t :!borrow.filter/until)
          {:type :date
           :required true
           :min start-date
           :value end-date
           :on-change #(dispatch [::filters/set-one :end-date (-> % .-target .-value)])}]]
        [:div.flex-auto.w-1_2
         [:button.px-4.py-2.w-100.rounded-lg.bg-content-inverse.text-color-content-inverse.font-semibold.text-lg
          {:on-click #(dispatch [::apply template-id start-date end-date])}
          "Apply"]]
        [:br]
        [:h2.font-bold.text-xl [:mark "Template Data"]]
        [:pre.text-xs {:style {:white-space :pre-wrap}}
         (js/JSON.stringify (clj->js template) 0 2)]])]))
