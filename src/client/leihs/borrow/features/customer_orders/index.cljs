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

(reg-sub ::target-users
         :<- [::current-user/user-data]
         (fn [cu]
           (let [user (:user cu)
                 delegations (:delegations cu)]
             (concat [user] delegations))))

(reg-sub ::user-id
         :<- [::current-user/user-data]
         :<- [::filters/user-id]
         (fn [[co user-id]]
           (or user-id (-> co :user :id))))

;; UI

(defn filter-panel []
  (let [user-id @(subscribe [::user-id])
        target-users @(subscribe [::target-users])]
    (if (> (count target-users) 1)
      [:div.px-3.py-4.bg-light {:class "mt-3 mb-3"}
       [:div.form.form-compact
        [:label.row
         [:span.text-xs.col-3.col-form-label (t :!borrow.filter/for)]
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


(defn order-line [rental]
  (let
   [title (or (:title rental) (:purpose rental))
    href (routing/path-for ::routes/rentals-show :rental-id (:id rental))
    is-open (= (:state rental) "OPEN")
    refined-rental-state (:refined-rental-state rental)
    total-quantity (:total-quantity rental)
    summary #_(t (if is-open
                   (if (= total-quantity 1) :summary/open-singular :summary/open-plural)
                   (if (= total-quantity 1) :summary/closed-singular :summary/closed-plural))

                 {:days (:total-days rental)
                  :date (if is-open
                          (:from-date rental)
                          (:until-date rental))
                  :count total-quantity})
    (if is-open
      (str (t :summary/open1)
           (:total-days rental)
           (t :summary/open2)
           (ui/format-date :short (:from-date rental))
           (t :summary/open3)
           total-quantity
           (t :summary/open4))

      (str (t :summary/closed1)
           (:total-days rental)
           (t :summary/closed2)
           (ui/format-date :short (:until-date rental))
           (t :summary/closed3)
           total-quantity
           (t :summary/closed4)))]

    [:<>
     [:> UI/Components.Design.ListCard {:href href}
      [:> UI/Components.Design.ListCard.Title
       [:a.stretched-link {:href href}
        title]]

      [:> UI/Components.Design.ListCard.Body
       summary]

      [:> UI/Components.Design.ListCard.Foot
       [:> UI/Components.Design.Stack {:space 2}
        (doall
         (for [rental-state refined-rental-state]
           (let [title (t (str :refined-state-label "/" rental-state))]
             ^{:key rental-state}
             [:> UI/Components.Design.ProgressInfo
              (merge
               {:title title :small true}

               (cond
                 (= "IN_APPROVAL" rental-state)
                 ; NOTE: no partial approval, `done` will always be 0 when state is "IN_APPROVAL"
                 (let [total total-quantity
                       done 0]
                   {:totalCount total :doneCount done :info (str (t :fulfillment-state/items-approved1) done (t :fulfillment-state/items-approved2) total (t :fulfillment-state/items-approved3) (t :fulfillment-state/items-approved4))})

                 (= "TO_PICKUP" rental-state)
                 (let [ft (:pickup-fulfillment rental)
                       total (:to-fulfill-quantity ft)
                       done (:fulfilled-quantity ft)]
                   {:totalCount total :doneCount done :info (str (t :fulfillment-state/items-pickedup1) done (t :fulfillment-state/items-pickedup2) total (t :fulfillment-state/items-pickedup3) (t :fulfillment-state/items-pickedup4))})

                 (= "TO_RETURN" rental-state)
                 (let [ft (:return-fulfillment rental)
                       total (:to-fulfill-quantity ft)
                       done (:fulfilled-quantity ft)]
                   {:totalCount total :doneCount done :info (str (t :fulfillment-state/items-returned1) done (t :fulfillment-state/items-returned2) total (t :fulfillment-state/items-returned3) (t :fulfillment-state/items-returned4))})))])))]]]]))

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

    [:> UI/Components.Design.PageLayout

     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)}

      (when-not is-loading? [:> UI/Components.Design.FilterButton
                             {:onClick #(js/alert "TODO!")}
                             (t :filter-bubble-label)])]

     [filter-panel]

     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>

        [:> UI/Components.Design.Stack {:space 5}

         (when-not (empty? open-rentals)
           [:> UI/Components.Design.Section
            {:title (t :section-title-open-rentals) :collapsible true}
            [orders-list open-rentals]])]

        (when-not (empty? closed-rentals)
          [:> UI/Components.Design.Section
           {:title (t :section-title-closed-rentals) :collapsible true}
           [orders-list closed-rentals]])])]))
