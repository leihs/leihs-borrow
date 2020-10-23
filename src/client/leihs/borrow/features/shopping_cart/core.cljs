(ns leihs.borrow.features.shopping-cart.core
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
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
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.components :as ui]
    [leihs.borrow.ui.icons :as icons]
    [leihs.borrow.features.current-user.core :as current-user]
    ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.shopping-cart)

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/shopping-cart
  (fn-traced [{:keys [db]} [_ _]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/shopping_cart/getShoppingCart.gql")
                {:userId (filters/user-id db)}
                [::on-fetched-data]]}))

(reg-event-db
  ::on-fetched-data
  (fn-traced [db [_ {:keys [data errors]}]]
    (-> db
        (assoc ::data (get-in data
                              [:current-user :unsubmitted-order]))
        (assoc-in [::data :edit-mode] nil)
        (cond-> errors (assoc ::errors errors)))))

(reg-event-fx
  ::reset-availability-and-fetch
  (fn-traced [{:keys [db]} [_ args]]
    {:db (update-in db
                    [::data :edit-mode] 
                    dissoc 
                    :max-quantity)
     :dispatch [::fetch-available-quantity args]}))

(reg-event-fx
  ::fetch-available-quantity
  (fn-traced [_ [_ args]]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/shopping_cart/getModelAvailableQuantity.gql")
                args
                [::on-fetched-available-quantity]]}))

(reg-event-db
  ::on-fetched-available-quantity
  [(path ::data :edit-mode)]
  (fn-traced [em [_ {:keys [data errors]}]]
    (assoc em
           :max-quantity (-> data :model :available-quantity-in-date-range))))

(reg-event-fx
  ::delete-reservations
  (fn-traced [_ [_ ids]]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/shopping_cart/deleteReservationLines.gql")
                {:ids ids}
                [::on-delete-reservations]]}))

(reg-event-db
  ::on-delete-reservations
  (fn-traced [db [_ {{ids :delete-reservation-lines} :data errors :errors}]]
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
  (fn-traced [{:keys [db]} [_ res-lines]]
    (let [exemplar (first res-lines)
          user-id (-> exemplar :user :id)
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
                      :user-id user-id
                      :model model
                      :inventory-pools (map :inventory-pool res-lines)})
       :dispatch [::fetch-available-quantity
                  {:modelId (:id model)
                   :startDate start-date 
                   :endDate end-date
                   :excludeReservationIds (map :id res-lines)}]})))

(reg-event-fx
  ::submit-order
  (fn-traced [{:keys [db]} [_ args]]
    {:dispatch [::re-graph/mutate
                (rc/inline "leihs/borrow/features/shopping_cart/submitOrderMutation.gql")
                (merge args {:userId (filters/user-id db)})
                [::on-submit-order-result]]}))

(reg-event-fx
  ::on-submit-order-result
  (fn-traced [{:keys [_db]} [_ {:keys [data errors]}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:alert (str "OK! " (pr-str data))
       :routing/refresh-page "yes"})))

(reg-event-fx
  ::update-reservations
  (fn-traced [_ [_ args]]
    {:dispatch
     [::re-graph/mutate
      (rc/inline "leihs/borrow/features/shopping_cart/updateReservations.gql")
      args
      [::on-update-reservations-result]]}))

(reg-event-fx
  ::on-update-reservations-result
  (fn-traced [{:keys [db]}
       [_ {:keys [errors] {del-ids :delete-reservation-lines
                           new-res-lines :create-reservation} :data}]]
    (if errors
      {:alert (str "FAIL! " (pr-str errors))}
      {:db (update-in db
                      [::data :reservations]
                      (fn [rs]
                        (as-> rs <>
                          (filter #(->> % :id ((set del-ids)) not) <>)
                          (into <> new-res-lines))))
       :dispatch [::routes/shopping-cart]})))

(reg-event-db ::update-start-date
                 [(path ::data :edit-mode :start-date)]
                 (fn-traced [_ [_ v]] v))

(reg-event-db ::update-end-date
                 [(path ::data :edit-mode :end-date)]
                 (fn-traced [_ [_ v]] v))

(reg-event-db ::update-user-id
              [(path ::data :edit-mode :user-id)]
              (fn-traced [_ [_ v]] v))

(reg-event-db ::update-quantity
                 [(path ::data :edit-mode :quantity)]
                 (fn-traced [_ [_ v]] v))

(reg-event-db ::cancel-edit
                 [(path ::data)]
                 (fn-traced [co _] (assoc co :edit-mode nil)))

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
        current-user @(subscribe [::current-user/data])
        user-id (:user-id edit-mode-data)
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
                                             user-id
                                             start-date
                                             end-date])))))
        on-update #(dispatch [::update-reservations
                              {:ids (map :id res-lines)
                               :modelId (:id model)
                               :startDate start-date
                               :endDate end-date
                               :userId user-id
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
                                              :userId user-id
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
                                           :userId user-id
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
      [:div.w-1_2
       [:label.d-block.mb-0
        [:span.d-block.text-color-muted "for"]
        [:select {:name :user-id
                  :value user-id
                  :on-change  (fn [e]
                                (let [new-user-id (-> e .-target .-value)]
                                  (dispatch [::update-user-id new-user-id])
                                  (dispatch [::reset-availability-and-fetch
                                             {:modelId (:id model)
                                              :userId user-id
                                              :startDate start-date
                                              :endDate end-date
                                              :excludeReservationIds (map :id res-lines)}])))}
         (doall
           (for [user (cons (:user current-user) (:delegations current-user))]
             [:option {:value (:id user) :key (:id user)}
              (:name user)]))]]]
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
          [:span quantity (str " " (t :line/total-items))]
          [:span " • "]
          [:span pool-names]]]
        [:div.px-1.self-center.flex-none
         [:button.text-sm
          {:on-click #(dispatch [::delete-reservations (map :id res-lines)])}
          icons/trash-icon]
         [:button.rounded.border.border-gray-600.px-2.text-color-muted
          {:on-click #(dispatch [::edit-reservation res-lines])}
          (t :edit)]]])]))

(reg-sub ::target-users
         :<- [::current-user/data]
         (fn [cu]
             (let [user (:user cu)
                   delegations (:delegations cu)]
               (concat [user] delegations))))

(reg-sub ::user-id
         :<- [::current-user/data]
         :<- [::filters/user-id]
         (fn [[co user-id]]
             (or user-id (-> co :user :id))))

(defn search-panel []
  (let [user-id @(subscribe [::user-id])
        delegations @(subscribe [::current-user/delegations])
        target-users @(subscribe [::target-users])]
    (when-not (empty? delegations)
      [:div.px-3.py-4.bg-light {:class "mb-4"
                                :style {:box-shadow "0 0rem 2rem rgba(0, 0, 0, 0.15) inset"}}
       [:div.form.form-compact
        [:label.row
         [:span.text-xs.col-3.col-form-label "Für "]
         [:div.col-9
          [:select {:class "form-control"
                    :default-value user-id
                    :name :user-id
                    :on-change (fn [ev]
                                 (dispatch [::filters/set-one
                                            :user-id
                                            (-> ev .-target .-value)])
                                 (dispatch [::routes/shopping-cart]))}
           (doall
             (for [user target-users]
               [:option {:value (:id user) :key (:id user)}
                (:name user)]))]]]]])))

(defn view []
  (let [purpose (reagent/atom "")
        title (reagent/atom "")
        linked? (reagent/atom true)]
    (fn []
      (let [data @(subscribe [::data])
            invalid-res-ids (set (:invalid-reservation-ids data))
            errors @(subscribe [::errors])
            reservations @(subscribe [::reservations])
            grouped-reservations @(subscribe [::reservations-grouped])
            summary @(subscribe [::order-summary])
            is-loading? (not (or data errors))]
        [:> UI/Components.AppLayout.Page
         {:title (t :order-overview)}
         [search-panel]

         (cond
           is-loading? [:div.text-5xl.text-center.p-8 [ui/spinner-clock]]
           errors [ui/error-view errors]
           (empty? grouped-reservations)
           [:> UI/Components.AppLayout.CallToAction 
            {:actions [{:children (t :borrow-items) :href (routing/path-for ::routes/home)}]}
            (t :empty-order)]

           :else
           [:<>
            [:div
             [:label.row
              [:span.text-xs.col-3.col-form-label (t :order-title)]
              [:div.mt-2.mb-4.flex
               [:div.flex-grow
                [:input.text-xl.w-100
                 {:name :title
                  :value @title
                  :on-change (fn [e] (reset! title (-> e .-target .-value)))
                  :placeholder (t :order-title-placeholder)}]]]]

             [:label.row
              [:span.text-xs.col-3.col-form-label (t :order-purpose)]
              [:div.mt-2.mb-4.flex
               [:div.flex-grow
                [:textarea.text-xl.w-100
                 {:name :purpose
                  :value (or (and @linked? @title) @purpose)
                  :on-change (fn [e]
                               (reset! purpose (-> e .-target .-value))
                               (reset! linked? false))
                  :placeholder (t :order-purpose-placeholder)}]]]]

             (doall
               (for [[grouped-key res-lines] grouped-reservations]
                 [:<> {:key grouped-key}
                  [reservation res-lines invalid-res-ids]]))

             [:div.mt-4.text-sm.text-color-muted
              [:p
               (str (t :line/total) " ")
               (:total-models summary) ui/nbsp (str (t :line/total-models)
                                                    ", ")
               (:total-items summary) ui/nbsp  (str (t :line/total-items)
                                                    ", ") 
               (str (t :line/from) " ")
               (string/join ", " (map :name (:pools summary)))
               "."]
              [:p
               (str (t :line/first-pickup) " ")
               (ui/format-date :short (:earliest-start-date summary))
               (str ", " (t :line/last-return) " ")
               (ui/format-date :short (:latest-end-date summary))
               "."]]

             [:div
              [:button.w-100.p-2.my-4.rounded-full.bg-content-inverse.text-color-content-inverse.text-xl
               {:disabled (or (empty? @purpose)
                              (not (empty? invalid-res-ids)))
                :on-click #(dispatch [::submit-order {:purpose @purpose, :title @title}])}
               (t :confirm-order)]
              [:button.w-100.p-2.my-4.rounded-full.bg-content-danger.text-color-content-inverse.text-xl
               {:on-click #(dispatch [::delete-reservations (map :id reservations)])}
               (t :delete-order)]]]])]))))
