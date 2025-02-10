(ns leihs.borrow.features.pools.show
  (:require
   ["/borrow-ui" :as UI]
   ["autolinker" :as autolinker]
   ["date-fns" :as df]
   [clojure.string :as string]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.lib.re-frame :refer [reg-event-db reg-event-fx reg-sub
                                      subscribe]]
   [leihs.borrow.lib.translate :refer [set-default-translate-path t] :as translate]
   [re-graph.core :as re-graph]
   [reagent.core :as r]
   [shadow.resource :as rc]))

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
        locale @(subscribe [::translate/date-locale])
        is-loading? (not (or pool errors))]
    [:> UI/Components.Design.PageLayout.ContentContainer
     [:> UI/Components.Design.PageLayout.Header
      {:title (cond (:name pool) (:name pool) :else "…")
       :sub-title (when (seq suspensions)
                    (r/as-element [:> UI/Components.Design.Warning (t :!borrow.pools.access-suspended)]))}]
     (cond
       is-loading? [ui/loading]
       errors [ui/error-view errors]
       :else
       [:<>
        [:div.row
         [:div.col-12.col-md.mb-5

          [:> UI/Components.Design.Section {:collapsible false
                                            :title (t :contact.title)
                                            :class "preserve-linebreaks text-break fw-bold decorate-links"}
           [:<>
            (when-let [email (:email pool)]
              [:a.decorate-links {:href (str "mailto:" email)}
               email])
            [:div
             (:contact pool)]]]]

         [:div.col-12.col-md.mb-5
          [:> UI/Components.Design.Section {:collapsible false
                                            :title (t :reservation-constraint.title)
                                            :class "fw-bold"}
           (cond
             (-> pool :has-reservable-items not)
             [:div.fw-bold (t :!borrow.pools.no-reservable-models)]
             (-> pool :maximum-reservation-duration)
             [:div.fw-bold (t :!borrow.pools.maximum-reservation-duration
                              {:days (-> pool :maximum-reservation-duration)})])]]]

        [:div.row
         [:div.col-12.col-md.mb-5
          [:> UI/Components.Design.Section {:id "opening-times"
                                            :collapsible false
                                            :title (t :opening-times.title)
                                            :class "fw-bold"}
           (doall (for [wday (:workdays pool)]
                    ^{:key (:day wday)}
                    [:div.row
                     [:div.col
                      (t (keyword (str "!borrow.terms.weekdays." (-> wday :day string/lower-case))))]
                     [:div.col (if (:open wday) (:info wday) (t :closed))]]))]]

         [:div.col-12.col-md.mb-5
          [:> UI/Components.Design.Section {:id "holidays"
                                            :collapsible false
                                            :title (t :holidays.title)
                                            :class "fw-bold"}

           [:> UI/Components.Design.TruncateText {:max-height "150px"
                                                  :translations {:more (t :show-remaining-holidays.more)
                                                                 :hide (t :show-remaining-holidays.hide)}}
            (doall (for [[index holiday] (map-indexed (fn [index item] [index item])
                                                      (sort-by :start-date (:holidays pool)))]
                     ^{:key index}
                     [:div.row {:key index}
                      [:div.col (:name holiday)]
                      (let [start-date (-> holiday :start-date df/parseISO (df/format "P" #js {:locale locale}))
                            end-date (-> holiday :end-date df/parseISO (df/format "P" #js {:locale locale}))]
                        [:div.col
                         (-> holiday :start-date df/parseISO (df/format "P" #js {:locale locale}))
                         (when (not= start-date end-date)
                           [:<>
                            "–"
                            (-> holiday :end-date df/parseISO (df/format "P" #js {:locale locale}))])])]))]]]]

        [:> UI/Components.Design.Stack {:space 5}
         (when-let [description (some-> pool :description autolinker/link)]
           [:> UI/Components.Design.Section {:collapsible false :title (t :description)}
            [:div {:class "preserve-linebreaks text-break fw-bold decorate-links"
                   :dangerouslySetInnerHTML {:__html description}}]])]])]))
