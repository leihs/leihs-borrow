(ns leihs.borrow.features.home-page.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path with-translate-path]]
   [leihs.borrow.lib.filters :as filters]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.features.models.core :as models]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.categories.core :as categories]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.home-page)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/home
 (fn-traced [_ [_ {:keys [query-params]}]]
            {:dispatch-n (list [::filters/init]
                               [::filters/set-multiple query-params]
                               [::categories/fetch-index 4])}))

(defn filter-modal [shown? cancel-fn]
  (with-translate-path :borrow.filter
    [:> UI/Components.Design.ModalDialog {:title "Filter" :shown shown?}
     [:> UI/Components.Design.ModalDialog.Body
      [:> UI/Components.Design.Stack {:space 4}
       [:> UI/Components.Design.Section {:title (t :search.title) :collapsible true}
        [:label.visually-hidden {:html-for "term"} (t :search.title)]
        [:> UI/Components.Design.InputWithClearButton
         {:name "term"
          :id "term"
          :placeholder (t :search.placeholder)
          :value nil
          :onChange (fn [e] (js/console.log (-> e .-target .-value)))}]]]]
     [:> UI/Components.Design.ModalDialog.Footer
      [:button.btn.btn-secondary {:type "button" :onClick cancel-fn}
       (t :cancel)]
      [:button.btn.btn-secondary {:type "button" :onClick #(js/alert "reset")}
       (t :reset)]
      [:button.btn.btn-primary {:type "button" :onClick #(js/alert "apply")}
       (t :apply)]]]))

(defn view []
  (let [modal-shown? (r/atom false)]
    (fn []
      (let [cats @(subscribe [::categories/categories-index])
            cats-url (routing/path-for ::routes/categories-index)
            ;; favs @(subscribe [::categories/categories-index])
            filters @(subscribe [::filters/current])
            delegations true]
        [:> UI/Components.AppLayout.Page
         [:> UI/Components.Design.PageLayout.Header {:title (t :catalog)}
          [filter-modal @modal-shown? #(reset! modal-shown? false)]
          [:> UI/Components.Design.FilterButton {:onClick #(reset! modal-shown? true)}
           (t :show-search-and-filter)]]
         [:> UI/Components.Design.Stack
          [:> UI/Components.Design.Section {:title (t :!borrow.categories.title)}
           (categories/categories-list cats)]]]))))
