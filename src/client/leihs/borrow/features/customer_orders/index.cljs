(ns leihs.borrow.features.customer-orders.index
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   #_[re-frame.core :as rf]
   [re-graph.core :as re-graph]
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
   [leihs.borrow.lib.filters :as filters]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.current-user.core :as current-user]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.rentals)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/rentals-index
 (fn-traced [{:keys [db]} [_ _]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/customer_orders/customerOrdersIndex.gql")
               {:userId (filters/user-id db)}
               [::on-fetched-data]]}))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ {:keys [data errors]}]]
   (-> db
       (cond-> errors (assoc ::errors errors))
       (assoc ::data data))))

(reg-sub ::data (fn [db _] (::data db)))

(reg-sub ::errors (fn [db _] (::errors db)))

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
         :<- [::current-user/user-data]
         :<- [::filters/user-id]
         (fn [[co user-id]]
           (or user-id (-> co :user :id))))

;; UI

(defn filter-panel []
  (let [user-id @(subscribe [::user-id])
        target-users @(subscribe [::current-user/target-users])]
    (when (> (count target-users) 1)
      [:div.px-3.py-4.bg-light {:class "mt-3 mb-3"}
       [:div.form.form-compact
        [:label.row
         [:span.text-xs.col-3.col-form-label (t :filter.delegation)]
         [:div.col-9
          [:select {:class "form-control"
                    :default-value user-id
                    :name :user-id
                    :on-change (fn [ev]
                                 (dispatch [::filters/set-one
                                            :user-id
                                            (-> ev .-target .-value)])
                                 (dispatch [::routes/rentals-index]))}
           (doall
            (for [user target-users]
              [:option {:value (:id user) :key (:id user)}
               (:name user)]))]]]]])))

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
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])
        is-loading? (not (or data errors))
        open-rentals @(subscribe [::open-rentals])
        closed-rentals @(subscribe [::closed-rentals])]

    [:<>

     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)}

      (when-not is-loading? [:> UI/Components.Design.FilterButton
                             {:onClick #(js/alert "TODO!")}
                             (t :filter-bubble-label)])]

     [filter-panel] ; TODO: put into modal

     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
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
