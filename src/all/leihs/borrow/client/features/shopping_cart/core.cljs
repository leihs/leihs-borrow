(ns leihs.borrow.client.features.shopping-cart.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.client.lib.helpers :as help]
    [leihs.borrow.client.lib.routing :as routing]
    [leihs.borrow.client.components :as ui]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/shopping-cart
 (fn [_ [_ _]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/client/features/shopping_cart/getShoppingCart.gql")
               {}
               [::on-fetched-data]]}))

(rf/reg-event-db
  ::on-fetched-data
  (fn [db [_ {:keys [data errors]}]]
    (-> db
        (assoc-in , [::current-order :errors] errors)
        (assoc-in , [::current-order :data]
                  (help/kebabize-keys
                    (get-in data
                            [:currentUser :unsubmittedOrder]))))))

(rf/reg-sub ::errors (fn [db] (get-in db [::current-order :errors])))

(rf/reg-sub
  ::current-order
  (fn [db _]
    (get-in db [::current-order :data])))

(rf/reg-sub
  ::reservations
  (fn [] [(rf/subscribe [::current-order])])
  (fn [[order]]
    (not-empty (get-in order [:reservations]))))

(rf/reg-sub
  ::reservations-grouped
  (fn [] [(rf/subscribe [::reservations])])
  (fn [[lines]]
    (->> lines
         (group-by
           (fn [line]
             [(get-in line [:model :id])
              (get-in line [:startDate])
              (get-in line [:endDate])])))))
(rf/reg-sub
  ::order-summary
  (fn [] [(rf/subscribe [::reservations])])
  (fn [[rs]]
    {:pools (-> (map :inventoryPool rs) distinct)
     :total-models (-> (map :model rs) distinct count)
     :total-items (-> (map :model rs) count)
     :earliest-start-date (-> (map :startDate rs) sort first)
     :latest-end-date (-> (map :endDate rs) sort last)}))

(rf/reg-event-fx
 ::submit-order
 (fn [_ [_ args]]
   {:dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/client/features/shopping_cart/submitOrderMutation.gql")
               args
               [::on-submit-order-result]]}))

(rf/reg-event-fx
  ::on-submit-order-result
  (fn [{:keys [_db]} [_ {:keys [data errors]}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:alert (str "OK! " (pr-str data))
       :routing/refresh-page "yes"})))


;_; VIEWS
(defn reservation-line [quantity line]
  (let
    [model (:model line)
     img (get-in model [:images 0])
     pool (get-in line [:inventoryPool :name])]
    [:div.flex.flex-row.border-b.mb-2.pb-2.-mx-1
     [:div.px-1.flex-none {:class "w-1/4"} [ui/image-square-thumb img]]
     [:div.px-1.flex-1
      [:div.font-semibold (:name model)]
      [:div.text-sm
       (ui/format-date :short (get-in line [:startDate]))
       (str ui/thin-space "–" ui/thin-space)
       (ui/format-date :short (get-in line [:endDate]))]
      [:div.text-sm.text-color-muted
       [:span quantity " Items"]
       [:span " • "]
       [:span pool]]]
     [:div.px-1.self-center.flex-none
      [:button.text-sm
       {:on-click #(js/alert "TODO")}
       ui/trash-icon]]]))


(defn view []
  (let [state (reagent/atom {:purpose ""})]
    (fn []
      (let [order @(rf/subscribe [::current-order])
            errors @(rf/subscribe [::errors])
            reservations @(rf/subscribe [::reservations-grouped])
            summary @(rf/subscribe [::order-summary])
            is-loading? (not (or order errors))]
        [:div.p-2
         [:h1.mt-2.font-bold.text-3xl "Order Overview"]

         (cond
           is-loading? [:div.text-5xl.text-center.p-8 [ui/spinner-clock]]
           errors [ui/error-view errors]
           (empty? reservations)
           [:div.bg-content-muted.text-center.my-6.px-4.py-6.rounded-lg
            [:div "Your order ist empty."] 
            [:a.inline-block.text-xl.bg-content-inverse.text-color-content-inverse.rounded-full.px-6.py-2.my-4 
             {:href (routing/path-for ::routes/home)}
             "Borrow Items"]]

           :else
           [:<>
            [:div
             [:div.mt-2.mb-4.flex
              [:div.flex-grow
               [:input.text-xl.w-100
                {:name :purpose
                 :value (:purpose @state)
                 :on-change (fn [e] (swap! state assoc :purpose (-> e .-target .-value)))
                 :placeholder "Name Your Order"}]]
              [:div.flex-none.px-1
               [:button.rounded.border.border-gray-600.px-2.text-color-muted "edit"]]]

             (doall
               (for [[grouped-key lines] reservations]
                 (let [line (first lines) quantity (count lines)]
                   [:<> {:key grouped-key}
                    [reservation-line quantity line]])))

             #_[:label.w-100
             [:span.text-xs.block.mt-4
             "Optional: enter more details about the purpose of the order (if the name is sufficient)"]
             [:input.text-md.w-100.my-2
             {:placeholder "details about the order purpose"}]]

             [:div.mt-4.text-sm.text-color-muted
              [:p
               "Total "
               (:total-models summary) ui/nbsp "Model(s), "
               (:total-items summary) ui/nbsp "Item(s), "
               "from "
               (string/join ", " (map :name (:pools summary)))
               "."]
              [:p
               "First pickup "
               (ui/format-date :short (:earliest-start-date summary))
               ", last return "
               (ui/format-date :short (:latest-end-date summary))
               "."]]

             [:div
              [:button.w-100.p-2.my-4.rounded-full.bg-content-inverse.text-color-content-inverse.text-xl
               {:disabled (empty? (:purpose @state))
                :on-click #(rf/dispatch [::submit-order @state])}
               "Confirm order"]]

             #_[:div.mt-4 [:hr] [:p.font-mono.m-2 (pr-str order)]]]])])))
  )
