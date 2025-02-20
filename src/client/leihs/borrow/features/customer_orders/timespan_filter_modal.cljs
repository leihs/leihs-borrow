(ns leihs.borrow.features.customer-orders.timespan-filter-modal
  (:require
   ["/borrow-ui" :as UI]
   ["date-fns" :as date-fns]
   [leihs.borrow.lib.translate :as translate :refer [t with-translate-path]]
   [leihs.core.core :refer [presence]]
   [reagent.core :as r]))

(defn timespan-filter-modal [hide! on-apply saved-filters date-locale]
  (let [start-date (r/atom (or (some-> saved-filters
                                       :from
                                       presence
                                       (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                       (date-fns/format "P" #js {:locale date-locale}))
                               ""))
        end-date (r/atom (or (some-> saved-filters
                                     :until
                                     presence
                                     (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                     (date-fns/format "P" #js {:locale date-locale}))
                             ""))]
    (fn [hide! on-apply saved-filters date-locale]
      (let [start-date-equal-or-before-end-date?
            #(let [s (some-> @start-date presence (date-fns/parse "P" (js/Date.) #js {:locale date-locale}))
                   e (some-> @end-date presence (date-fns/parse "P" (js/Date.) #js {:locale date-locale}))]
               (if (and s e)
                 (or (date-fns/isEqual s e) (date-fns/isBefore s e))
                 true))
            valid? start-date-equal-or-before-end-date?]
        (with-translate-path :borrow.rentals.filter.timespan-modal
          [:> UI/Components.Design.ModalDialog {:title (t :title) :shown true :dismissible true :onDismiss hide!}
           [:> UI/Components.Design.ModalDialog.Body
            [:form {:id "filter-form"
                    :noValidate true
                    :autoComplete "off"
                    :onSubmit
                    #(do (-> % .preventDefault)
                         (hide!)
                         (on-apply
                          {:from (some-> @start-date
                                         presence
                                         (date-fns/parse "P" (js/Date.) #js {:locale date-locale})
                                         (date-fns/format "yyyy-MM-dd"))
                           :until (some-> @end-date
                                          presence
                                          (date-fns/parse "P" (js/Date.) #js {:locale date-locale})
                                          (date-fns/format "yyyy-MM-dd"))}))}
             [:div.d-grid.gap-4
              [:div.d-grid.gap-4
               [:> UI/Components.Design.Section
                [:fieldset
                 [:legend.visually-hidden (t :title)]
                 [:div.d-flex.flex-column.gap-3
                  [:> UI/Components.Design.DatePicker
                   {:locale date-locale
                    :name "start-date"
                    :id "start-date"
                    :value @start-date
                    :on-change (fn [e] (reset! start-date (-> e .-target .-value)))
                    :placeholder (t :undefined)
                    :label (r/as-element ^{:key "key"} [:label {:html-for "start-date"} (t :from)])}]
                  [:> UI/Components.Design.DatePicker
                   {:locale date-locale
                    :name "end-date"
                    :id "end-date"
                    :value @end-date
                    :on-change (fn [e] (reset! end-date (-> e .-target .-value)))
                    :placeholder (t :undefined)
                    :label (r/as-element ^{:key "key"} [:label {:html-for "end-date"} (t :until)])}]
                  (when-not (start-date-equal-or-before-end-date?)
                    [:> UI/Components.Design.Warning
                     (t :errors.start-date-equal-or-before-end-date)])]]]]]]]

           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-primary
             {:type "submit"
              :disabled (not (valid?))
              :form "filter-form"}
             (t :apply)]
            [:button.btn.btn-secondary
             {:type "button"
              :onClick hide!}
             (t :cancel)]]])))))

