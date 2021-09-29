(ns leihs.borrow.features.filter-modal.core
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
   [leihs.borrow.lib.translate :refer [t set-default-translate-path with-translate-path]]
   [leihs.borrow.lib.helpers :refer [log spy]]
   [leihs.borrow.lib.filters :as filters]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.core.core :refer [remove-nils presence]]
   ["date-fns" :as date-fns]
   ["date-fns/locale" :as locale]
   ["/leihs-ui-client-side-external-react" :as UI]))

(defn filter-modal [shown? cancel-fn]
  (let [term (r/atom @(subscribe [::filters/term]))
        pool-id (r/atom @(subscribe [::filters/pool-id]))
        pools @(subscribe [::current-user/pools])
        only-available (r/atom @(subscribe [::filters/only-available]))
        format-date (fn [x]
                      (some-> x
                              (date-fns/parse "yyyy-MM-dd" (js/Date.))
                              (date-fns/format "dd.MM.yyyy")))
        start-date (r/atom (format-date @(subscribe [::filters/start-date])))
        end-date (r/atom (format-date @(subscribe [::filters/end-date])))
        user-id (r/atom @(subscribe [::filters/user-id]))
        auth-user @(subscribe [::current-user/user-data])
        quantity (r/atom @(subscribe [::filters/quantity]))
        target-users @(subscribe [::current-user/target-users
                                  (t :!borrow.rental-show.user-or-delegation-personal-postfix)])
        valid? (r/atom true)
        start-date-and-end-date-set? #(and (presence @start-date) (presence @end-date))
        start-date-equal-or-before-end-date?
        #(let [s (date-fns/parse @start-date "dd.MM.yyyy" (js/Date.))
               e (date-fns/parse @end-date "dd.MM.yyyy" (js/Date.))]
           (or (date-fns/isEqual s e) (date-fns/isBefore s e)))
        valid? #(or (not @only-available)
                    (and (start-date-and-end-date-set?)
                         (start-date-equal-or-before-end-date?)))]
    (fn [shown? cancel-fn]
      (with-translate-path :borrow.filter
        [:> UI/Components.Design.ModalDialog {:title "Filter" :shown shown?}
         [:> UI/Components.Design.ModalDialog.Body
          [:> UI/Components.Design.Stack {:space 4}
           [:> UI/Components.Design.Section {:title (t :delegations {:n 1})
                                             :collapsible true}
            [:label.visually-hidden {:html-for "user-id"} (t :pools.title)]
            [:select.form-select
             {:name "user-id" :id "user-id" :value @user-id
              :on-change (fn [e] (reset! user-id (-> e .-target .-value)))}
             (doall (for [{:keys [id name]} target-users]
                      [:option {:value id :key id} name]))]]

           [:> UI/Components.Design.Section {:title (t :search.title) :collapsible true}
            [:label.visually-hidden {:html-for "term"} (t :search.title)]
            [:> UI/Components.Design.InputWithClearButton
             {:name "term"
              :id "term"
              :placeholder (t :search.placeholder)
              :value @term
              :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]

           [:> UI/Components.Design.Section {:title (t :pools.title) :collapsible true}
            [:label.visually-hidden {:html-for "pool-id"} (t :pools.title)]
            [:select.form-select {:name "pool-id" :id "pool-id" :value @pool-id
                                  :on-change (fn [e] (reset! pool-id (-> e .-target .-value)))}
             [:option {:value "all" :key "all"} (t :pools.all)]
             (doall (for [{pool-id :id pool-name :name} pools]
                      [:option {:value pool-id :key pool-id} pool-name]))]]

           [:> UI/Components.Design.Section {:title (t :show-only-available) :collapsible true}
            [:label.visually-hidden {:html-for "only-available"} (t :show-only-available)]
            [:input.form-check-input {:type :checkbox :name "only-available" :id "only-available"
                                      :checked @only-available
                                      :on-change (fn [_] (swap! only-available not))}]]

           (when @only-available
             [:> UI/Components.Design.Stack {:space 4}
              [:> UI/Components.Design.Section {:title (t :time-span.title) :collapsible true}
               [:fieldset
                [:legend.visually-hidden (t :time-span.title)]
                [:div.d-flex.flex-column.gap-3
                 [:> UI/Components.Design.DatePicker
                  {:locale locale/de
                   :name "start-date"
                   :id "start-date"
                   :value @start-date
                   :on-change (fn [e] (reset! start-date (-> e .-target .-value)))
                   :placeholder (t :time-span.undefined)
                   :label (r/as-element [:label {:html-for "start-date"} (t :from)])}]
                 [:> UI/Components.Design.DatePicker
                  {:locale locale/de
                   :name "end-date"
                   :id "end-date"
                   :value @end-date
                   :on-change (fn [e] (reset! end-date (-> e .-target .-value)))
                   :placeholder (t :time-span.undefined)
                   :label (r/as-element [:label {:html-for "end-date"} (t :until)])}]]]]

              [:> UI/Components.Design.Section {:title (t :quantity) :collapsible true}
               [:> UI/Components.Design.MinusPlusControl
                {:name "quantity"
                 :id "quantity"
                 :number @quantity
                 :min 1
                 :onChange (fn [n] (reset! quantity n))}]]

              (cond
               (not (start-date-and-end-date-set?))
               [:> UI/Components.Design.Warning "Start and end date must be set."]
               (not (start-date-equal-or-before-end-date?))
               [:> UI/Components.Design.Warning "Start date must be equal to or before end date."])])]]

         [:> UI/Components.Design.ModalDialog.Footer
          [:button.btn.btn-secondary {:type "button" :onClick cancel-fn}
           (t :cancel)]
          [:button.btn.btn-secondary
           {:type "button"
            :onClick #(do (reset! term "")
                          (reset! pool-id "all")
                          (reset! user-id (:id auth-user))
                          (reset! only-available false)
                          (reset! start-date nil)
                          (reset! end-date nil)
                          (reset! quantity 1))}
           (t :reset)]
          [:button.btn.btn-primary
           {:type "button"
            :disabled (not (valid?))
            :onClick #(dispatch [:routing/navigate
                                 [::routes/models
                                  {:query-params
                                   (remove-nils
                                    (letfn [(format-date [x]
                                              (some-> x
                                                      (date-fns/parse "dd.MM.yyyy" (js/Date.))
                                                      (date-fns/format "yyyy-MM-dd")))]
                                      {:term (presence @term)
                                       :pool-id (if (= @pool-id "all") nil @pool-id)
                                       :user-id (when (not= @user-id (:id auth-user)) @user-id)
                                       :only-available @only-available
                                       :start-date (when @only-available (format-date @start-date))
                                       :end-date (when @only-available (format-date @end-date))
                                       :quantity (when @only-available @quantity)}))}]])}
           (t :apply)]]]))))
