(ns leihs.borrow.features.home-page.core
  (:require
   [clojure.string :as string]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
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
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.features.models.core :as models]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.categories.core :as categories]
   [leihs.core.core :refer [remove-nils]]
    ["date-fns" :as date-fns]
   ["/leihs-ui-client-side-external-react" :as UI]
   ["react" :as React]))

(set-default-translate-path :borrow.home-page)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/home
 (fn-traced [_ [_ {:keys [query-params]}]]
            {:dispatch-n (list [::filters/init]
                               [::filters/set-multiple query-params]
                               [::categories/fetch-index 4])}))

(defn filter-modal [shown? cancel-fn]
  (let [term (r/atom @(subscribe [::filters/term]))
        pool-id (r/atom @(subscribe [::filters/pool-id]))
        pools @(subscribe [::current-user/pools])
        available-between (r/atom @(subscribe [::filters/available-between?]))
        start-date (r/atom @(subscribe [::filters/start-date]))
        end-date (r/atom @(subscribe [::filters/end-date]))
        user-id (r/atom @(subscribe [::filters/user-id]))
        target-users @(subscribe [::current-user/target-users
                                  (t :!borrow.rental-show.user-or-delegation-personal-postfix)])]
    (fn [shown? cancel-fn]
      (with-translate-path :borrow.filter
        [:> UI/Components.Design.ModalDialog {:title "Filter" :shown shown?}
         [:> UI/Components.Design.ModalDialog.Body
          [:> UI/Components.Design.Stack {:space 4}
           [:> UI/Components.Design.Section {:title (t :delegations {:n 1})
                                             :collapsible true}
            [:label.visually-hidden {:html-for "delegation"} (t :pools.title)]
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
            [:label.visually-hidden {:html-for "pool"} (t :pools.title)]
            [:select.form-select {:name "pool-id" :id "pool-id" :value @pool-id
                                  :on-change (fn [e] (reset! pool-id (-> e .-target .-value)))}
             [:option {:value "all" :key "all"} (t :pools.all)]
             (doall (for [{pool-id :id pool-name :name} pools]
                      [:option {:value pool-id :key pool-id} pool-name]))]]

           [:> UI/Components.Design.Section {:title (t :show-only-available) :collapsible true}
            [:label.visually-hidden {:html-for "available-between"} (t :show-only-available)]
            [:input.form-check-input {:type :checkbox :name "available-between" :id "available-between"
                                      :value @available-between
                                      :on-change (fn [_] (swap! available-between not))}]]

           (when @available-between
             [:> UI/Components.Design.Section {:title (t :time-span.title) :collapsible true}
              [:fieldset
               [:legend.visually-hidden (t :time-span.title)]
               [:div.d-flex.flex-column.gap-3
                [:> UI/Components.Design.DatePicker
                 {:name "start-date"
                  :id "start-date"
                  :value @start-date
                  :on-change (fn [e] (reset! start-date (-> e .-target .-value)))
                  :placeholder (t :time-span.undefined)
                  :label (r/as-element [:label {:html-for "start-date"} (t :from)])}]
                [:> UI/Components.Design.DatePicker
                 {:name "end-date"
                  :id "end-date"
                  :value @end-date
                  :on-change (fn [e] (reset! end-date (-> e .-target .-value)))
                  :placeholder (t :time-span.undefined)
                  :label (r/as-element [:label {:html-for "end-date"} (t :until)])}]]]])]]

         [:> UI/Components.Design.ModalDialog.Footer
          [:button.btn.btn-secondary {:type "button" :onClick cancel-fn}
           (t :cancel)]
          [:button.btn.btn-secondary
           {:type "button"
            :onClick #(js/alert "reset")}
           (t :reset)]
          [:button.btn.btn-primary
           {:type "button"
            :onClick #(dispatch [:routing/navigate
                                 [::routes/models
                                  {:query-params (remove-nils {:term @term
                                                               :pool-id @pool-id
                                                               :user-id @user-id
                                                               :only-available @available-between
                                                               :start-date @start-date
                                                               :end-date @end-date})}]])}
           (t :apply)]]]))))

(defn view []
  (let [modal-shown? (r/atom false)]
    (fn []
      (let [cats @(subscribe [::categories/categories-index])
            cats-url (routing/path-for ::routes/categories-index)
            ;; favs @(subscribe [::categories/categories-index])
            filters @(subscribe [::filters/current])
            delegations true]
        [:> UI/Components.AppLayout.Page
         [:> UI/Components.Design.PageLayout.Header {:title (t :catalog)}
          [filter-modal @modal-shown? #(reset! modal-shown? false)]
          [:> UI/Components.Design.FilterButton {:onClick #(reset! modal-shown? true)}
           (t :show-search-and-filter)]]
         [:> UI/Components.Design.Stack
          [:> UI/Components.Design.Section {:title (t :!borrow.categories.title)}
           (categories/categories-list cats)]]]))))
