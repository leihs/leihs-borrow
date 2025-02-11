(ns leihs.borrow.features.customer-orders.reservation-card
  (:require
   ["/borrow-ui" :as UI]
   ["date-fns" :as date-fns]
   [leihs.borrow.lib.translate :as translate :refer [set-default-translate-path
                                                     t]]
   [leihs.borrow.lib.helpers :as h]
   [reagent.core :as reagent]))

(set-default-translate-path :borrow.rental-show)

(defn reservation-card [reservation now href date-locale]
  (let [model (:model reservation)
        option (:option reservation)
        name (:name (or model option))
        quantity (:quantity reservation)
        start-date (js/Date. (:start-date reservation))
        actual-end-date (js/Date. (:actual-end-date reservation)) ;; equals end_date, or returned_date when present
        total-days (+ 1 (date-fns/differenceInCalendarDays actual-end-date start-date))
        title (t :reservation-line.title {:itemCount quantity, :itemName name})
        inventory-code (-> reservation :item :inventory-code)
        pool-name (-> reservation :inventory-pool :name)
        status (:status reservation)
        days-to-action (some->
                        (cond (= "SIGNED" status) actual-end-date
                              (= "APPROVED" status) start-date)
                        (date-fns/differenceInCalendarDays now))
        is-over? (date-fns/isAfter (js/Date.) (date-fns/addDays actual-end-date 1))
        expired-unapproved? (and (= status "SUBMITTED") is-over?)
        expired? (and (= status "APPROVED") is-over?)
        refined-status (cond expired-unapproved? "EXPIRED-UNAPPROVED" expired? "EXPIRED" :else status)
        imgSrc (or (get-in model [:cover-image :image-url])
                   (get-in model [:images 0 :image-url]))]
    [:<>
     [:> UI/Components.Design.ListCard
      {:href href
       :img (reagent/as-element [:> UI/Components.Design.SquareImage {:imgSrc imgSrc :paddingClassName "p-0"}])}
      [:> UI/Components.Design.ListCard.Title
       [:div.d-md-flex.gap-5.justify-content-between
        [:div
         title (when inventory-code [:span " (" inventory-code ")"])
         (when option [:span " (" (t :reservation-line.option) ")"])]
        [:div.text-nowrap {:class (cond
                                    (nil? days-to-action) ""
                                    (< days-to-action 0) "text-danger"
                                    (<= days-to-action 1) "text-warning"
                                    (<= days-to-action 5) "text-primary")}
         (t (str :reservation-status-label "/" refined-status)) " "
         (when days-to-action
           (if (< days-to-action 0) (t :reservation-line/overdue)  (t :in-x-days {:days days-to-action})))]]]

      [:> UI/Components.Design.ListCard.Body
       [:div pool-name]
       [:div
        (h/format-date-range start-date actual-end-date date-locale)
        " (" (t :reservation-line.duration-days {:totalDays total-days}) ")"]]]]))