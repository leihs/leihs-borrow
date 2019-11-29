(ns leihs.borrow.client.features.shopping-cart
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/shopping-cart
 (fn [_ [_ _]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/client/queries/getShoppingCart.gql")
               {}
               [::on-fetched-data]]}))

(rf/reg-event-db
 ::on-fetched-data
 (fn [db [_ {:keys [data errors]}]]
   (-> db
       (assoc-in , [::current-order :errors] errors)
       (assoc-in , [::current-order :data] (get-in data [:currentUser :unsubmittedOrder])))))

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

(rf/reg-event-fx
 ::submit-order
 (fn [_ [_ args]]
   {:dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/client/queries/submitOrderMutation.gql")
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
    [:div.flex.flex-row.border-b.mb-2.pb-2
     [:div.pr-2.flex-none {:class "w-1/4"} [ui/image-square-thumb img]]
     [:div.pl-2.flex-1
      [:div.font-semibold (:name model)]
      [:div.text-sm 
       (get-in line [:startDate]) 
       (str ui/thin-space "–" ui/thin-space)
       (get-in line [:endDate])]
      [:div.text-sm.text-color-muted
       [:span quantity " Items"]
       [:span " • "]
       [:span pool]]]
     [:div.self-center.flex-none
      [:button.text-sm
       {:on-click #(js/alert "TODO")}
       ui/trash-icon]]])
  )


(defn view []
  (let [order @(rf/subscribe [::current-order])
        errors @(rf/subscribe [::errors])
        reservations @(rf/subscribe [::reservations-grouped])
        is-loading? (not (or order errors))
        state (reagent/atom {:purpose ""})]

    (fn []
      [:div.p-2
       [:h1.mt-2.font-bold.text-3xl "Order Overview"]

       [:input.text-xl.my-2 
        {:name :purpose
         :value (:purpose @state)
         :on-change (fn [e] (swap! state assoc :purpose (-> e .-target .-value)))
         :placeholder "Name Your Order"}]

       #_[:p.font-mono.m-2 (pr-str reservations)]

       (cond
         is-loading? [:div.text-5xl.text-center.p-8 [ui/spinner-clock]]
         errors [ui/error-view errors]
         :else
         [:<>
          (when reservations
            [:div
             (doall
              (for [[_keys lines] reservations]
                (let [line (first lines) quantity (count lines)]
                  [reservation-line quantity line])))

             #_[:label.w-100
                [:span.text-xs.block.mt-4
                 "Optional: enter more details about the purpose of the order (if the name is sufficient)"]
                [:input.text-md.w-100.my-2
                 {:placeholder "details about the order purpose"}]]

             [:div
              [:button.w-100.p-2.my-4.rounded-full.bg-black.text-white.text-xl
               {:on-click #(rf/dispatch [::submit-order @state])}
               "Confirm order"]]

             #_[:div.mt-4 [:hr] [:p.font-mono.m-2 (pr-str order)]]])])])))
