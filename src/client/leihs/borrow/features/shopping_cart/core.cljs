(ns leihs.borrow.features.shopping-cart.core
  (:require
   ["date-fns" :as datefn]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [clojure.string :as string]
   [reagent.core :as reagent]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.filters :as filters]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.core.core :refer [dissoc-in]]
   [leihs.borrow.features.shopping-cart.timeout :as timeout]
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
                             [:current-user :user :unsubmitted-order]))
       (assoc-in [::data :edit-mode] nil)
       (cond-> errors (assoc ::errors errors)))))

(reg-event-fx
 ::delete-reservations
 (fn-traced [{:keys [db]} [_ ids]]
   {:db (assoc-in db [::data :pending-count] (- 0 (count ids)))
    :dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/features/shopping_cart/deleteReservationLines.gql")
               {:ids ids}
               [::on-delete-reservations]]}))

(reg-event-fx
 ::on-delete-reservations
 (fn-traced [{:keys [db]} [_ {{ids :delete-reservation-lines} :data errors :errors}]]
   (if errors
     {:db (dissoc-in db [::data :pending-count])
      :alert (str "FAIL! " (pr-str errors))}
     {:db (-> db
              (update-in [::data :reservations]
                         (partial filter #(->> % :id ((set ids)) not)))
              (dissoc-in [::data :pending-count]))
      :dispatch [::timeout/refresh]})))

(reg-event-fx
 ::submit-order
 (fn-traced [{:keys [db]} [_ args]]
   {:dispatch [::re-graph/mutate
               (rc/inline "leihs/borrow/features/shopping_cart/submitOrderMutation.gql")
               (merge args {:userId (filters/user-id db)})
               [::on-submit-order-result]]}))

(reg-event-fx
 ::on-submit-order-result
 (fn-traced [{:keys [_db]}
             [_ {{{:keys [id]} :submit-order} :data
                 errors :errors}]]
   (if errors
     {:alert (str "FAIL! " (pr-str errors))}
     {:dispatch [:routing/navigate
                 [::routes/rentals-show {:rental-id id}]]})))

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
                (group-by
                 (fn [line]
                   [(get-in line [:model :id])
                    (get-in line [:start-date])
                    (get-in line [:end-date])])))))

(reg-sub ::delegations
         :<- [::current-user/user-data]
         (fn [user-data]
           (let [user {:id (:id user-data) :name (str (:name user-data) (t :delegation/person-postfix))}
                 delegations (:delegations user-data)]
             (concat [user] delegations))))

(reg-sub ::user-id
         :<- [::current-user/user-data]
         :<- [::filters/user-id]
         (fn [[user-data user-id]]
           (or user-id (:id user-data))))

(defn reservation [res-lines invalid-res-ids]
  (let [exemplar (first res-lines)
        model (:model exemplar)
        quantity (count res-lines)
        pool-names (->> res-lines
                        (map (comp :name :inventory-pool))
                        distinct
                        (clojure.string/join ", "))
        invalid? (every? invalid-res-ids (map :id res-lines))
        start-date (js/Date. (:start-date exemplar))
        end-date (js/Date. (:end-date exemplar))
        ;; NOTE: should be in API
        total-days (+ 1 (datefn/differenceInCalendarDays end-date start-date))
        duration (t :line.duration {:totalDays total-days :fromDate start-date})]
    [:div
     [:> UI/Components.Design.ListCard
      {:on-click #(js/alert "TODO")}

      [:> UI/Components.Design.ListCard.Title
       (str quantity "x ")
       (:name model)]

      [:> UI/Components.Design.ListCard.Body
       pool-names]

      [:> UI/Components.Design.ListCard.Foot
       [:> UI/Components.Design.Badge {:class (when invalid? "bg-danger")} duration]]]]))

(defn delegation-select []
  (let [user-id @(subscribe [::user-id])
        delegations @(subscribe [::delegations])]
    [:> UI/Components.Design.Section {:title (t :delegation/section-title) :collapsible true}
     [:label.visually-hidden {:for :user-id} (t :delegation/section-title)]
     [:select {:id :user-id
               :name :user-id
               :class "form-control"
               :default-value user-id
               :on-change (fn [e]
                            (let [v  (-> e .-target .-value)]
                              (dispatch [::filters/set-one :user-id v])
                              (dispatch [::timeout/refresh v])
                              (dispatch [::routes/shopping-cart])))}
      (doall
       (for [user delegations]
         [:option {:value (:id user) :key (:id user)}
          (:name user)]))]]))

(defn countdown []
  (let [now (reagent/atom (js/Date.))]
    (fn []
      (js/setInterval #(reset! now (js/Date.)) 1000)
      (let [data @(subscribe [::data])
            user-id @(subscribe [::filters/user-id])
            valid-until (-> data :valid-until datefn/parseISO)
            total-minutes 30
            remaining-minutes (datefn/differenceInMinutes valid-until @now)]
        [:> UI/Components.Design.Section {:title (t :countdown/section-title) :collapsible true}
         [:> UI/Components.Design.Stack {:space 3}
          [:> UI/Components.Design.ProgressInfo {:title (t :countdown/time-limit)
                                                 :info (t :countdown/time-left {:minutesLeft remaining-minutes})
                                                 :totalCount total-minutes
                                                 :doneCount (- total-minutes remaining-minutes)}]
          [:> UI/Components.Design.ActionButtonGroup
           [:button.btn.btn-secondary {:type "button" :on-click #(dispatch [::timeout/refresh user-id])} (t :countdown/reset)]]]]))))

(defn order-dialog []
  (let [purpose (reagent/atom {:value ""})
        title (reagent/atom {:value ""})
        linked? (reagent/atom true)
        form-validated? (reagent/atom false)]
    (fn [shown? hide!]
      [:> UI/Components.Design.ModalDialog {:shown shown? :title (t :confirm-dialog/dialog-title)}
       [:> UI/Components.Design.ModalDialog.Body
        [:form
         {:on-submit (fn [e]
                       (-> e .preventDefault)
                       (reset! form-validated? true)
                       (when (-> e .-target .checkValidity)
                         (dispatch [::submit-order
                                    {:purpose (:value @purpose)
                                     :title (:value @title)}])))
          :no-validate true
          :auto-complete :off
          :id :the-form
          :class (when @form-validated? "was-validated")}
         [:> UI/Components.Design.Stack {:space "4"}

          [:> UI/Components.Design.Section
           {:title (t :confirm-dialog/title)
            :collapsible true
            :class (when (:was-validated @title) "was-validated")}
           [:label {:htmlFor :title, :class "visually-hidden"} (t :confirm-dialog/title)]
           [:input.form-control
            {:type :text
             :name :title
             :id :title
             :required true
             :value (:value @title)
             :on-change (fn [e]
                          (let [v (-> e .-target .-value)]
                            (swap! title assoc :value v)
                            (when @linked? (swap! purpose assoc :value v))))
             :on-blur #(swap! title assoc :was-validated true)}]]

          [:> UI/Components.Design.Section
           {:title (t :confirm-dialog/purpose)
            :collapsible true
            :class (when (:was-validated @purpose) "was-validated")}
           [:label {:htmlFor :purpose, :class "visually-hidden"} (t :confirm-dialog/purpose)]
           [:> UI/Components.Design.Textarea
            {:maxRows 15
             :minRows 3
             :name :purpose
             :id :purpose
             :class "form-control"
             :required true
             :value (:value @purpose)
             :on-change (fn [e]
                          (swap! purpose assoc :value (-> e .-target .-value))
                          (reset! linked? false))
             :on-blur #(swap! purpose assoc :was-validated true)}]]]]]
       [:> UI/Components.Design.ModalDialog.Footer
        [:button.btn.btn-secondary {:on-click hide!} (t :confirm-dialog/cancel)]
        [:button.btn.btn-primary {:form :the-form :type :submit} (t :confirm-dialog/confirm)]]])))

(defn view []
  (let [order-dialog-shown? (reagent/atom false)]
    (fn []
      (let [data @(subscribe [::data])
            invalid-res-ids (set (:invalid-reservation-ids data))
            errors @(subscribe [::errors])
            reservations @(subscribe [::reservations])
            grouped-reservations @(subscribe [::reservations-grouped])
            is-loading? (not (or data errors))]

        [:> UI/Components.Design.PageLayout
         (cond
           is-loading? [:div.text-5xl.text-center.p-8 [ui/spinner-clock]]

           errors [ui/error-view errors]

           (empty? grouped-reservations)
           [:<>
            [:> UI/Components.Design.PageLayout.Header {:title  (t :order-overview)}]
            [:> UI/Components.Design.Stack {:space 4 :class "text-center"}
             (t :empty-order)
             [:a.text-decoration-underline {:href (routing/path-for ::routes/home)}
              (t :borrow-items)]]]

           :else
           [:<>
            [:> UI/Components.Design.PageLayout.Header {:title  (t :order-overview)}]

            [order-dialog @order-dialog-shown? #(reset! order-dialog-shown? false)]

            [:> UI/Components.Design.Stack {:space 5}

             [countdown]

             [delegation-select]

             [:> UI/Components.Design.Section {:title (t :line/section-title) :collapsible true}
              [:> UI/Components.Design.ListCard.Stack
               (doall
                (for [[grouped-key res-lines] grouped-reservations]
                  [:<> {:key grouped-key}
                   [reservation res-lines invalid-res-ids]]))]]

             [:> UI/Components.Design.Section
              [:> UI/Components.Design.ActionButtonGroup
               (when (> (count invalid-res-ids) 0)
                 [:> UI/Components.Design.Warning
                  (t :line/invalid-items-warning {:invalidItemsCount (count invalid-res-ids)})])
               [:button.btn.btn-primary
                {:type "button"
                 :disabled (not (empty? invalid-res-ids))
                 :on-click #(reset! order-dialog-shown? true)}
                (t :confirm-order)]
               [:button.btn.btn-secondary
                {:type "button"
                 :on-click #(dispatch [::delete-reservations (map :id reservations)])}
                (t :delete-order)]]]]])]))))
