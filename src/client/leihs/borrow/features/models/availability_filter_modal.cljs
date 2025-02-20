(ns leihs.borrow.features.models.availability-filter-modal
  (:require ["/borrow-ui" :as UI]
            ["date-fns" :as date-fns]
            [leihs.borrow.lib.form-helpers :refer [UiDateRangePicker]]
            [leihs.borrow.lib.translate :as translate :refer [t
                                                              with-translate-path]]
            [leihs.core.core :refer [presence]]
            [reagent.core :as r]))

(defn availability-filter-modal [hide! on-apply saved-filters date-locale]
  (let [parse-date #(some-> % (date-fns/parse "yyyy-MM-dd" (js/Date.)))
        format-date #(some-> % (date-fns/format "yyyy-MM-dd"))
        selected-range (r/atom {:startDate (or
                                            (some-> saved-filters :start-date parse-date)
                                            (date-fns/startOfToday))
                                :endDate (or
                                          (some-> saved-filters :end-date parse-date)
                                          (date-fns/startOfTomorrow))})
        quantity (r/atom (or (:quantity saved-filters) 1))]
    (fn [hide! on-apply saved-filters date-locale]
      (let [today (date-fns/startOfToday)
            max-date (date-fns/addYears today 10)
            change-selected-range (fn [r]
                                    (let [start-date (-> r .-startDate)
                                          end-date (-> r .-endDate)]
                                      (reset! selected-range {:startDate start-date :endDate end-date})))
            start-date-and-end-date-set? #(and (presence (:startDate @selected-range)) (presence (:endDate @selected-range)))
            start-date-equal-or-before-end-date?
            #(let [s (:startDate @selected-range)
                   e (:endDate @selected-range)]
               (or (date-fns/isEqual s e) (date-fns/isBefore s e)))
            valid? #(and (start-date-and-end-date-set?)
                         (start-date-equal-or-before-end-date?))]
        (with-translate-path :borrow.filter.availability-modal
          [:> UI/Components.Design.ModalDialog {:title (t :title) :shown true :dismissible true :onDismiss hide!}
           [:> UI/Components.Design.ModalDialog.Body
            [:form {:id "filter-form"
                    :noValidate true
                    :autoComplete "off"
                    :onSubmit
                    #(do (-> % .preventDefault)
                         (hide!)
                         (on-apply
                          {:only-available true
                           :start-date (format-date (:startDate @selected-range))
                           :end-date (format-date (:endDate @selected-range))
                           :quantity @quantity}))}
             [:div.d-grid.gap-4

              [:> UI/Components.Design.Section

               [:fieldset
                [:legend.visually-hidden (t :timespan.title)]
                [:div.d-flex.flex-column.gap-3
                 [UiDateRangePicker
                  {:locale date-locale
                   :txt {:from (t :from)
                         :until (t :until)
                         :placeholderFrom (t :timespan.undefined)
                         :placeholderUntil (t :timespan.undefined)}
                   :selected-range @selected-range
                   :onChange change-selected-range
                   :min-date today
                   :max-date max-date}]
                 (cond
                   (not (start-date-and-end-date-set?))
                   [:> UI/Components.Design.Warning (t :timespan.errors.start-date-and-end-date-set)]
                   (not (start-date-equal-or-before-end-date?))
                   [:> UI/Components.Design.Warning  (t :timespan.errors.start-date-equal-or-before-end-date)])]]]

              [:> UI/Components.Design.Section {:title (t :quantity)}
               [:label.visually-hidden {:html-for "quantity"} (t :quantity)]
               [:> UI/Components.Design.MinusPlusControl
                {:name "quantity"
                 :id "quantity"
                 :value @quantity
                 :min 1
                 :onChange (fn [n] (reset! quantity n))}]]]]]

           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-primary {:type "submit"
                                      :disabled (not (valid?))
                                      :form "filter-form"}
             (t :apply)]
            [:button.btn.btn-secondary
             {:type "button"
              :onClick hide!}
             (t :cancel)]]])))))
