(ns leihs.borrow.features.customer-orders.reservation-card
  (:require
   ["/borrow-ui" :as UI]
   ["date-fns" :as date-fns]
   [leihs.borrow.lib.translate :as translate :refer [set-default-translate-path
                                                     t]]
   [leihs.borrow.lib.helpers :as h]
   [reagent.core :as reagent]))

(set-default-translate-path :borrow.rental-show)

(defn status-info [status start-date actual-end-date now]
  (let [days-to-action (some->
                        (cond (= "SIGNED" status) actual-end-date
                              (= "APPROVED" status) start-date)
                        (date-fns/differenceInCalendarDays now))
        is-end-date-past? (date-fns/isAfter (js/Date.) (date-fns/addDays actual-end-date 1))
        expired-unapproved? (and (= status "SUBMITTED") is-end-date-past?)
        expired? (and (= status "APPROVED") is-end-date-past?)
        actionable? (and days-to-action (not expired-unapproved?) (not expired?))
        refined-status (cond expired-unapproved? "EXPIRED-UNAPPROVED" expired? "EXPIRED" :else status)]
    [:div {:class (cond
                    (not actionable?) ""
                    (< days-to-action 0) "text-danger"
                    (<= days-to-action 1) "text-warning"
                    (<= days-to-action 5) "text-primary")}
     (t (str :reservation-status-label "/" refined-status)) " "
     (when actionable?
       (if (< days-to-action 0)
         (t :reservation-line/overdue)
         (t :in-x-days {:days days-to-action})))]))

(defn reservation-card [reservation now href date-locale]
  (let [model (:model reservation)
        option (:option reservation)
        name (:name (or model option))
        quantity (:quantity reservation)
        status (:status reservation)
        start-date (js/Date. (:start-date reservation))
        actual-end-date (js/Date. (:actual-end-date reservation)) ;; equals end_date, or returned_date when present
        total-days (+ 1 (date-fns/differenceInCalendarDays actual-end-date start-date))
        title (t :reservation-line.title {:itemCount quantity, :itemName name})
        inventory-code (-> reservation :item :inventory-code)
        pool-name (-> reservation :inventory-pool :name)
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
        [:div.text-nowrap.d-none.d-md-block
         [status-info status start-date actual-end-date now]]]]

      [:> UI/Components.Design.ListCard.Body
       [:div pool-name]
       [:div
        (h/format-date-range start-date actual-end-date date-locale)
        " (" (t :reservation-line.duration-days {:totalDays total-days}) ")"]]
      [:> UI/Components.Design.ListCard.Foot {:class "fw-bold d-md-none"}
       [status-info status start-date actual-end-date now]]]]))