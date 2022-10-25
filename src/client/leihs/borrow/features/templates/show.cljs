(ns leihs.borrow.features.templates.show
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :as translate :refer [t set-default-translate-path]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.templates.apply-template :as apply-template]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.templates.show)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/templates-show
 (fn-traced [{:keys [db]} [_ args]]
   (let [template-id (get-in args [:route-params :template-id])]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/features/templates/show.gql")
                 {:id template-id
                  :userId (current-user/get-current-profile-id db)}
                 [::on-fetched-data template-id]]})))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ template-id {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc-in , [::errors template-id] errors))
       (assoc-in [:ls ::data template-id] (:template data)))))


(reg-sub ::data
         (fn [db [_ id]] (get-in db [:ls ::data id])))

(reg-sub ::errors
         (fn [db [_ id]] (get-in db [::errors id])))

(reg-sub ::current-profile-id
         :<- [::current-user/current-profile-id]
         (fn [current-profile-id _] current-profile-id))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        template-id (get-in routing [:bidi-match :route-params :template-id])
        template @(subscribe [::data template-id])
        errors @(subscribe [::errors template-id])
        is-loading? (not (or template errors))
        error403? (and (not is-loading?) (some #(= 403 (-> % :extensions :code)) errors))
        current-profile-id @(subscribe [::current-profile-id])
        date-fns-locale @(subscribe [::translate/i18n-locale])]
    [:<>
     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)
       :sub-title (:name template)}
      (when error403?
        [:> UI/Components.Design.InfoMessage {:class "mt-2"} (t :template-not-available)])]
     (cond
       is-loading? [ui/loading]
       errors
       (when (not error403?) ; (403 already displayed in the header, details not relevant for user)
         [ui/error-view errors])
       :else
       (let [models
             (->> (:lines template)
                  (group-by #(-> % :model :id)) ; Note: one model can appear multiple times in a template (missing unique constraint?)
                  (map
                   (fn [tuple]
                     (let [lines (-> tuple second)
                           model (-> lines first :model)
                           quantity (->> lines (map #(:quantity %)) (reduce +))]
                       (assoc model :quantity quantity))))
                  (sort-by
                   (fn [model]
                     (:name model))))
             model-list-items
             (->> models
                  (map
                   (fn [model]
                     {:id (:id model)
                      :imgSrc (or (get-in model [:cover-image :image-url])
                                  (get-in model [:images 0 :image-url]))
                      :isDimmed (-> model :is-reservable not)
                      :caption (t :item-title {:itemCount (if (-> model :is-reservable)
                                                            (:quantity model)
                                                            0)
                                               :itemName (:name model)})
                      :subCaption (:manufacturer model)
                      :href (when (-> model :is-reservable)
                              (routing/path-for ::routes/models-show
                                                :model-id (:id model)))
                      :isFavorited (:is-favorited model)})))
             some-not-reservable? (->> template
                                       :lines
                                       (map #(-> % :model :is-reservable))
                                       (not-every? identity))
             none-reservable? (->> template
                                   :lines
                                   (map #(-> % :model :is-reservable))
                                   (not-any? identity))]
         [:<>
          [apply-template/dialog template models current-profile-id date-fns-locale]
          [apply-template/success-notification]

          [:> UI/Components.Design.Stack {:space 4}

           (cond none-reservable?
                 [:> UI/Components.Design.Warning
                  (t :no-items-available)]
                 some-not-reservable?
                 [:> UI/Components.Design.InfoMessage
                  (t :some-items-not-available)])

           (when-not none-reservable?
             [:> UI/Components.Design.ActionButtonGroup
              [:button.btn.btn-primary {:disabled none-reservable? :onClick #(dispatch [::apply-template/open-dialog])}
               (t :apply-button-label)]])

           [:> UI/Components.Design.Section {:title (t :items) :collapsible true}
            [:> UI/Components.ModelList {:list model-list-items}]]]]))]))
