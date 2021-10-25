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
    :refer [t set-default-translate-path with-translate-path]]
   [leihs.borrow.lib.helpers :refer [log spy]]
   [leihs.borrow.lib.form-helpers :refer [UiInputWithClearButton UiDatepicker]]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.core.core :refer [remove-blanks presence update-vals]]
   ["date-fns" :as date-fns]
   ["date-fns/locale" :as locale]
   ["/leihs-ui-client-side-external-react" :as UI]))

(defn default-dispatch-fn [query-params]
  (dispatch [:routing/navigate
             [::routes/models {:query-params query-params}]]))

(reg-event-db ::toggle-debug
              (fn-traced [db _]
                (update-in db [:ls :debug ::filter-labels] not)))

(reg-event-db ::save-options
              (fn-traced [db [_ query-params]]
                (assoc-in db [:ls ::options] query-params)))

(reg-event-db ::clear-options
              (fn-traced [db _]
                (assoc-in db [:ls ::options] nil)))

(reg-sub ::filter-labels
         (fn [db _] (get-in db [:ls :debug ::filter-labels])))

(reg-sub ::options
         (fn [db _]
           (->> db :ls ::options (update-vals #(or % "")))))

(defn filter-modal [hide! dispatch-fn saved-opts locale auth-user-id]
  (log locale)
  (let [format-date #(some-> %
                             (date-fns/parse "yyyy-MM-dd" (js/Date.))
                             (date-fns/format "P" #js {:locale locale}))
        user-id (r/atom (or (:user-id saved-opts) auth-user-id))
        term (r/atom (or (:term saved-opts) ""))
        pool-id (r/atom (or (:pool-id saved-opts) ""))
        only-available (r/atom (let [oa (:only-available saved-opts)]
                                 (if (nil? oa) false oa)))
        start-date (r/atom (or (some-> saved-opts :start-date format-date) ""))
        end-date (r/atom (or (some-> saved-opts :end-date format-date) ""))
        quantity (r/atom (or (:quantity saved-opts) 1))]
    (fn [hide! dispatch-fn saved-opts locale auth-user-id]
      (let [pools @(subscribe [::current-user/pools])
            auth-user @(subscribe [::current-user/user-data])
            target-users @(subscribe [::current-user/target-users
                                      (t :!borrow.rental-show.user-or-delegation-personal-postfix)])
            locale-to-use @(subscribe [::translate/locale-to-use])
            locale (case locale-to-use :de-CH locale/de :en-GB locale/enGB)
            start-date-and-end-date-set? #(and (presence @start-date) (presence @end-date))
            start-date-equal-or-before-end-date?
            #(let [s (date-fns/parse @start-date "P" (js/Date.) #js {:locale locale})
                   e (date-fns/parse @end-date "P" (js/Date.) #js {:locale locale})]
               (or (date-fns/isEqual s e) (date-fns/isBefore s e)))
            valid? #(or (not @only-available)
                        (and (start-date-and-end-date-set?)
                             (start-date-equal-or-before-end-date?)))]
        (with-translate-path :borrow.filter
          [:> UI/Components.Design.ModalDialog {:title "Filter" :shown true}
           [:> UI/Components.Design.ModalDialog.Body
            [:> UI/Components.Design.Stack {:space 4}
             [:> UI/Components.Design.Section {:title (t :delegations {:n 1})
                                               :collapsible true}
              [:label.visually-hidden {:html-for "user-id"} (t :pools.title)]
              [:select.form-select
               {:name "user-id" :id "user-id" :value @user-id
                :on-change (fn [e] (reset! user-id (-> e .-target .-value)))}
               (doall (for [{:keys [id name]} target-users]
                        [:option {:value id :key id} name]))]]

             [:> UI/Components.Design.Section {:title (t :search.title) :collapsible true}
              [:label.visually-hidden {:html-for "term"} (t :search.title)]
              ; --------------------------------------------
              ; There is some bug when one clicks on X icon.
              ; --------------------------------------------
              ; [UiInputWithClearButton {:name "term"
              ;                          :id "term"
              ;                          :placeholder (t :search.placeholder)
              ;                          :value @term
              ;                          :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]
              [:input.form-control {:name "term"
                                    :id "term"
                                    :placeholder (t :search.placeholder)
                                    :value @term
                                    :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]

             [:> UI/Components.Design.Section {:title (t :pools.title) :collapsible true}
              [:label.visually-hidden {:html-for "pool-id"} (t :pools.title)]
              [:select.form-select {:name "pool-id" :id "pool-id" :value (or @pool-id "all")
                                    :on-change (fn [e] (reset! pool-id (-> e .-target .-value)))}
               [:option {:value "all" :key "all"} (t :pools.all)]
               (doall (for [{pool-id :id pool-name :name} pools]
                        [:option {:value pool-id :key pool-id} pool-name]))]]

             [:<>
              [:> UI/Components.Design.Section {:title (t :show-only-available) :collapsible true}
               [:label.visually-hidden {:html-for "only-available"} (t :show-only-available)]
               [:input.form-check-input {:type :checkbox :name "only-available" :id "only-available"
                                         :checked @only-available
                                         :on-change (fn [_] (swap! only-available not))}]]

              (when @only-available
                [:> UI/Components.Design.Stack {:space 4}
                 [:> UI/Components.Design.Section {:title (t :time-span.title) :collapsible true}
                  [:fieldset
                   [:legend.visually-hidden (t :time-span.title)]
                   (letfn [(format-date [x]
                             (some-> x
                                     (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                     (date-fns/format "P" #js {:locale locale})))]
                     [:div.d-flex.flex-column.gap-3
                      [#_UiDatepicker :> UI/Components.Design.DatePicker
                       {:locale locale
                        :name "start-date"
                        :id "start-date"
                        :value @start-date
                        :on-change (fn [e] (reset! start-date (-> e .-target .-value)))
                        :placeholder (t :time-span.undefined)
                        :label (r/as-element [:label {:html-for "start-date"} (t :from)])}]
                      [#_UiDatepicker :> UI/Components.Design.DatePicker
                       {:locale locale
                        :name "end-date"
                        :id "end-date"
                        :value @end-date
                        :on-change (fn [e] (reset! end-date (-> e .-target .-value)))
                        :placeholder (t :time-span.undefined)
                        :label (r/as-element [:label {:html-for "end-date"} (t :until)])}]])]]

                 [:> UI/Components.Design.Section {:title (t :quantity) :collapsible true}
                  [:> UI/Components.Design.MinusPlusControl
                   {:name "quantity"
                    :id "quantity"
                    :value @quantity
                    :min 1
                    :onChange (fn [n] (reset! quantity n))}]]

                 (cond
                   (not (start-date-and-end-date-set?))
                   [:> UI/Components.Design.Warning (t :time-span.errors.start-date-and-end-date-set)]
                   (not (start-date-equal-or-before-end-date?))
                   [:> UI/Components.Design.Warning  (t :time-span.errors.start-date-equal-or-before-end-date)])])]]]

           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-secondary
             {:type "button"
              :onClick #(do (reset! term "")
                            (reset! pool-id "all")
                            (reset! user-id "")
                            (reset! only-available false)
                            (reset! start-date "")
                            (reset! end-date "")
                            (reset! quantity 1))}
             (t :reset)]
            [:button.btn.btn-primary
             {:type "button"
              :disabled (not (valid?))
              :onClick
              #(do (hide!)
                   (dispatch-fn
                    (remove-blanks
                     (letfn [(format-date [x]
                               (some-> x
                                       (date-fns/parse "P" (js/Date.) #js {:locale locale})
                                       (date-fns/format "yyyy-MM-dd")))]
                       {:term @term
                        :pool-id (if (= @pool-id "all") nil @pool-id)
                        :user-id (when (not= @user-id (:id auth-user)) @user-id)
                        :only-available (when @only-available @only-available)
                        :start-date (when @only-available (format-date @start-date))
                        :end-date (when @only-available (format-date @end-date))
                        :quantity (when @only-available @quantity)}))))}
             (t :apply)]]])))))

(defn filter-comp [dispatch-fn]
  (let [modal-shown? (r/atom false)]
    (fn [dispatch-fn]
      (let [hide! #(reset! modal-shown? false)
            show! #(reset! modal-shown? true)
            auth-user @(subscribe [::current-user/user-data])
            saved-opts @(subscribe [::options])
            locale @(subscribe [::translate/i18n-locale])
            debug? @(subscribe [::filter-labels])]
        [:<>
         (when @modal-shown?
           [filter-modal hide! dispatch-fn saved-opts locale (:id auth-user)])
         [:> UI/Components.Design.FilterButton {:onClick show!}
          (if debug?
            (js/JSON.stringify (clj->js saved-opts))
            (t :borrow.home-page.show-search-and-filter))]
         [:div
          [:br]
          [:button.btn.btn-outline-secondary.btn-sm
           {:on-click #(dispatch [::toggle-debug])}
           "toggle debug"]]]))))