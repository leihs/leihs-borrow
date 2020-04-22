(ns leihs.borrow.client.features.shopping-cart.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   [clojure.string :as string]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [re-frame.std-interceptors :refer [path]]
   [shadow.resource :as rc]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.lib.helpers :as help]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.ui.icons :as icons]))


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
  [(path ::current-order)]
  (fn [co [_ {:keys [data errors]}]]
    (assoc co
           :errors errors
           :data (help/kebabize-keys
                   (get-in data
                           [:currentUser :unsubmittedOrder]))
           :edit-mode nil)))

(rf/reg-event-fx
  ::reset-availability-and-fetch
  (fn [{:keys [db]} [_ args]]
    {:db (update-in db
                    [::current-order :edit-mode] 
                    dissoc 
                    :max-quantity)
     :dispatch [::fetch-available-quantity args]}))

(rf/reg-event-fx
  ::fetch-available-quantity
  (fn [_ [_ args]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/client/features/shopping_cart/getModelAvailableQuantity.gql")
                args
                [::on-fetched-available-quantity]]}))

(rf/reg-event-db
  ::on-fetched-available-quantity
  [(path ::current-order :edit-mode)]
  (fn [em [_ {:keys [data errors]}]]
    (assoc em
           :max-quantity (-> data :model :availableQuantityInDateRange))))

(rf/reg-event-fx
  ::delete-reservations
  (fn [_ [_ ids]]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/client/features/shopping_cart/deleteReservationLines.gql")
                {:ids ids}
                [::on-delete-reservations]]}))

(rf/reg-event-db
  ::on-delete-reservations
  (fn [db [_ {{ids :deleteReservationLines} :data errors :errors}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      (update-in db
                 [::current-order :data :reservations]
                 (partial filter #(->> %
                                       :id
                                       ((set ids))
                                       not))))))

(rf/reg-event-fx
  ::edit-reservation
  (fn [{:keys [db]} [_ res-lines]]
    (let [exemplar (first res-lines)
          model (:model exemplar)
          start-date (:startDate exemplar)
          end-date (:endDate exemplar)
          quantity (count res-lines)]
      {:db (assoc-in db
                     [::current-order :edit-mode]
                     {:reservation-lines res-lines
                      :start-date start-date
                      :end-date end-date
                      :quantity quantity
                      :model model
                      :inventory-pools (map :inventoryPool res-lines)})
       :dispatch [::fetch-available-quantity
                  {:modelId (:id model)
                   :startDate start-date 
                   :endDate end-date
                   :excludeReservationIds (map :id res-lines)}]})))

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

(rf/reg-event-fx
  ::update-reservations
  (fn [_ [_ args]]
    {:dispatch
     [::re-graph/mutate
      (rc/inline "leihs/borrow/client/features/shopping_cart/updateReservations.gql")
      args
      [::on-update-reservations-result]]}))

(rf/reg-event-fx
  ::on-update-reservations-result
  (fn [{:keys [db]}
       [_ {:keys [errors] {del-ids :deleteReservationLines
                           new-res-lines :createReservation} :data}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:db (update-in db
                      [::current-order :data :reservations]
                      (fn [rs]
                        (as-> rs <>
                          (filter #(->> % :id ((set del-ids)) not) <>)
                          (into <> new-res-lines))))})))

(rf/reg-event-db ::update-start-date
                 [(path ::current-order :edit-mode :start-date)]
                 (fn [_ [_ v]] v))

(rf/reg-event-db ::update-end-date
                 [(path ::current-order :edit-mode :end-date)]
                 (fn [_ [_ v]] v))

(rf/reg-event-db ::update-quantity
                 [(path ::current-order :edit-mode :quantity)]
                 (fn [_ [_ v]] v))

(rf/reg-event-db ::cancel-edit
                 [(path ::current-order)]
                 (fn [co _] (assoc co :edit-mode nil)))

(rf/reg-sub ::current-order
            (fn [db _] (::current-order db)))

(rf/reg-sub ::errors
            :<- [::current-order]
            (fn [co _] (:errors co)))

(rf/reg-sub ::data
            :<- [::current-order]
            (fn [co _] (:data co)))

(rf/reg-sub ::edit-mode-data
            :<- [::current-order]
            (fn [co _] (:edit-mode co)))

(rf/reg-sub ::max-quantity
            :<- [::edit-mode-data]
            (fn [emd _] (:max-quantity emd)))

(rf/reg-sub ::edit-mode?
            :<- [::edit-mode-data]
            (fn [em [_ res-lines]]
              (boolean
                (some->> em
                         :reservation-lines
                         (map :id)
                         set
                         (= (->> res-lines (map :id) set))))))

(rf/reg-sub ::reservations
            :<- [::data]
            (fn [co _] (:reservations co)))

(rf/reg-sub ::reservations-grouped
            :<- [::reservations]
            (fn [lines _]
              (->> lines
                   (group-by
                     (fn [line]
                       [(get-in line [:model :id])
                        (get-in line [:startDate])
                        (get-in line [:endDate])])))))

(rf/reg-sub ::order-summary
            :<- [::reservations]
            (fn [rs _]
              {:pools (-> (map :inventoryPool rs) distinct)
               :total-models (-> (map :model rs) distinct count)
               :total-items (-> (map :model rs) count)
               :earliest-start-date (-> (map :startDate rs) sort first)
               :latest-end-date (-> (map :endDate rs) sort last)}))

(defn edit-reservation [res-lines]
  (let [edit-mode-data @(rf/subscribe [::edit-mode-data])
        model (:model edit-mode-data)
        start-date (:start-date edit-mode-data)
        end-date (:end-date edit-mode-data)
        quantity (:quantity edit-mode-data)
        max-quantity @(rf/subscribe [::max-quantity])
        on-change-date-fn (fn [reset-fn]
                            (fn [e]
                              (let [v (.-value (.-target e))]
                                (reset-fn v)
                                (when (and @start-date @end-date)
                                  (rf/dispatch [::reset-availability-and-fetch
                                                (:id model)
                                                start-date
                                                end-date])))))
        on-update #(rf/dispatch [::update-reservations
                                 {:ids (map :id res-lines)
                                  :modelId (:id model)
                                  :startDate start-date
                                  :endDate end-date
                                  :quantity quantity}])]
    [:div.border-b-2.border-gray-300.mt-4.pb-4
     [:h3.font-bold.text-lg.Xtext-color-muted.mb-2 "Change reservation"]
     [:div.d-flex.mx-n2
      [:label.px-2.w-1_2
       [:span.text-color-muted "from "]
       [:input {:type :date
                :name :start-date
                :value start-date
                :on-change (fn [e]
                             (let [new-start-date (-> e .-target .-value)]
                               (rf/dispatch [::update-start-date new-start-date])
                               (rf/dispatch [::reset-availability-and-fetch
                                             {:modelId (:id model)
                                              :startDate new-start-date 
                                              :endDate end-date
                                              :excludeReservationIds (map :id res-lines)}])))}]]
      [:label.px-2.w-1_2
       [:span.text-color-muted "until "]
       [:input {:type :date
                :name :end-date
                :value end-date
                :on-change (fn [e]
                             (let [new-end-date (-> e .-target .-value)]
                               (rf/dispatch [::update-end-date new-end-date])
                               (rf/dispatch [::reset-availability-and-fetch
                                             {:modelId (:id model)
                                              :startDate start-date
                                              :endDate new-end-date
                                              :excludeReservationIds (map :id res-lines)}])))}]]]
     [:div.d-flex.items-end.mt-2
      [:div.w-1_2
       [:div.d-flex.flex-wrap.align-items-end
        [:div.w-1_2
         [:label.d-block.mb-0
          [:span.d-block.text-color-muted "quantity "]
          [:input.w-full
           {:type :number
            :name :quantity
            :max max-quantity
            :value quantity
            :disabled (or (nil? max-quantity) (> quantity max-quantity))
            :on-change (fn [e]
                         (let [new-quantity (-> e .-target .-value js/parseInt)]
                           (rf/dispatch [::update-quantity new-quantity])))}]]]
        [:div.flex-1.w-1_2 [:span.no-underline.text-color-muted 
                            {:aria-label (str "maximum available quantity is " max-quantity)} 
                            "/" ui/thin-space (or max-quantity [ui/spinner-clock]) ui/thin-space "max."]]]]
      [:div.flex-auto.w-1_2
       [:button.px-4.py-2.w-100.rounded-lg.font-semibold.text-lg
        {:on-click #(rf/dispatch [::cancel-edit])}
        "Cancel"]
       [:button.px-4.py-2.w-100.rounded-lg.bg-content-inverse.text-color-content-inverse.font-semibold.text-lg
        (cond-> {:on-click on-update}
          (> quantity max-quantity)
          (assoc :disabled true))
        "Update"]]]]))

;_; VIEWS
(defn reservation [res-lines]
  (let [exemplar (first res-lines)
        model (:model exemplar)
        img (get-in model [:images 0])
        quantity (count res-lines)
        edit-mode? @(rf/subscribe [::edit-mode? res-lines])
        pool-names (->> res-lines
                        (map (comp :name :inventoryPool))
                        distinct
                        (clojure.string/join ", "))]
    [:div.flex.flex-row.border-b.mb-2.pb-2.-mx-1
     [:div.px-1.flex-none {:class "w-1/4"} [ui/image-square-thumb img]]
     (if #_true edit-mode?
       [edit-reservation res-lines]
       [:div
        [:div.px-1.flex-1
         [:div.font-semibold (:name model)]
         [:div.text-sm
          (ui/format-date :short (get-in exemplar [:startDate]))
          (str ui/thin-space "–" ui/thin-space)
          (ui/format-date :short (get-in exemplar [:endDate]))]
         [:div.text-sm.text-color-muted
          [:span quantity " Items"]
          [:span " • "]
          [:span pool-names]]]
        [:div.px-1.self-center.flex-none
         [:button.text-sm
          {:on-click #(rf/dispatch [::delete-reservations (map :id res-lines)])}
          icons/trash-icon]
         [:button.rounded.border.border-gray-600.px-2.text-color-muted
          {:on-click #(rf/dispatch [::edit-reservation res-lines])}
          "Edit"]]])]))

(defn view []
  (let [state (reagent/atom {:purpose ""})]
    (fn []
      (let [data @(rf/subscribe [::data])
            errors @(rf/subscribe [::errors])
            reservations @(rf/subscribe [::reservations])
            grouped-reservations @(rf/subscribe [::reservations-grouped])
            summary @(rf/subscribe [::order-summary])
            is-loading? (not (or data errors))]
        [:div.p-2
         [:h1.mt-3.font-bold.text-3xl "Order Overview"]

         (cond
           is-loading? [:div.text-5xl.text-center.p-8 [ui/spinner-clock]]
           errors [ui/error-view errors]
           (empty? grouped-reservations)
           [:div.bg-content-muted.text-center.my-4.px-2.py-4.rounded-lg
            [:div.text-base "Your order is empty."] 
            [:a.d-inline-block.text-xl.bg-content-inverse.text-color-content-inverse.rounded-pill.px-4.py-2.my-4 
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
               (for [[grouped-key res-lines] grouped-reservations]
                 [:<> {:key grouped-key}
                  [reservation res-lines]]))

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
               "Confirm order"]
              [:button.w-100.p-2.my-4.rounded-full.bg-content-danger.text-color-content-inverse.text-xl
               {:on-click #(rf/dispatch [::delete-reservations (map :id reservations)])}
               "Delete order"]]]])]))))
