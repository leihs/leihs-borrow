(ns leihs.borrow.features.home-page.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   #_[reagent.core :as r]
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
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
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

(defn view []
  (fn []
    (let [cats @(subscribe [::categories/categories-index])
          cats-url (routing/path-for ::routes/categories-index)
          ;; favs @(subscribe [::categories/categories-index])
          filters @(subscribe [::filters/current])]


      [:> UI/Components.AppLayout.Page

       [:> UI/Components.Design.PageLayout.Header {:title (t :catalog)}
        [:> UI/Components.FilterBubblePanelSwitcher {:labelText (t :show-search-and-filter)}
         [:<>
          [:span.fs-6.text-danger "TODO: new panel design, fullscreen"]
          ^{:key (hash filters)}
          [models/search-panel
           #(dispatch [:routing/navigate [::routes/models {:query-params %}]])
           #(dispatch [::models/clear])
           filters
           nil]]]]

       [:> UI/Components.Design.Stack
        [:> UI/Components.Design.Section {:title (t :!borrow.categories.title)}

         (categories/categories-list cats)]]])))
