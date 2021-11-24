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
   #_[leihs.borrow.lib.helpers :refer [log]]
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
        user-id (:user-id filters)
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
      (assoc :poolIds [pool-id])
      user-id
      (assoc :userId user-id))))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/rentals-index
 (fn-traced [{:keys [db]} [_ {:keys [query-params]}]]
   {:dispatch-n
    (list [::filter-modal/save-filter-options query-params]
          [::current-user/set-chosen-user-id (:user-id query-params)]
          [::re-graph/query
           (rc/inline "leihs/borrow/features/customer_orders/customerOrdersIndex.gql")
           (prepare-query-vars query-params)
           [::on-fetched-data]])}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc ::data data)
       (assoc-in [::data :loading?] false))))

(reg-event-db
 ::toggle-loading
 (fn-traced [db _] (update-in db [::data :loading?] not)))

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

(reg-sub ::user-id
         :<- [::current-user/chosen-user-id]
         (fn [user-id _] user-id))

;; UI

(defn rental-progress-bars [rental small]
  (let [fulfillment-states (:fulfillment-states rental)
        total-quantity (:total-quantity rental)]

    [:> UI/Components.Design.Stack {:space 2}
     (doall
      (for [rental-state fulfillment-states]
        (let [title (t (str :fulfillment-state-label "/" rental-state))]
          ^{:key rental-state}
          [:> UI/Components.Design.ProgressInfo
           (merge
            {:title title :small small}

            (cond
              (= "IN_APPROVAL" rental-state)
                 ; NOTE: no partial approval, `done` will always be 0 when state is "IN_APPROVAL"
              (let [total total-quantity
                    done 0]
                {:totalCount total :doneCount done :info (t :fulfillment-state.summary-line.IN_APPROVAL {:totalCount total :doneCount done})})

              (= "TO_PICKUP" rental-state)
              (let [ft (:pickup-fulfillment rental)
                    total (:to-fulfill-quantity ft)
                    done (:fulfilled-quantity ft)]
                {:totalCount total :doneCount done :info (t :fulfillment-state.summary-line.TO_PICKUP {:totalCount total :doneCount done})})

              (= "TO_RETURN" rental-state)
              (let [ft (:return-fulfillment rental)
                    total (:to-fulfill-quantity ft)
                    done (:fulfilled-quantity ft)]
                {:totalCount total :doneCount done :info (t :fulfillment-state.summary-line.TO_RETURN {:totalCount total :doneCount done})})))])))]))

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
       (rental-progress-bars rental true)]]]))

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
         #(do (dispatch [::toggle-loading])
              (dispatch [:routing/navigate
                         [::routes/rentals-index {:query-params %}]]))])]
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
