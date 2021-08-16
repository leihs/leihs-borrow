(ns leihs.borrow.features.shopping-cart2.core
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    ["date-fns" :as datefn]
    [clojure.string :as string]
    [cljs-time.core :as tc]
    [cljs-time.format :as tf]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [re-frame.std-interceptors :refer [path]]
    [shadow.resource :as rc]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.helpers :as help :refer [log spy camel-case-keys]]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path with-translate-path]]
    [leihs.borrow.components :as ui]
    [leihs.borrow.ui.icons :as icons]
    [leihs.borrow.features.current-user.core :as current-user]
    [leihs.borrow.features.shopping-cart2.timeout :as timeout]
    ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.shopping-cart2)

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/shopping-cart
  (fn-traced [{:keys [db]} [_ _]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/shopping_cart2/getShoppingCart.gql")
                {:userId (filters/user-id db)}
                [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ {:keys [data errors]}]]
    (-> db
        (assoc ::data (get-in data
                              [:current-user :user :unsubmitted-order]))
        (assoc-in [::data :edit-mode] nil)
        (cond-> errors (assoc ::errors errors)))))

(reg-sub ::data
         (fn [db _] (::data db)))

(reg-sub ::errors
         (fn [db _] (::errors db)))

(reg-sub ::reservations
         :<- [::data]
         (fn [co _] (:reservations co)))

(reg-sub ::reservations-grouped
         :<- [::reservations]
         (fn [lines _]
           (->> lines
                (map camel-case-keys)
                (group-by
                  (fn [line]
                    [(get-in line [:model :id])
                     (get-in line [:inventoryPool :id])
                     (get-in line [:startDate])
                     (get-in line [:endDate])])))))

(defn empty-new-rental []
  [:> UI/Components.Design.Stack {:space 4 :className "text-center"}
   (t :empty-order/no-items)
   [:a.text-decoration-underline {:href "/app/borrow/"}
    (t :empty-order/link-to-catalog)]])

(defn delegations-switcher []
  (let [selected-id @(subscribe [::filters/user-id])
        user @(subscribe [::current-user/user-data])]
    [:div.mb-4
     [:> UI/Components.Delegations.Switcher
      {:user user
       :selectedId selected-id
       :t {:personal (t :!borrow.delegations/personal)}
       :onChangeCallback (fn [e]
                           (let [v  (-> e .-target .-value)]
                             (dispatch [::filters/set-one :user-id v])
                             (dispatch [::timeout/refresh v])
                             (dispatch [::routes/shopping-cart])))}]]))

(defn countdown []
  (let [now (r/atom (js/Date.))]
    (fn []
      (js/setInterval #(reset! now (js/Date.)) 1000)
      (let [data @(subscribe [::data])
            user-id @(subscribe [::filters/user-id])
            valid-until (-> data :valid-until datefn/parseISO)
            total-count 30
            elapsed-count (datefn/differenceInMinutes valid-until @now)]
        [:div.mb-4
         [:> UI/Components.ShoppingCart.Countdown
          {:totalCount total-count
           :doneCount (- total-count elapsed-count)
           :onResetTimeLimitClick #(dispatch [::timeout/refresh user-id])
           :t (with-translate-path :borrow.timeout
                {:title (t :limit/title)
                 :still (t :still)
                 :minutesLeft (t :minutes-left)
                 :resetLimit (t :limit/reset)})}]]))))

(defn view []
  (let [data @(subscribe [::data])
        errors @(subscribe [::errors])
        reservations @(subscribe [::reservations])
        grouped-reservations @(subscribe [::reservations-grouped])
        is-loading? (not (or data errors))]
    (log grouped-reservations)
    [:> UI/Components.AppLayout.Page
     (cond
       is-loading? [:div.text-5xl.text-center.p-8 [ui/spinner-clock]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:> UI/Components.Design.PageLayout.Header {:title (t :title)}]
        [delegations-switcher]
        (if (empty? reservations)
          [empty-new-rental]
          [:<>
           [countdown]
           [:> UI/Components.ShoppingCart.Items {:groupedRs grouped-reservations}]])])]))
