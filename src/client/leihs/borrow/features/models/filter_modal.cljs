(ns leihs.borrow.features.models.filter-modal
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
    :refer [t dict set-default-translate-path with-translate-path]]
   [leihs.borrow.lib.helpers :as h :refer [log spy]]
   [leihs.borrow.lib.form-helpers :refer [UiInputWithClearButton UiDateRangePicker]]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.core.core :refer [remove-blanks presence update-vals]]
   ["date-fns" :as date-fns]
   ["date-fns/locale" :as locale]
   ["/leihs-ui-client-side-external-react" :as UI]))

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
                  ((fn [h] (select-keys h known-filter-keys)))
                  (update-vals #(or % ""))))))

(reg-sub ::pools-with-reservable-items
         :<- [::current-user/current-profile]
         (fn [current-profile _]
           (filter #(:has-reservable-items %) (:inventory-pools current-profile))))

(reg-sub ::suspensions
         :<- [::current-user/current-profile]
         (fn [current-profile _]
           (:suspensions current-profile)))

(reg-sub ::user-locale
         :<- [::current-user/locale]
         (fn [l _] l))

(defn model-search-filter-texts []
  (clj->js (get-in dict [:borrow :filter])))

(defn filter-modal [hide! dispatch-fn saved-opts locale]
  (let [parse-date #(some-> % (date-fns/parse "yyyy-MM-dd" (js/Date.)))
        term (r/atom (or (:term saved-opts) ""))
        pool-id (r/atom (or (:pool-id saved-opts) ""))
        only-available (r/atom (let [oa (:only-available saved-opts)]
                                 (if (nil? oa) false true)))
        selected-range (r/atom {:startDate (or
                                            (some-> saved-opts :start-date parse-date)
                                            (date-fns/startOfToday))
                                :endDate (or
                                          (some-> saved-opts :end-date parse-date)
                                          (date-fns/startOfTomorrow))})
        quantity (r/atom (or (:quantity saved-opts) 1))]
    (fn [hide! dispatch-fn saved-opts]
      (let [today (date-fns/startOfToday)
            max-date (date-fns/addYears today 10)
            pools @(subscribe [::pools-with-reservable-items])
            is-unselectable-pool (not-any? #{@pool-id} (concat ["" "all"] (map #(:id %) pools)))
            suspensions @(subscribe [::suspensions])
            user-suspended-in-pool? (->> suspensions (some #(= @pool-id (-> % :inventory-pool :id))))
            locale-to-use @(subscribe [::translate/locale-to-use])
            locale (case locale-to-use :de-CH locale/de :gsw-CH locale/de :en-GB locale/enGB #_fallback-> locale/enGB)

            change-selected-range (fn [r]
                                    (let [start-date (-> r .-startDate)
                                          end-date (-> r .-endDate)]
                                      (reset! selected-range {:startDate start-date :endDate end-date})))
            start-date-and-end-date-set? #(and (presence (:startDate @selected-range)) (presence (:endDate @selected-range)))
            start-date-equal-or-before-end-date?
            #(let [s (:startDate @selected-range)
                   e (:endDate @selected-range)]
               (or (date-fns/isEqual s e) (date-fns/isBefore s e)))
            valid? #(or (not @only-available)
                        (and (start-date-and-end-date-set?)
                             (start-date-equal-or-before-end-date?)))]
        (with-translate-path :borrow.filter
          [:> UI/Components.Design.ModalDialog {:title "Filter" :shown true :dismissible true :onDismiss hide!}
           [:> UI/Components.Design.ModalDialog.Body
            [:form {:id "filter-form"
                    :noValidate true
                    :autoComplete "off"
                    :onSubmit
                    #(do (-> % .preventDefault)
                         (hide!)
                         (dispatch-fn
                          (remove-blanks
                           (letfn [(format-date [x]
                                     (some-> x
                                             (date-fns/format "yyyy-MM-dd")))]
                             {:term @term
                              :pool-id (if (= @pool-id "all") nil @pool-id)
                              :only-available (when @only-available @only-available)
                              :start-date (when @only-available (format-date (:startDate @selected-range)))
                              :end-date (when @only-available (format-date (:endDate @selected-range)))
                              :quantity (when @only-available @quantity)}))))}
             [:> UI/Components.Design.Stack {:space 4}

              [:> UI/Components.Design.Section {:title (t :search.title)}
               [:label.visually-hidden {:html-for "term"} (t :search.title)]
               [UiInputWithClearButton {:name "term"
                                        :id "term"
                                        :placeholder (t :search.placeholder)
                                        :value @term
                                        :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]

              [:> UI/Components.Design.Section {:title (t :availability)}
               [:div.form-check.mb-3
                [:input.form-check-input {:type :checkbox :name "only-available" :id "only-available"
                                          :checked @only-available
                                          :on-change (fn [_] (swap! only-available not))}]
                [:label.form-check-label {:html-for "only-available"} (t :show-only-available)]]
               (when @only-available
                 [:fieldset
                  [:legend.visually-hidden (t :time-span.title)]
                  [:div.d-flex.flex-column.gap-3
                   [UiDateRangePicker
                    {:locale locale
                     :txt {:from (t :from)
                           :until (t :until)
                           :placeholderFrom (t :time-span.undefined)
                           :placeholderUntil (t :time-span.undefined)}
                     :selected-range @selected-range
                     :onChange change-selected-range
                     :min-date today
                     :max-date max-date}]
                   (cond
                     (not (start-date-and-end-date-set?))
                     [:> UI/Components.Design.Warning (t :time-span.errors.start-date-and-end-date-set)]
                     (not (start-date-equal-or-before-end-date?))
                     [:> UI/Components.Design.Warning  (t :time-span.errors.start-date-equal-or-before-end-date)])]])]

              (when @only-available
                [:> UI/Components.Design.Section {:title (t :quantity)}
                 [:label.visually-hidden {:html-for "quantity"} (t :quantity)]
                 [:> UI/Components.Design.MinusPlusControl
                  {:name "quantity"
                   :id "quantity"
                   :value @quantity
                   :min 1
                   :onChange (fn [n] (reset! quantity n))}]])

              [:> UI/Components.Design.Section {:title (t :pools.title)}
               [:label.visually-hidden {:html-for "pool-id"} (t :pools.title)]
               [:select.form-select {:name "pool-id" :id "pool-id" :value (or @pool-id "all")
                                     :on-change (fn [e] (reset! pool-id (-> e .-target .-value)))}
                [:option {:value "all"} (t :pools.all)]
                (when is-unselectable-pool
                  [:option {:value @pool-id} (t :pools.invalid-option)])
                (doall (for [{pool-id :id pool-name :name} pools]
                         [:option {:value pool-id :key pool-id} pool-name]))]
               (when is-unselectable-pool
                 [:> UI/Components.Design.Warning {:class "mt-2"}
                  (t :pools.invalid-option-info)])
               (when user-suspended-in-pool?
                 [:> UI/Components.Design.Warning {:class "mt-2"}
                  (t :pools.pool-suspension)])]]]]

           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-primary {:type "submit"
                                      :disabled (not (valid?))
                                      :form "filter-form"}
             (t :apply)]
            [:button.btn.btn-secondary
             {:type "button"
              :onClick #(do (reset! term "")
                            (reset! pool-id "all")
                            (reset! only-available false)
                            (reset! selected-range {:startDate (date-fns/startOfToday)
                                                    :endDate (date-fns/startOfTomorrow)})
                            (reset! quantity 1))}
             (t :reset)]]])))))

(defn filter-comp [dispatch-fn]
  (let [modal-shown? (r/atom false)
        entered-search-term (r/atom false)]
    (fn [dispatch-fn]
      (let [set-entered-search-term #(reset! entered-search-term %)
            hide! #(reset! modal-shown? false)
            show! #(reset! modal-shown? true)
            saved-filters @(subscribe [::options])
            pools-with-reservable-items @(subscribe [::pools-with-reservable-items])
            user-locale @(subscribe [::user-locale])
            ; NOTE: map unsupported swiss-german to german for consistency
            locale (case user-locale "gsw-CH" "de-CH" #_default-> user-locale)

            ;
            available-filters {:pools (map (fn [p] {:type :pool :id (:id p) :label (:name p)}) pools-with-reservable-items)}
            ; NOTE: sync state between main input and the one inside the panel!
            current-search-term (or (presence @entered-search-term) (saved-filters :term))
            current-filters (let [; NOTE: do not give just id but the same object like in `availableFilters`
                                  pool (first (filter #(= (:id %) (saved-filters :pool-id)) (:pools available-filters)))]
                              (-> saved-filters
                                  ; NOTE: UI supports multiple selection of pools, fake it here until app supports it as well
                                  (dissoc :pool-id) (assoc :pool-ids (when pool (vector pool)))
                                  (assoc :term current-search-term)
                                  h/camel-case-keys
                                  clj->js))
            ;
            ; NOTE: needed to force update internal state of input field when term changes *from outside*,
            ; either by app-db change or when navigating (using history length).
            ; But: history length is does not change when using the back button! we'd need something like a "Location Key", see <https://github.com/remix-run/react-router/blob/82adc1601039a87e2f925690cb949ce85a766150/docs/getting-started/concepts.md#locations>
            ; Alternative: listen to window.history and set a new key on route change
            input-key (str (saved-filters :term) (->> js/window .-history .-length))
            ;
            on-input-submit #(let [term (presence (.-searchTerm %))]
                               (dispatch-fn (assoc saved-filters :term term)))
            on-clear-filter (fn [filter-to-clear]
                              (dispatch-fn
                               (case (.-type filter-to-clear)
                                 "pool" (dissoc saved-filters :pool-id)
                                 "onlyAvailable" (dissoc saved-filters :only-available))))]

        [:<>

         (when @modal-shown?
           [filter-modal
            hide!
            dispatch-fn
            (assoc saved-filters :term current-search-term)])

         [:> UI/Components.ModelSearchFilter
          {:key input-key
           :availableFilters available-filters
           :currentFilters current-filters
           :onChangeSearchTerm set-entered-search-term
           :onOpenPanel show!
           :onClearFilter on-clear-filter
           :onSubmit on-input-submit
           :locale locale
           :txt (model-search-filter-texts)}]]))))
