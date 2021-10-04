(ns leihs.borrow.features.filter-modal.core
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
   [leihs.borrow.lib.translate :refer [t set-default-translate-path with-translate-path]]
   [leihs.borrow.lib.helpers :refer [log spy]]
   [leihs.borrow.lib.filters :as filters]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.core.core :refer [remove-nils presence]]
   ["date-fns" :as date-fns]
   ["date-fns/locale" :as locale]
   ["/leihs-ui-client-side-external-react" :as UI]))

(reg-event-db ::toggle-debug
              (fn-traced [db _]
                (update-in db [:ls :debug ::filter-labels] not)))

(reg-sub ::filter-labels
         (fn [db _] (get-in db [:ls :debug ::filter-labels])))

(defn filter-modal [shown? hide!]
  (let [user-id (r/atom nil)
        term (r/atom nil)
        pool-id (r/atom nil)
        only-available (r/atom nil)
        start-date (r/atom nil)
        end-date (r/atom nil)
        quantity (r/atom nil)]
    (fn [shown? hide!]
      (let [pools @(subscribe [::current-user/pools])
            auth-user @(subscribe [::current-user/user-data])
            target-users @(subscribe [::current-user/target-users
                                      (t :!borrow.rental-show.user-or-delegation-personal-postfix)])
            filters @(subscribe [::filters/current])
            format-date (fn [x]
                          (some-> x
                                  (date-fns/parse "yyyy-MM-dd" (js/Date.))
                                  (date-fns/format "dd.MM.yyyy")))
            start-date-and-end-date-set? #(and (presence @start-date) (presence @end-date))
            start-date-equal-or-before-end-date?
            #(let [s (date-fns/parse @start-date "dd.MM.yyyy" (js/Date.))
                   e (date-fns/parse @end-date "dd.MM.yyyy" (js/Date.))]
               (or (date-fns/isEqual s e) (date-fns/isBefore s e)))
            valid? #(or (not @only-available)
                        (and (start-date-and-end-date-set?)
                             (start-date-equal-or-before-end-date?)))]
        (with-translate-path :borrow.filter
          [:> UI/Components.Design.ModalDialog {:title "Filter" :shown shown?}
           [:> UI/Components.Design.ModalDialog.Body
            [:> UI/Components.Design.Stack {:space 4}
             [:> UI/Components.Design.Section {:title (t :delegations {:n 1})
                                               :collapsible true}
              [:label.visually-hidden {:html-for "user-id"} (t :pools.title)]
              [:select.form-select
               {:name "user-id" :id "user-id" :value (or @user-id (:user-id filters))
                :on-change (fn [e] (reset! user-id (-> e .-target .-value)))}
               (doall (for [{:keys [id name]} target-users]
                        [:option {:value id :key id} name]))]]

             [:> UI/Components.Design.Section {:title (t :search.title) :collapsible true}
              [:label.visually-hidden {:html-for "term"} (t :search.title)]
              [:> UI/Components.Design.InputWithClearButton
               {:name "term"
                :id "term"
                :placeholder (t :search.placeholder)
                :value (or @term (:term filters))
                :onChange (fn [e] (reset! term (-> e .-target .-value)))}]]

             [:> UI/Components.Design.Section {:title (t :pools.title) :collapsible true}
              [:label.visually-hidden {:html-for "pool-id"} (t :pools.title)]
              [:select.form-select {:name "pool-id" :id "pool-id" :value (or @pool-id (:pool-id filters) "all")
                                    :on-change (fn [e] (reset! pool-id (-> e .-target .-value)))}
               [:option {:value "all" :key "all"} (t :pools.all)]
               (doall (for [{pool-id :id pool-name :name} pools]
                        [:option {:value pool-id :key pool-id} pool-name]))]]

             [:> UI/Components.Design.Section {:title (t :show-only-available) :collapsible true}
              [:label.visually-hidden {:html-for "only-available"} (t :show-only-available)]
              [:input.form-check-input {:type :checkbox :name "only-available" :id "only-available"
                                        :checked (if (nil? @only-available)
                                                  (or (:only-available filters) false)
                                                  @only-available)
                                        :on-change (fn [_] (swap! only-available not))}]]

             (when @only-available
               [:> UI/Components.Design.Stack {:space 4}
                [:> UI/Components.Design.Section {:title (t :time-span.title) :collapsible true}
                 [:fieldset
                  [:legend.visually-hidden (t :time-span.title)]
                  [:div.d-flex.flex-column.gap-3
                   [:> UI/Components.Design.DatePicker
                    {:locale locale/de
                     :name "start-date"
                     :id "start-date"
                     :value (or @start-date (some-> filters :start-date format-date))
                     :on-change (fn [e] (reset! start-date (-> e .-target .-value)))
                     :placeholder (t :time-span.undefined)
                     :label (r/as-element [:label {:html-for "start-date"} (t :from)])}]
                   [:> UI/Components.Design.DatePicker
                    {:locale locale/de
                     :name "end-date"
                     :id "end-date"
                     :value (or @end-date (some-> filters :start-date format-date))
                     :on-change (fn [e] (reset! end-date (-> e .-target .-value)))
                     :placeholder (t :time-span.undefined)
                     :label (r/as-element [:label {:html-for "end-date"} (t :until)])}]]]]

                [:> UI/Components.Design.Section {:title (t :quantity) :collapsible true}
                 [:> UI/Components.Design.MinusPlusControl
                  {:name "quantity"
                   :id "quantity"
                   :number (or @quantity (:quantity filters) 1)
                   :min 1
                   :onChange (fn [n] (reset! quantity n))}]]

                (cond
                 (not (start-date-and-end-date-set?))
                 [:> UI/Components.Design.Warning "Start and end date must be set."]
                 (not (start-date-equal-or-before-end-date?))
                 [:> UI/Components.Design.Warning "Start date must be equal to or before end date."])])]]

           [:> UI/Components.Design.ModalDialog.Footer
            [:button.btn.btn-secondary
             {:type "button"
              :onClick #(do (reset! term "")
                            (reset! pool-id "all")
                            (reset! user-id (:id auth-user))
                            (reset! only-available false)
                            (reset! start-date nil)
                            (reset! end-date nil)
                            (reset! quantity 1))}
             (t :reset)]
            [:button.btn.btn-primary
             {:type "button"
              :disabled (not (valid?))
              :onClick
              #(do (hide!)
                   (dispatch [:routing/navigate
                              [::routes/models
                               {:query-params
                                (remove-nils
                                 (letfn [(format-date [x]
                                           (some-> x
                                                   (date-fns/parse "dd.MM.yyyy" (js/Date.))
                                                   (date-fns/format "yyyy-MM-dd")))]
                                   {:term (presence @term)
                                    :pool-id (if (= @pool-id "all") nil @pool-id)
                                    :user-id @user-id
                                    :only-available (when @only-available @only-available)
                                    :start-date (when @only-available (format-date @start-date))
                                    :end-date (when @only-available (format-date @end-date))
                                    :quantity (when @only-available @quantity)}))}]]))}
             (t :apply)]]])))))

(defn reassess-filters [fs auth-user-id]
  (cond-> fs
    (some-> fs :quantity (= 1))
    (dissoc :quantity)
    (some-> fs :user-id (= auth-user-id))
    (dissoc :user-id)))

(defn filter-comp []
  (let [modal-shown? (r/atom false)]
    (fn []
      (let [hide! #(reset! modal-shown? false)
            show! #(reset! modal-shown? true)
            auth-user @(subscribe [::current-user/user-data])
            filters @(subscribe [::filters/current])
            filter-labels (reassess-filters filters (:id auth-user))
            debug? @(subscribe [::filter-labels])]
        [:<>
         [filter-modal @modal-shown? hide!]
         [:> UI/Components.Design.FilterButton {:onClick show!}
          (if debug? #_(empty? label-filters)
            (js/JSON.stringify (clj->js filters))
            (t :borrow.home-page.show-search-and-filter))]
         [:div
          [:br]
          [:button.btn.btn-outline-secondary.btn-sm 
           {:on-click #(dispatch [::toggle-debug])}
           "toggle debug"]]]))))
