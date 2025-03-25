(ns leihs.borrow.features.customer-orders.index
  (:require
   ["/borrow-ui" :as UI]
   ["date-fns" :as date-fns]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.customer-orders.current-lendings-status :as current-lendings-status]
   [leihs.borrow.features.customer-orders.order-filter :as order-filter]
   [leihs.borrow.features.customer-orders.reservation-card :refer [reservation-card]]
   [leihs.borrow.features.customer-orders.status-summary :refer [status-summary]]
   [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db reg-event-fx
                                      reg-sub subscribe]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :as translate :refer [set-default-translate-path t]]
   [re-graph.core :as re-graph]
   [reagent.core :as r]
   [shadow.resource :as rc]
   [leihs.borrow.lib.helpers :as h]))

(set-default-translate-path :borrow.rentals)

(defn prepare-query-vars [filters]
  (let [from (:from filters)
        until (:until filters)
        term (:term filters)
        pool-id (:pool-id filters)]
    (cond-> {}
      (not-empty term)
      (assoc :searchTerm term)
      from
      (assoc :from from)
      until
      (assoc :until until)
      pool-id
      (assoc :poolIds [pool-id]))))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/rentals-index
 (fn-traced [{:keys [db]} [_ {:keys [query-params]}]]
   {:dispatch-n
    (list [::set-loading]
          [::re-graph/query
           (rc/inline "leihs/borrow/features/customer_orders/customerOrdersIndex.gql")
           (merge {:userId (current-user/get-current-profile-id db)}
                  (prepare-query-vars query-params))
           [::on-fetched-data]])
    :db (-> db (assoc ::errors nil))}))

(reg-event-fx
 ::on-fetched-data
 (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
   {:db (-> db
            (cond-> errors (assoc ::errors errors))
            (assoc ::data data)
            (assoc-in [::data :loading?] false))
    :dispatch [::current-lendings-status/set-current-lendings (-> data :current-lendings-status)]}))

(reg-event-db
 ::set-loading
 (fn-traced [db _] (assoc-in db [::data :loading?] true)))

(reg-sub ::query-params
         (fn [db _]
           (let [known-filter-keys [:term :pool-id :from :until :seq :tab]]
             (->> db
                  :routing/routing :bidi-match :query-params
                  ((fn [h] (update-vals (select-keys h known-filter-keys) #(or % ""))))))))

(reg-sub ::data (fn [db _] (::data db)))
(reg-sub ::errors (fn [db _] (::errors db)))
(reg-sub ::loading?
         :<- [::data]
         (fn [data _] (let [loading? (:loading? data)]
                        (if (nil? loading?) true loading?))))

(reg-sub
 ::open-rentals
 :<- [::data]
 (fn [data _]
   (->> (get-in data [:open-rentals :edges])
        (map :node)
        not-empty)))

(reg-sub
 ::closed-rentals
 :<- [::data]
 (fn [data _]
   (->> (get-in data [:closed-rentals :edges])
        (map :node)
        not-empty)))

(reg-sub
 ::current-lendings
 :<- [::data]
 (fn [data _]
   (->> (get-in data [:current-lendings]))))

;; UI

(defn rental-line [rental date-locale]
  (let
   [title (or (:title rental) (:purpose rental))
    href (routing/path-for ::routes/rentals-show :rental-id (:id rental))]

    [:<>
     [:> UI/Components.Design.ListCard {:href href}
      [:div.d-md-flex
       [:div.pe-4 {:style {:flex "1 1 53%"}}
        [:> UI/Components.Design.ListCard.Title
         [:a.stretched-link {:href href}
          title]]

        [:> UI/Components.Design.ListCard.Body
         (h/format-date-range
          (js/Date. (:from-date rental))
          (js/Date. (:until-date rental))
          date-locale)
         ", "
         (t :x-items {:itemCount (:total-quantity rental)})]]

       [:div {:style {:flex "1 1 47%"}}
        [:> UI/Components.Design.ListCard.Foot {:class "p-md-0 pe-md-3"}
         [status-summary rental true]]]]]]))

(defn rentals-list [rentals date-locale]
  [:> UI/Components.Design.ListCard.Stack
   (doall
    (for [rental rentals]
      [:<> {:key (:id rental)}
       [rental-line rental date-locale]]))])

(defn reservations-list [reservations now date-locale]
  [:> UI/Components.Design.ListCard.Stack
   (doall
    (for [[reservation href] reservations]
      [:<> {:key (:id reservation)}
       [reservation-card reservation now href date-locale]]))])

(defn no-matches [filters]
  [:div.text-center.mt-5
   (if (empty? (dissoc filters :seq :tab)) (t :no-orders-yet) (t :no-orders-found))])

(defn switch-tab [filters tab]
  (dispatch [:routing/navigate
             [::routes/rentals-index {:query-params (assoc filters :tab tab)}]]))

(defn view []
  (let [errors @(subscribe [::errors])
        loading? @(subscribe [::loading?])
        open-rentals @(subscribe [::open-rentals])
        closed-rentals @(subscribe [::closed-rentals])
        filters @(subscribe [::query-params])
        date-locale @(subscribe [::translate/date-locale])

        now (js/Date.)
        rental-path-by-reservation-id (into {} (mapcat
                                                (fn [r] (map
                                                         #(vector
                                                           (:id %)
                                                           (routing/path-for ::routes/rentals-show :rental-id (:id r)))
                                                         (:reservations r)))
                                                open-rentals))
        current-lendings (->> @(subscribe [::current-lendings])
                              (sort-by (fn [r] [(date-fns/differenceInCalendarDays
                                                 (if (= "SIGNED" (:status r))
                                                   (js/Date. (:end-date r))
                                                   (js/Date. (:start-date r)))
                                                 now)
                                                (js/Date. (:start-date r))
                                                (js/Date. (:end-date r))
                                                (-> r :inventory-pool :name)
                                                (-> r :model :name)
                                                (-> r :id)]))
                              (map #(vector % (-> % :id rental-path-by-reservation-id))))
        tab (or (:tab filters) "current-lendings")]
    [:> UI/Components.Design.PageLayout.ContentContainer
     [:> UI/Components.Design.PageLayout.Header {:title (t :title)}
      [:div.pt-2
       [order-filter/filter-comp
        filters
        #(dispatch [:routing/navigate
                    [::routes/rentals-index {:query-params %}]])]]]

     (cond
       loading? [ui/loading]

       errors [ui/error-view errors]

       :else
       [:div.responsive-tab-combo
        [:div
         [:select.form-select.tab-select {:value tab
                                          :on-change #(switch-tab filters (-> % .-target .-value))}
          [:option {:value "current-lendings"} (str (t :section-title-current-lendings)) " (" (count current-lendings) ")"]
          [:option {:value "open-orders"} (str (t :section-title-open-rentals)) " (" (count open-rentals) ")"]
          [:option {:value "closed-orders"} (str (t :section-title-closed-rentals)) " (" (count closed-rentals) ")"]]]

        [:> UI/Components.ReactBootstrap.Tabs
         {:active-key tab
          :on-select #(switch-tab filters %)}
         [:> UI/Components.ReactBootstrap.Tab
          {:event-key "current-lendings"
           :title (r/as-element
                   [:span
                    (t :section-title-current-lendings) " "
                    [:> UI/Components.Design.CircleBadge {:inline true :variant :secondary} (count current-lendings)]])}
          (if (empty? current-lendings)
            [no-matches filters]
            [reservations-list current-lendings now date-locale])]
         [:> UI/Components.ReactBootstrap.Tab
          {:event-key "open-orders"
           :title (r/as-element
                   [:span
                    (t :section-title-open-rentals) " "
                    [:> UI/Components.Design.CircleBadge {:inline true :variant :secondary} (count open-rentals)]])}
          (if (empty? open-rentals)
            [no-matches filters]
            [rentals-list open-rentals date-locale])]
         [:> UI/Components.ReactBootstrap.Tab
          {:event-key "closed-orders"
           :title (r/as-element
                   [:span
                    (t :section-title-closed-rentals) " "
                    [:> UI/Components.Design.CircleBadge {:inline true :variant :secondary} (count closed-rentals)]])}
          (if (empty? closed-rentals)
            [no-matches filters]
            [rentals-list closed-rentals date-locale])]]])]))

(defn current-lendings-status-badge []
  (let [now (js/Date.)
        current-lendings @(subscribe [::current-lendings-status/current-lendings])
        current-lendings-count (some-> current-lendings count)
        min-days-to-action
        (apply min (map (fn [{:keys [status start-date actual-end-date]}]
                          (js/console.log start-date actual-end-date)
                          (->
                           (cond (= "SIGNED" status) (date-fns/parseISO actual-end-date)
                                 (= "APPROVED" status) (date-fns/parseISO start-date))
                           (date-fns/differenceInCalendarDays now)))
                        current-lendings))]
    [:> UI/Components.Design.CircleBadge
     {:inline true
      :variant (cond
                 (nil? min-days-to-action) "secondary"
                 (< min-days-to-action 0) "danger"
                 (<= min-days-to-action 1) "warning"
                 (<= min-days-to-action 5) "primary")}
     (or current-lendings-count ui/non-breaking-space)]))
