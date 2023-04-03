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
   [leihs.core.core :refer [remove-nils presence]]
   ["date-fns" :as date-fns]
   ["/leihs-ui-client-side-external-react" :as UI]))

(reg-event-db ::save-filter-options
              (fn-traced [db [_ query-params]]
                (assoc-in db [:ls ::options] query-params)))

(reg-sub ::options
         (fn [db _] (update-vals (->> db :ls ::options) #(or % ""))))

(reg-sub ::inventory-pools
         :<- [::current-user/current-profile]
         (fn [current-profile _]
           (:inventory-pools current-profile)))

(defn filter-modal
  [filter-opts hide! dispatch-fn date-locale]
  (let [term (r/atom (or (:term filter-opts) ""))
        pool-id (r/atom (or (:pool-id filter-opts) "all"))
        state (r/atom (or (:state filter-opts) "all"))
        start-date (r/atom (or (some-> filter-opts
                                       :from
                                       presence
                                       (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                       (date-fns/format "P" #js {:locale date-locale}))
                               ""))
        end-date (r/atom (or (some-> filter-opts
                                     :until
                                     presence
                                     (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                     (date-fns/format "P" #js {:locale date-locale}))
                             ""))
        seq (-> filter-opts :seq (or 0) js/parseInt (bit-xor 1)) ; to force a reload even when query args where not changed
        ]
    (fn [filter-opts hide! dispatch-fn date-locale]
      (let [pools @(subscribe [::inventory-pools])
            is-unselectable-pool (not-any? #{@pool-id} (concat ["" "all"] (map #(:id %) pools)))
            start-date-equal-or-before-end-date?
            #(let [s (some-> @start-date presence (date-fns/parse "P" (js/Date.) #js {:locale date-locale}))
                   e (some-> @end-date presence (date-fns/parse "P" (js/Date.) #js {:locale date-locale}))]
               (if (and s e)
                 (or (date-fns/isEqual s e) (date-fns/isBefore s e))
                 true))
            valid? start-date-equal-or-before-end-date?]
        (with-translate-path :borrow.rentals.filter
          [:> UI/Components.Design.ModalDialog {:title "Filter" :shown true :dismissible true :onDismiss hide!}
           [:> UI/Components.Design.ModalDialog.Body
            [:form {:id "filter-form"
                    :noValidate true
                    :autoComplete "off"
                    :onSubmit #(do (-> % .preventDefault)
                                   (hide!)
                                   (dispatch-fn
                                    (remove-nils
                                     {:term (presence @term)
                                      :pool-id (if (= @pool-id "all") nil @pool-id)
                                      :state (if (= @state "all") nil @state)
                                      :from (some-> @start-date
                                                    presence
                                                    (date-fns/parse "P" (js/Date.) #js {:locale date-locale})
                                                    (date-fns/format "yyyy-MM-dd"))
                                      :until (some-> @end-date
                                                     presence
                                                     (date-fns/parse "P" (js/Date.) #js {:locale date-locale})
                                                     (date-fns/format "yyyy-MM-dd"))
                                      :seq seq})))}
             [:> UI/Components.Design.Stack {:space 4}

              [:> UI/Components.Design.Section {:title (t :search.title)}
               [:label.visually-hidden {:html-for "term"} (t :search.title)]
               [UiInputWithClearButton {:name "term"
                                        :id "term"
                                        :placeholder (t :search.placeholder)
                                        :value @term
                                        :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]

              [:> UI/Components.Design.Section {:title (t :states.title)}
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
                         [:option {:value state-name :key state-name} (t (str :states.state-filter-label "/" state-name))]))]]

              [:> UI/Components.Design.Stack {:space 4}
               [:> UI/Components.Design.Section {:title (t :time-span.title)}
                [:fieldset
                 [:legend.visually-hidden (t :time-span.title)]
                 [:div.d-flex.flex-column.gap-3
                  [:> UI/Components.Design.DatePicker
                   {:locale date-locale
                    :name "start-date"
                    :id "start-date"
                    :value @start-date
                    :on-change (fn [e] (reset! start-date (-> e .-target .-value)))
                    :placeholder (t :time-span.undefined)
                    :label (r/as-element [:label {:html-for "start-date"} (t :from)])}]
                  [:> UI/Components.Design.DatePicker
                   {:locale date-locale
                    :name "end-date"
                    :id "end-date"
                    :value @end-date
                    :on-change (fn [e] (reset! end-date (-> e .-target .-value)))
                    :placeholder (t :time-span.undefined)
                    :label (r/as-element [:label {:html-for "end-date"} (t :until)])}]
                  (when-not (start-date-equal-or-before-end-date?)
                    [:> UI/Components.Design.Warning
                     (t :time-span.errors.start-date-equal-or-before-end-date)])]]]]

              [:> UI/Components.Design.Section {:title (t :pools.title)}
               [:label.visually-hidden {:html-for "pool-id"} (t :pools.title)]
               [:select.form-select {:name "pool-id" :id "pool-id" :value @pool-id
                                     :on-change (fn [e] (reset! pool-id (-> e .-target .-value)))}
                [:option {:value "all"} (t :pools.all)]
                (when is-unselectable-pool
                  [:option {:value @pool-id} (t :pools.invalid-option)])
                (doall (for [{pool-id :id pool-name :name} pools]
                         [:option {:value pool-id :key pool-id} pool-name]))]
               (when is-unselectable-pool
                 [:> UI/Components.Design.Warning {:class "mt-2"}
                  (t :pools.invalid-option-info)])]]]]

           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-primary
             {:type "submit"
              :disabled (not (valid?))
              :form "filter-form"}
             (t :apply)]
            [:button.btn.btn-secondary
             {:type "button"
              :onClick #(do (reset! term "")
                            (reset! pool-id "all")
                            (reset! start-date "")
                            (reset! end-date "")
                            (reset! state "all"))}
             (t :reset)]]])))))

(defn filter-comp [dispatch-fn]
  (let [modal-shown? (r/atom false)]
    (fn [dispatch-fn]
      (let [hide! #(reset! modal-shown? false)
            show! #(reset! modal-shown? true)
            date-locale @(subscribe [::translate/date-locale])
            filter-opts @(subscribe [::options])]
        [:<>
         (when @modal-shown?
           [filter-modal filter-opts hide! dispatch-fn date-locale])
         [:> UI/Components.Design.FilterButton {:onClick show!}
          (t :!borrow.rentals.filter.show-filters)]]))))
