(ns leihs.borrow.features.customer-orders.filter-modal
  (:require
   [clojure.string :as string]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.translate :as translate
    :refer [t set-default-translate-path with-translate-path]]
   [leihs.borrow.lib.helpers :refer [log spy]]
   [leihs.borrow.lib.form-helpers :refer [UiInputWithClearButton]]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.core.core :refer [remove-nils presence update-vals]]
   ["date-fns" :as date-fns]
   ["date-fns/locale" :as locale]
   ["/leihs-ui-client-side-external-react" :as UI]))

(reg-event-db ::save-filter-options
              (fn-traced [db [_ query-params]]
                (assoc-in db [:ls ::options] query-params)))

(reg-sub ::options
         (fn [db _] (->> db :ls ::options (update-vals #(or % "")))))

(defn filter-modal
  [filter-opts hide! dispatch-fn locale auth-user-id]
  (let [user-id (r/atom (or (:user-id filter-opts) auth-user-id))
        term (r/atom (or (:term filter-opts) ""))
        pool-id (r/atom (or (:pool-id filter-opts) "all"))
        state (r/atom (or (:state filter-opts) "all"))
        start-date (r/atom (or (some-> filter-opts
                                       :from
                                       presence
                                       (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                       (date-fns/format "P" #js {:locale locale}))
                               ""))
        end-date (r/atom (or (some-> filter-opts
                                     :until
                                     presence
                                     (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                     (date-fns/format "P" #js {:locale locale}))
                             ""))]
    (fn [filter-opts hide! dispatch-fn locale auth-user-id]
      (let [pools @(subscribe [::current-user/pools])
            auth-user @(subscribe [::current-user/user-data])
            target-users @(subscribe [::current-user/target-users
                                      (t :!borrow.rental-show.user-or-delegation-personal-postfix)])
            start-date-equal-or-before-end-date?
            #(let [s (some-> @start-date presence (date-fns/parse "P" (js/Date.) #js {:locale locale}))
                   e (some-> @end-date presence (date-fns/parse "P" (js/Date.) #js {:locale locale}))]
               (if (and s e)
                 (or (date-fns/isEqual s e) (date-fns/isBefore s e))
                 true))
            valid? start-date-equal-or-before-end-date?]
        (with-translate-path :borrow.rentals.filter
          [:> UI/Components.Design.ModalDialog {:title "Filter" :shown true}
           [:> UI/Components.Design.ModalDialog.Body
            [:> UI/Components.Design.Stack {:space 4}
             [:> UI/Components.Design.Section {:title (t :delegations {:n 1})
                                               :collapsible true}
              [:label.visually-hidden {:html-for "user-id"} (t :delegations {:n 1})]
              [:select.form-select
               {:name "user-id" :id "user-id" :value (or (presence @user-id)
                                                         (-> target-users first :id))
                :on-change (fn [e] (reset! user-id (-> e .-target .-value)))}
               (doall (for [{:keys [id name]} target-users]
                        [:option {:value id :key id} name]))]]

             [:> UI/Components.Design.Section {:title (t :search.title) :collapsible true}
              [:label.visually-hidden {:html-for "term"} (t :search.title)]
              ; --------------------------------------------
              ; There is some bug when one clicks on X icon.
              ; --------------------------------------------
              ; [UiInputWithClearButton {:name "term"
              ;                          :id "term"
              ;                          :placeholder (t :search.placeholder)
              ;                          :value @term
              ;                          :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]
              [:input.form-control {:name "term"
                                    :id "term"
                                    :placeholder (t :search.placeholder)
                                    :value @term
                                    :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]

             [:> UI/Components.Design.Section {:title (t :states.title) :collapsible true}
              [:label.visually-hidden {:html-for "state"} (t :states.title)]
              [:select.form-select {:name "state" :id "state" :value @state
                                    :on-change (fn [e] (reset! state (-> e .-target .-value)))}
               [:option {:value "all" :key "all"} (t :states.all)]
               (doall (for [state-name ["IN_APPROVAL"
                                        "REJECTED"
                                        "CANCELED"
                                        "EXPIRED"
                                        "TO_PICKUP"
                                        "TO_RETURN"
                                        "OVERDUE"
                                        "RETURNED"]]
                        [:option {:value state-name :key state-name} state-name]))]]

             [:> UI/Components.Design.Stack {:space 4}
              [:> UI/Components.Design.Section {:title (t :time-span.title) :collapsible true}
               [:fieldset
                [:legend.visually-hidden (t :time-span.title)]
                [:div.d-flex.flex-column.gap-3
                 [:> UI/Components.Design.DatePicker
                  {:locale locale
                   :name "start-date"
                   :id "start-date"
                   :value @start-date
                   :on-change (fn [e] (reset! start-date (-> e .-target .-value)))
                   :placeholder (t :time-span.undefined)
                   :label (r/as-element [:label {:html-for "start-date"} (t :from)])}]
                 [:> UI/Components.Design.DatePicker
                  {:locale locale
                   :name "end-date"
                   :id "end-date"
                   :value @end-date
                   :on-change (fn [e] (reset! end-date (-> e .-target .-value)))
                   :placeholder (t :time-span.undefined)
                   :label (r/as-element [:label {:html-for "end-date"} (t :until)])}]]]]

              (when-not (start-date-equal-or-before-end-date?)
                [:> UI/Components.Design.Warning
                 (t :time-span.errors.start-date-equal-or-before-end-date)])]
             
             [:> UI/Components.Design.Section {:title (t :pools.title) :collapsible true}
              [:label.visually-hidden {:html-for "pool-id"} (t :pools.title)]
              [:select.form-select {:name "pool-id" :id "pool-id" :value @pool-id
                                    :on-change (fn [e] (reset! pool-id (-> e .-target .-value)))}
               [:option {:value "all" :key "all"} (t :pools.all)]
               (doall (for [{pool-id :id pool-name :name} pools]
                        [:option {:value pool-id :key pool-id} pool-name]))]]]]

           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-secondary
             {:type "button"
              :onClick #(do (reset! term "")
                            (reset! pool-id "all")
                            (reset! user-id (:id auth-user))
                            (reset! start-date "")
                            (reset! end-date "")
                            (reset! state "all"))}
             (t :reset)]
            [:button.btn.btn-primary
             {:type "button"
              :disabled (not (valid?))
              :onClick
              #(do (hide!)
                   (dispatch-fn
                    (remove-nils
                     {:term (presence @term)
                      :pool-id (if (= @pool-id "all") nil @pool-id)
                      :state (if (= @state "all") nil @state)
                      :user-id (presence @user-id)
                      :from (some-> @start-date
                                    presence
                                    (date-fns/parse "P" (js/Date.) #js {:locale locale})
                                    (date-fns/format "yyyy-MM-dd"))
                      :until (some-> @end-date
                                     presence
                                     (date-fns/parse "P" (js/Date.) #js {:locale locale})
                                     (date-fns/format "yyyy-MM-dd"))})))}
             (t :apply)]]])))))

(defn filter-comp [dispatch-fn]
  (let [modal-shown? (r/atom false)]
    (fn [dispatch-fn]
      (let [hide! #(reset! modal-shown? false)
            show! #(reset! modal-shown? true)
            locale @(subscribe [::translate/i18n-locale])
            filter-opts @(subscribe [::options])
            auth-user @(subscribe [::current-user/user-data])]
        [:<>
         (when @modal-shown?
           [filter-modal filter-opts hide! dispatch-fn locale (:id auth-user)])
         [:> UI/Components.Design.FilterButton {:onClick show!}
          (t :!borrow.home-page.show-search-and-filter)]]))))