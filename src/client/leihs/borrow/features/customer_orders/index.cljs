(ns leihs.borrow.features.customer-orders.index
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    #_[reagent.core :as reagent]
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
    [leihs.borrow.lib.helpers :refer [log]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.features.current-user.core :as current-user]
    ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.customer-orders)

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
  ::submitted-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:submitted-orders :edges])
         (map :node) 
         not-empty)))

(reg-sub 
  ::rejected-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:rejected-orders :edges])
         (map :node)
         not-empty)))

(reg-sub 
  ::approved-orders 
  :<- [::data]
  (fn [data _]
    (->> (get-in data [:approved-orders :edges])
         (map :node)
         not-empty)))

(defn order-line [order]
  (let
    [label (:purpose order)
     href (routing/path-for ::routes/rentals-show :rental-id (:id order))
     rental-state (:rental-state order)
     refined-rental-state (:refined-rental-state order)]
    [:<>
     [:a {:href href} 
      label " "
      [:span.text-color-muted (ui/format-date :short (:created-at order))]
      (doall 
        (for [s refined-rental-state]
          [:span {:class (cond
                           (and (= rental-state "OPEN")
                                (not= s "EXPIRED")) "badge rounded-pill bg-warning"
                           (or (= rental-state "CLOSED")
                               (= s "EXPIRED")) "badge rounded-pill bg-info")}
           s]))]]))

(defn orders-list [orders]
  [:ul (doall (for [order orders] [:li {:key (:id order)} [order-line order]]))])

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

(defn search-panel []
  (let [user-id @(subscribe [::user-id])
        target-users @(subscribe [::target-users])]
    (log user-id)
    (log target-users)
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
              (:name user)]))]]]]]))

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])
        is-loading? (not (or data errors))
        submitted-orders @(subscribe [::submitted-orders])
        rejected-orders @(subscribe [::rejected-orders])
        approved-orders @(subscribe [::approved-orders])]

    [:> UI/Components.AppLayout.Page
     {:title (t :title)}
     [search-panel]
     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       errors [ui/error-view errors]
       :else
       [:<>
        (when-not (empty? submitted-orders)
          [:div.mt-3
           [:h2.text-xl.font-semibold (t :active-orders)]
           [orders-list submitted-orders ""]])

        (when-not (empty? rejected-orders)
          [:div.mt-3
           [:h2.text-xl.font-semibold (t :rejected-orders)]
           [orders-list rejected-orders]])

        (when-not (empty? approved-orders)
          [:div.mt-3
           [:h2.text-xl.font-semibold (t :approved-orders)]
           [orders-list approved-orders ""]])

        #_[:pre {:style {:white-space :pre-wrap}} (pr-str rejected-orders)]

        #_[:pre {:style {:white-space :pre-wrap}} (pr-str approved-orders)]

        #_[:p.debug (pr-str data)]])]))
