(ns leihs.borrow.features.customer-orders.status-summary
  (:require
   [reagent.core :as r]
   ["/borrow-ui" :as UI]
   [leihs.borrow.lib.translate :refer [set-default-translate-path t]]))

(set-default-translate-path :borrow.rentals)

(defn status-summary [rental small]
  (let [initial-total-count (-> rental :total-quantity)
        canceled? (-> rental :fulfillment-states (= ["CANCELED"]))

        approved-count (-> rental :approve-fulfillment :fulfilled-quantity)
        rejected-count (-> rental :rejected-quantity)
        expired-unapproved-count (-> rental :expired-unapproved-quantity)

        picked-up-count (-> rental :pickup-fulfillment :fulfilled-quantity)
        expired-count (-> rental :expired-quantity)

        returned-count (-> rental :return-fulfillment :fulfilled-quantity)
        overdue-count (-> rental :overdue-quantity)]

    (if canceled?

      [:> UI/Components.Design.ProgressInfo
       {:small small
        :title (t (str :fulfillment-state-label.CANCELED))}]

      [:div.d-grid.gap-2

       ; Process 1: Approval
       (let [total-count initial-total-count
             done-count (+ approved-count rejected-count expired-unapproved-count)
             all-approved? (= approved-count total-count)
             all-rejected? (= rejected-count total-count)
             all-expired-unapproved? (= expired-unapproved-count total-count)
             title (cond
                     all-rejected? (t :fulfillment-state-label.REJECTED)
                     all-expired-unapproved? (t :fulfillment-state-label.EXPIRED-UNAPPROVED)
                     :else (t :fulfillment-state-label.IN_APPROVAL))
             info (when (not (or all-rejected? all-expired-unapproved?))
                    (str
                     (t :fulfillment-state.summary-line.IN_APPROVAL {:totalCount total-count :doneCount approved-count})
                     (when (> rejected-count 0)
                       (t :fulfillment-state.partial-status.REJECTED {:count rejected-count}))
                     (when (> expired-unapproved-count 0)
                       (t :fulfillment-state.partial-status.EXPIRED {:count expired-unapproved-count}))))]
         (when (not all-approved?)
           [:> UI/Components.Design.ProgressInfo
            (merge {:small small :title title :info info}
                   (when (not= done-count total-count) {:totalCount total-count :doneCount done-count}))]))

       ; Process 2: Pickup
       (let [total-count (- initial-total-count rejected-count expired-unapproved-count)
             done-count (+ picked-up-count expired-count)
             started? (> approved-count 0)
             all-picked? (= picked-up-count total-count)
             all-expired? (= expired-count total-count)
             title (cond
                     all-expired? (t :fulfillment-state-label.EXPIRED)
                     :else (t :fulfillment-state-label.TO_PICKUP))
             info (when (not all-expired?)
                    (str
                     (t :fulfillment-state.summary-line.TO_PICKUP {:totalCount total-count :doneCount picked-up-count})
                     (when (> expired-count 0)
                       (t :fulfillment-state.partial-status.EXPIRED {:count expired-count}))))]
         (when (-> started? (and (not all-picked?)))
           [:> UI/Components.Design.ProgressInfo
            (merge {:small small :title title :info info}
                   (when (not= done-count total-count) {:totalCount total-count :doneCount done-count}))]))

        ; Process 3: Return
       (let [total-count (- initial-total-count rejected-count expired-unapproved-count expired-count)
             done-count returned-count
             started? (> picked-up-count 0)
             all-returned? (= returned-count total-count)
             title (cond
                     (> overdue-count 0) (r/as-element [:div {:class "invalid-feedback-icon text-danger"} (t :fulfillment-state-label.OVERDUE)])
                     all-returned? (t :fulfillment-state-label.RETURNED)
                     :else (t :fulfillment-state-label.TO_RETURN))
             info (when (not all-returned?)
                    (str
                     (t :fulfillment-state.summary-line.TO_RETURN {:totalCount total-count :doneCount returned-count})
                     (when (> overdue-count 0)
                       (t :fulfillment-state.partial-status.OVERDUE {:count overdue-count}))))]
         (when started?
           [:> UI/Components.Design.ProgressInfo
            (merge {:small small :title title :info info}
                   (when (not= done-count total-count) {:totalCount total-count :doneCount done-count}))]))])))
