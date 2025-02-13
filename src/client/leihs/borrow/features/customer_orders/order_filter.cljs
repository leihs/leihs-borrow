(ns leihs.borrow.features.customer-orders.order-filter
  (:require
   ["/borrow-ui" :as UI]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.customer-orders.timespan-filter-modal :refer [timespan-filter-modal]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [reg-sub subscribe]]
   [leihs.borrow.lib.translate :as translate :refer [t with-translate-path]]
   [leihs.borrow.translations :as translations]
   [leihs.core.core :refer [presence remove-blanks]]
   [reagent.core :as r]))

(reg-sub ::inventory-pools
         :<- [::current-user/current-profile]
         (fn [current-profile _]
           (:inventory-pools current-profile)))
(defn filter-comp [filters dispatch-fn]
  (let [timespan-modal-shown? (r/atom false)]
    (fn [filters dispatch-fn]
      (with-translate-path :borrow.rentals.filter
        (let [hide! #(reset! timespan-modal-shown? false)
              show! #(reset! timespan-modal-shown? true)

              ;; subscriptions
              text-locale @(subscribe [::translate/text-locale])
              date-locale @(subscribe [::translate/date-locale])
              inventory-pools @(subscribe [::inventory-pools])

              ;; derived from subscriptions
              current-search-term (filters :term)
              selected-pool-id (-> filters :pool-id (or ""))
              is-unselectable-pool (not-any? #{selected-pool-id} (concat [""] (map #(:id %) inventory-pools)))
              available-filters {:pools (concat
                                         [{:id "" :label (t :pools.all)}]
                                         (when is-unselectable-pool [{:id selected-pool-id :label (t :pools.invalid-option)}])
                                         (map (fn [{:keys [id name]}] {:type :pool :id id :label name}) inventory-pools))}
              selected-pool (first (filter #(= (:id %) selected-pool-id) (:pools available-filters)))
              current-filters (-> filters
                                  (assoc :selected-pool selected-pool)
                                  (assoc :term current-search-term)
                                  h/camel-case-keys
                                  clj->js)

              ;; dispatchers
              apply-filter (fn [filters]
                             (dispatch-fn ;; force a reload even when query args where not changed, by toggling the "seq" arg
                              (update filters :seq #(-> % (or 0) js/parseInt (bit-xor 1)))))
              on-submit-term #(apply-filter (assoc filters :term %))
              on-change-pool #(apply-filter (if-let [pool-id (presence %)]
                                              (assoc filters :pool-id pool-id)
                                              (dissoc filters :pool-id)))
              on-apply-timespan #(apply-filter (remove-blanks (merge filters %)))
              on-clear-filter (fn [filter-to-clear]
                                (apply-filter
                                 (case (.-type filter-to-clear)
                                   "term" (dissoc filters :term)
                                   "pool" (dissoc filters :pool-id)
                                   "timespan" (-> filters (dissoc :from) (dissoc :until)))))]

          [:<>
           (when @timespan-modal-shown?
             [timespan-filter-modal
              hide!
              on-apply-timespan
              filters
              date-locale])

           [:> UI/Components.OrderSearchFilter
            {:key (str selected-pool-id (:term filters)) ; force update of controlled inputs
             :availableFilters available-filters
             :currentFilters current-filters
             :onTriggerTimespan show!
             :onClearFilter on-clear-filter
             :onSubmitTerm on-submit-term
             :onChangePool on-change-pool
             :locale text-locale
             :txt (clj->js (get-in translations/dict [:borrow :rentals :filter :js-component]))}]
           (when is-unselectable-pool
             [:> UI/Components.Design.Warning {:class "mt-2"}
              (t :pools.invalid-option-info)])])))))
