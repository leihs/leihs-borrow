(ns leihs.borrow.features.models.model-filter
  (:require ["/borrow-ui" :as UI]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [leihs.borrow.client.routes :as routes]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.features.models.availability-filter-modal :refer [availability-filter-modal]]
            [leihs.borrow.lib.helpers :as h]
            [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db reg-sub
                                               subscribe]]
            [leihs.borrow.lib.translate :as translate :refer [t with-translate-path]]
            [leihs.borrow.translations :as translations]
            [leihs.core.core :refer [presence remove-blanks]]
            [reagent.core :as r]))

(defn default-dispatch-fn [query-params]
  (dispatch [:routing/navigate
             [::routes/models {:query-params query-params}]]))

(reg-event-db ::clear-options
              (fn-traced [db _]
                (assoc-in db [:ls ::options] nil)))

(reg-sub ::options
         (fn [db _]
           ; NOTE: maybe this list should be somewhere in constants?
           (let [known-filter-keys [:term :pool-id :only-available :start-date :end-date :quantity]]
             (->> db
                  :routing/routing :bidi-match :query-params
                  ((fn [h] (update-vals (select-keys h known-filter-keys) #(or % ""))))))))

(reg-sub ::pools-with-reservable-items
         :<- [::current-user/current-profile]
         (fn [current-profile _]
           (filter #(:has-reservable-items %) (:inventory-pools current-profile))))

(reg-sub ::suspensions
         :<- [::current-user/current-profile]
         (fn [current-profile _]
           (:suspensions current-profile)))

(defn model-search-filter-texts []
  (clj->js (get-in translations/dict [:borrow :filter])))

(defn filter-comp [dispatch-fn]
  (let [availability-modal-shown? (r/atom false)]
    (fn [dispatch-fn]
      (with-translate-path :borrow.filter
        (let [hide! #(reset! availability-modal-shown? false)
              show! #(reset! availability-modal-shown? true)

            ; subscriptions
              text-locale @(subscribe [::translate/text-locale])
              date-locale @(subscribe [::translate/date-locale])
              saved-filters @(subscribe [::options])
              pools-with-reservable-items @(subscribe [::pools-with-reservable-items])
              suspensions @(subscribe [::suspensions])

            ; pools
              selected-pool-id (-> saved-filters :pool-id (or ""))
              is-unselectable-pool (not-any? #{selected-pool-id} (concat [""] (map #(:id %) pools-with-reservable-items)))
              user-suspended-in-pool? (->> suspensions (some #(= selected-pool-id (-> % :inventory-pool :id))))
              available-filters {:pools (concat
                                         [{:id "" :label (t :all-pools-option-label)}]
                                         (when is-unselectable-pool [{:id selected-pool-id :label (t :invalid-pool-option-label)}])
                                         (map (fn [{:keys [id name]}] {:type :pool :id id :label name}) pools-with-reservable-items))}
              selected-pool (first (filter #(= (:id %) selected-pool-id) (:pools available-filters)))

              current-search-term (saved-filters :term)
              current-filters (-> saved-filters
                                  (assoc :selected-pool selected-pool)
                                  (assoc :term current-search-term)
                                  h/camel-case-keys
                                  clj->js)

              on-submit-term #(dispatch-fn (assoc saved-filters :term %))
              on-change-pool #(dispatch-fn (if-let [pool-id (presence %)]
                                             (assoc saved-filters :pool-id pool-id)
                                             (dissoc saved-filters :pool-id)))
              on-apply-availability #(dispatch-fn (remove-blanks (merge saved-filters %)))
              on-clear-filter (fn [filter-to-clear]
                                (dispatch-fn
                                 (case (.-type filter-to-clear)
                                   "term" (dissoc saved-filters :term)
                                   "pool" (dissoc saved-filters :pool-id)
                                   "onlyAvailable" (dissoc saved-filters :only-available))))]

          [:<>

           (when @availability-modal-shown?
             [availability-filter-modal
              hide!
              on-apply-availability
              saved-filters
              date-locale])

           [:> UI/Components.ModelSearchFilter
            {:key (str selected-pool-id (:term saved-filters)) ; force update of controlled inputs
             :availableFilters available-filters
             :currentFilters current-filters
             :onTriggerAvailability show!
             :onClearFilter on-clear-filter
             :onSubmitTerm on-submit-term
             :onChangePool on-change-pool
             :locale text-locale
             :txt (model-search-filter-texts)}]
           (when is-unselectable-pool
             [:> UI/Components.Design.Warning {:class "mt-2"}
              (t :invalid-pool-message)])
           (when user-suspended-in-pool?
             [:> UI/Components.Design.Warning {:class "mt-2"}
              (t :pool-suspended-message)])])))))
