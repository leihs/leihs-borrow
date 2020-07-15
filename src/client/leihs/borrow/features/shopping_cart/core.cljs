(ns leihs.borrow.features.shopping-cart.core
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    [clojure.string :as string]
    [cljs-time.core :as tc]
    [cljs-time.format :as tf]
    [reagent.core :as reagent]
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
    [leihs.borrow.lib.helpers :as help]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.components :as ui]
    [leihs.borrow.ui.icons :as icons]))


; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/shopping-cart
 (fn [_ [_ _]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/shopping_cart/getShoppingCart.gql")
               {}
               [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn [db [_ {:keys [data errors]}]]
    (-> db
        (assoc ::data (help/kebabize-keys
                        (get-in data
                                [:current-user :unsubmitted-order])))
        (assoc-in [::data :edit-mode] nil)
        (cond-> errors (assoc ::errors errors)))))

(reg-event-fx
  ::reset-availability-and-fetch
  (fn [{:keys [db]} [_ args]]
    {:db (update-in db
                    [::data :edit-mode] 
                    dissoc 
                    :max-quantity)
     :dispatch [::fetch-available-quantity args]}))

(reg-event-fx
  ::fetch-available-quantity
  (fn [_ [_ args]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/shopping_cart/getModelAvailableQuantity.gql")
                args
                [::on-fetched-available-quantity]]}))

(reg-event-db
  ::on-fetched-available-quantity
  [(path ::data :edit-mode)]
  (fn [em [_ {:keys [data errors]}]]
    (assoc em
           :max-quantity (-> data :model :available-quantity-in-date-range))))

(reg-event-fx
  ::delete-reservations
  (fn [_ [_ ids]]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/shopping_cart/deleteReservationLines.gql")
                {:ids ids}
                [::on-delete-reservations]]}))

(reg-event-db
  ::on-delete-reservations
  (fn [db [_ {{ids :delete-reservation-lines} :data errors :errors}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      (update-in db
                 [::data :reservations]
                 (partial filter #(->> %
                                       :id
                                       ((set ids))
                                       not))))))

(reg-event-fx
  ::edit-reservation
  (fn [{:keys [db]} [_ res-lines]]
    (let [exemplar (first res-lines)
          model (:model exemplar)
          start-date (:start-date exemplar)
          end-date (:end-date exemplar)
          quantity (count res-lines)]
      {:db (assoc-in db
                     [::data :edit-mode]
                     {:reservation-lines res-lines
                      :start-date start-date
                      :end-date end-date
                      :quantity quantity
                      :model model
                      :inventory-pools (map :inventory-pool res-lines)})
       :dispatch [::fetch-available-quantity
                  {:modelId (:id model)
                   :startDate start-date 
                   :endDate end-date
                   :excludeReservationIds (map :id res-lines)}]})))

(reg-event-fx
  ::submit-order
  (fn [_ [_ args]]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/shopping_cart/submitOrderMutation.gql")
                args
                [::on-submit-order-result]]}))

(reg-event-fx
  ::on-submit-order-result
  (fn [{:keys [_db]} [_ {:keys [data errors]}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:alert (str "OK! " (pr-str data))
       :routing/refresh-page "yes"})))

(reg-event-fx
  ::update-reservations
  (fn [_ [_ args]]
    {:dispatch
     [::re-graph/mutate
      (rc/inline "leihs/borrow/features/shopping_cart/updateReservations.gql")
      args
      [::on-update-reservations-result]]}))

(reg-event-fx
  ::on-update-reservations-result
  (fn [{:keys [db]}
       [_ {:keys [errors] {del-ids :delete-reservation-lines
                           new-res-lines :create-reservation} :data}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:db (update-in db
                      [::data :reservations]
                      (fn [rs]
                        (as-> rs <>
                          (filter #(->> % :id ((set del-ids)) not) <>)
                          (into <> new-res-lines))))})))

(reg-event-db ::update-start-date
                 [(path ::data :edit-mode :start-date)]
                 (fn [_ [_ v]] v))

(reg-event-db ::update-end-date
                 [(path ::data :edit-mode :end-date)]
                 (fn [_ [_ v]] v))

(reg-event-db ::update-quantity
                 [(path ::data :edit-mode :quantity)]
                 (fn [_ [_ v]] v))

(reg-event-db ::cancel-edit
                 [(path ::data)]
                 (fn [co _] (assoc co :edit-mode nil)))

(reg-sub ::data
            (fn [db _] (::data db)))

(reg-sub ::errors
            (fn [db _] (::errors db)))

(reg-sub ::edit-mode-data
            :<- [::data]
            (fn [data _] (:edit-mode data)))

(reg-sub ::max-quantity
            :<- [::edit-mode-data]
            (fn [emd _] (:max-quantity emd)))

(reg-sub ::edit-mode?
            :<- [::edit-mode-data]
            (fn [em [_ res-lines]]
              (boolean
                (some->> em
                         :reservation-lines
                         (map :id)
                         set
                         (= (->> res-lines (map :id) set))))))

(reg-sub ::reservations
            :<- [::data]
            (fn [co _] (:reservations co)))

(reg-sub ::reservations-grouped
            :<- [::reservations]
            (fn [lines _]
              (->> lines
                   (group-by
                     (fn [line]
                       [(get-in line [:model :id])
                        (get-in line [:start-date])
                        (get-in line [:end-date])])))))

(reg-sub ::order-summary
            :<- [::reservations]
            (fn [rs _]
              {:pools (-> (map :inventory-pool rs) distinct)
               :total-models (-> (map :model rs) distinct count)
               :total-items (-> (map :model rs) count)
               :earliest-start-date (-> (map :start-date rs) sort first)
               :latest-end-date (-> (map :end-date rs) sort last)}))

(reg-sub ::timed-out?
            :<- [::data]
            (fn [co _]
              (boolean (some->> co
                                :valid-until
                                tf/parse
                                (tc/after? (tc/now))))))

(defn edit-reservation [res-lines]
  (let [edit-mode-data @(subscribe [::edit-mode-data])
        model (:model edit-mode-data)
        start-date (:start-date edit-mode-data)
        end-date (:end-date edit-mode-data)
        quantity (:quantity edit-mode-data)
        max-quantity @(subscribe [::max-quantity])
        on-change-date-fn (fn [reset-fn]
                            (fn [e]
                              (let [v (.-value (.-target e))]
                                (reset-fn v)
                                (when (and @start-date @end-date)
                                  (dispatch [::reset-availability-and-fetch
                                                (:id model)
                                                start-date
                                                end-date])))))
        on-update #(dispatch [::update-reservations
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
                               (dispatch [::update-start-date new-start-date])
                               (dispatch [::reset-availability-and-fetch
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
                               (dispatch [::update-end-date new-end-date])
                               (dispatch [::reset-availability-and-fetch
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
                           (dispatch [::update-quantity new-quantity])))}]]]
        [:div.flex-1.w-1_2 [:span.no-underline.text-color-muted 
                            {:aria-label (str "maximum available quantity is " max-quantity)} 
                            "/" ui/thin-space (or max-quantity [ui/spinner-clock]) ui/thin-space "max."]]]]
      [:div.flex-auto.w-1_2
       [:button.px-4.py-2.w-100.rounded-lg.font-semibold.text-lg
        {:on-click #(dispatch [::cancel-edit])}
        "Cancel"]
       [:button.px-4.py-2.w-100.rounded-lg.bg-content-inverse.text-color-content-inverse.font-semibold.text-lg
        (cond-> {:on-click on-update}
          (> quantity max-quantity)
          (assoc :disabled true))
        "Update"]]]]))

;_; VIEWS
(defn reservation [res-lines invalid-res-ids]
  (let [exemplar (first res-lines)
        model (:model exemplar)
        img (get-in model [:images 0])
        quantity (count res-lines)
        edit-mode? @(subscribe [::edit-mode? res-lines])
        pool-names (->> res-lines
                        (map (comp :name :inventory-pool))
                        distinct
                        (clojure.string/join ", "))
        invalid? (every? invalid-res-ids (map :id res-lines))]
    (js/console.log invalid?)
    [:div.flex.flex-row.border-b.mb-2.pb-2.-mx-1
     [:div.px-1.flex-none {:class "w-1/4"} [ui/image-square-thumb img]]
     (if edit-mode?
       [edit-reservation res-lines]
       [:div
        [:div.px-1.flex-1 (if invalid? {:style {:color "red"}})
         [:div.font-semibold (:name model)]
         [:div.text-sm
          (ui/format-date :short (get-in exemplar [:start-date]))
          (str ui/thin-space "–" ui/thin-space)
          (ui/format-date :short (get-in exemplar [:end-date]))]
         [:div.text-sm.text-color-muted (if invalid? {:style {:color "red"}})
          [:span quantity " Items"]
          [:span " • "]
          [:span pool-names]]]
        [:div.px-1.self-center.flex-none
         [:button.text-sm
          {:on-click #(dispatch [::delete-reservations (map :id res-lines)])}
          icons/trash-icon]
         [:button.rounded.border.border-gray-600.px-2.text-color-muted
          {:on-click #(dispatch [::edit-reservation res-lines])}
          "Edit"]]])]))

(defn view []
  (let [state (reagent/atom {:purpose ""})]
    (fn []
      (let [data @(subscribe [::data])
            invalid-res-ids (set (:invalid-reservation-ids data))
            errors @(subscribe [::errors])
            reservations @(subscribe [::reservations])
            grouped-reservations @(subscribe [::reservations-grouped])
            summary @(subscribe [::order-summary])
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
                  [reservation res-lines invalid-res-ids]]))

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
               {:disabled (or (empty? (:purpose @state))
                              (not (empty? invalid-res-ids)))
                :on-click #(dispatch [::submit-order @state])}
               "Confirm order"]
              [:button.w-100.p-2.my-4.rounded-full.bg-content-danger.text-color-content-inverse.text-xl
               {:on-click #(dispatch [::delete-reservations (map :id reservations)])}
               "Delete order"]]]])]))))
