(ns leihs.borrow.features.customer-orders.index
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   #_[re-frame.core :as rf]
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
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.customer-orders.filter-modal :refer [filter-comp]
    :as filter-modal]
   [leihs.borrow.features.current-user.core :as current-user]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.rentals)

(defn prepare-query-vars [filters]
  (let [from (:from filters)
        until (:until filters)
        term (:term filters)
        state (:state filters)
        pool-id (:pool-id filters)]
    (cond-> {}
      term
      (assoc :searchTerm term)
      from
      (assoc :from from)
      until
      (assoc :until until)
      state
      (assoc :refinedRentalState state)
      pool-id
      (assoc :poolIds [pool-id]))))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/rentals-index
 (fn-traced [{:keys [db]} [_ {:keys [query-params]}]]
   {:dispatch-n
    (list [::filter-modal/save-filter-options query-params]
          [::set-loading]
          [::re-graph/query
           (rc/inline "leihs/borrow/features/customer_orders/customerOrdersIndex.gql")
           (merge {:userId (current-user/get-current-profile-id db)}
                  (prepare-query-vars query-params))
           [::on-fetched-data]])}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc ::data data)
       (assoc-in [::data :loading?] false))))

(reg-event-db
 ::set-loading
 (fn-traced [db _] (assoc-in db [::data :loading?] true)))

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

;; UI

(defn status-summary [rental small]
  (let [initial-total-count (-> rental :total-quantity)
        canceled? (-> rental :fulfillment-states (= ["CANCELED"]))

        approved-count (-> rental :approve-fulfillment :fulfilled-quantity)
        rejected-count (-> rental :rejected-quantity)
        expired-unapproved-count (-> rental :expired-unapproved-quantity)

        picked-up-count (-> rental :pickup-fulfillment :fulfilled-quantity)
        expired-count (-> rental :expired-quantity)

        returned-count (-> rental :return-fulfillment :fulfilled-quantity)
        overdue-count (-> rental :overdue-quantity)]

    (if canceled?

      [:> UI/Components.Design.ProgressInfo
       {:small small
        :title (t (str :fulfillment-state-label.CANCELED))}]

      [:> UI/Components.Design.Stack {:space 2}

       ; Process 1: Approval
       (let [total-count initial-total-count
             done-count (+ approved-count rejected-count expired-unapproved-count)
             all-approved? (= approved-count total-count)
             all-rejected? (= rejected-count total-count)
             all-expired-unapproved? (= expired-unapproved-count total-count)
             title (cond
                     all-rejected? (t :fulfillment-state-label.REJECTED)
                     all-expired-unapproved? (t :fulfillment-state-label.EXPIRED-UNAPPROVED)
                     :else (t :fulfillment-state-label.IN_APPROVAL))
             info (when (not (or all-rejected? all-expired-unapproved?))
                    (str
                     (t :fulfillment-state.summary-line.IN_APPROVAL {:totalCount total-count :doneCount approved-count})
                     (when (> rejected-count 0)
                       (t :fulfillment-state.partial-status.REJECTED {:count rejected-count}))
                     (when (> expired-unapproved-count 0)
                       (t :fulfillment-state.partial-status.EXPIRED {:count expired-unapproved-count}))))]
         (when (not all-approved?)
           [:> UI/Components.Design.ProgressInfo
            (merge {:small small :title title :info info}
                   (when (not= done-count total-count) {:totalCount total-count :doneCount done-count}))]))

       ; Process 2: Pickup
       (let [total-count (- initial-total-count rejected-count expired-unapproved-count)
             done-count (+ picked-up-count expired-count)
             started? (> approved-count 0)
             all-picked? (= picked-up-count total-count)
             all-expired? (= expired-count total-count)
             title (cond
                     all-expired? (t :fulfillment-state-label.EXPIRED)
                     :else (t :fulfillment-state-label.TO_PICKUP))
             info (when (not all-expired?)
                    (str
                     (t :fulfillment-state.summary-line.TO_PICKUP {:totalCount total-count :doneCount picked-up-count})
                     (when (> expired-count 0)
                       (t :fulfillment-state.partial-status.EXPIRED {:count expired-count}))))]
         (when (-> started? (and (not all-picked?)))
           [:> UI/Components.Design.ProgressInfo
            (merge {:small small :title title :info info}
                   (when (not= done-count total-count) {:totalCount total-count :doneCount done-count}))]))

        ; Process 3: Return
       (let [total-count (- initial-total-count rejected-count expired-unapproved-count expired-count)
             done-count returned-count
             started? (> picked-up-count 0)
             all-returned? (= returned-count total-count)
             title (cond
                     (> overdue-count 0) (r/as-element [:div {:class "invalid-feedback-icon text-danger"} (t :fulfillment-state-label.OVERDUE)])
                     all-returned? (t :fulfillment-state-label.RETURNED)
                     :else (t :fulfillment-state-label.TO_RETURN))
             info (when (not all-returned?)
                    (str
                     (t :fulfillment-state.summary-line.TO_RETURN {:totalCount total-count :doneCount returned-count})
                     (when (> overdue-count 0)
                       (t :fulfillment-state.partial-status.OVERDUE {:count overdue-count}))))]
         (when started?
           [:> UI/Components.Design.ProgressInfo
            (merge {:small small :title title :info info}
                   (when (not= done-count total-count) {:totalCount total-count :doneCount done-count}))]))])))

(defn order-line [rental]
  (let
   [title (or (:title rental) (:purpose rental))
    href (routing/path-for ::routes/rentals-show :rental-id (:id rental))
    summary-text (rentals/rental-summary-text rental)]

    [:<>
     [:> UI/Components.Design.ListCard {:href href}
      [:> UI/Components.Design.ListCard.Title
       [:a.stretched-link {:href href}
        title]]

      [:> UI/Components.Design.ListCard.Body
       summary-text]

      [:> UI/Components.Design.ListCard.Foot
       [status-summary rental true]]]]))

(defn orders-list [orders]
  [:> UI/Components.Design.ListCard.Stack
   (doall
    (for [order orders]
      [:<> {:key (:id order)}
       [order-line order]]))])

(defn view []
  (let [errors @(subscribe [::errors])
        loading? @(subscribe [::loading?])
        open-rentals @(subscribe [::open-rentals])
        closed-rentals @(subscribe [::closed-rentals])]
    [:<>
     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)}
      (when-not loading?
        [filter-comp
         #(dispatch [:routing/navigate
                     [::routes/rentals-index {:query-params %}]])])]
     (cond
       loading? [ui/loading]

       (and (empty? open-rentals) (empty? closed-rentals))
       [:p.p-6.w-full.text-center (t :!borrow.pagination/nothing-found)]

       errors [ui/error-view errors]

       :else
       [:<>
        [:> UI/Components.Design.Stack {:space 5}
         (when-not (empty? open-rentals)
           [:> UI/Components.Design.Section
            {:title (t :section-title-open-rentals) :collapsible true}
            [orders-list open-rentals]])
         (when-not (empty? closed-rentals)
           [:> UI/Components.Design.Section
            {:title (t :section-title-closed-rentals) :collapsible true}
            [orders-list closed-rentals]])]])]))
