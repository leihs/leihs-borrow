(ns leihs.borrow.features.debug-page.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [clojure.string :as string]
   [reagent.core :as reagent]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      dispatch
                                      subscribe]]
   [leihs.borrow.lib.translate :refer [t]]
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.components :as ui]
   ["/leihs-ui-client-side-external-react" :as UI]))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/debug-page
 (fn-traced [_ [_ _]] {}))

(def sample-error {:some "details"})
(def sample-errors [sample-error sample-error])

(reg-event-fx
 ::add-fatal-error
 (fn-traced [{:keys [db]}]
   {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) sample-error)}))

(reg-event-fx
 ::throw
 (fn-traced []
   (throw (js/Error. "catch me if you can"))))

(reg-event-fx
 ::error-query
 (fn-traced [_ [_ _]]
   {:dispatch [::re-graph/query
               "query { testing(sleepSecs: \"bad-value\")}"
               {}
               [::on-error-request]]}))

(reg-event-fx
 ::error-mutation
 (fn-traced [_ [_ _]]
   {:dispatch [::re-graph/mutate
               "mutation { testing(sleepSecs: \"bad-value\")}"
               {}
               [::on-error-request]]}))

(reg-event-db
 ::on-error-request
 (fn-traced [db [_ payload]]
   (js/console.log "payload" payload)))


(defn matches-media-query? [media-query]
  (-> (js/window.matchMedia media-query) .-matches))

(defn deco-bool [bool] (if bool "yes" "no"))

(defn- get-debug-page-data []
  [["browser time" (js/String (js/Date.))]
   ["browser timezone" (-> (js/Intl.DateTimeFormat. :default) .resolvedOptions .-timeZone)]
   ["browser language(s)" (string/join ", " (or (.-languages js/navigator) [(.-language js/navigator)]))]
   ["CSS dark mode" (deco-bool (matches-media-query? "(prefers-color-scheme: dark)"))]
   ["CSS reduced motion" (deco-bool (matches-media-query? "(prefers-reduced-motion: reduce)"))]
   ["CSS pointer support" (cond
                            (matches-media-query? "(pointer: coarse)") "coarse (e.g. touch)"
                            (matches-media-query? "(pointer: fine)") "fine (e.g mouse)"
                            :else "unknown")]])

(defn crash-component [] (throw (js/Error. "I crashed!")))
(def crash-atom (reagent/atom nil))
(def show-error-screen (reagent/atom false))
(def show-error-view (reagent/atom false))
(def show-error-alert (reagent/atom false))

(defn view []
  [:<>

   [:> UI/Components.Design.PageLayout.Header
    {:title (t :borrow.debug-page/title)}]

   [:> UI/Components.Design.Stack {:space 4}
    [:> UI/Components.Design.Section {:title "Links which are not in the menu" :collapsible true}
     [:> UI/Components.Design.ListCard.Stack
      [:> UI/Components.Design.ListCard {:href (routing/path-for ::routes/debug-page)
                                         :one-line true}
       (t :borrow.debug-page/title) " (this page)"]
      [:> UI/Components.Design.ListCard {:href (routing/path-for ::routes/templates-index)
                                         :one-line true}
       (t :borrow.templates/title)]]]


    [:> UI/Components.Design.Section {:title "Dev nav" :collapsible true}
     [:> UI/Components.Design.ListCard.Stack
      [:> UI/Components.Design.ListCard {:href (routing/path-for ::routes/categories-show
                                                                 :categories-path "09ac0343-0d83-5c7f-b112-d5921e9479fd")
                                         :one-line true}
       "a sample category"]
      [:> UI/Components.Design.ListCard {:href (routing/path-for ::routes/models)
                                         :one-line true}
       "model index"]
      [:> UI/Components.Design.ListCard {:href (routing/path-for ::routes/models-show
                                                                 :model-id "1c18b3d3-88e8-57ac-8c28-24d3f8f77604")
                                         :one-line true}
       "a sample model"]
      [:> UI/Components.Design.ListCard {:href "/app/borrow/graphiql/index.html"
                                         :one-line true}
       "Graph" [:i "i"] "QL API console"]]]

    [:> UI/Components.Design.Section {:title "Local storage" :collapsible true}
     [:button.btn.btn-secondary {:type :button :on-click #(dispatch [::ls/clear])} "Clear :ls"]]

    [:> UI/Components.Design.Section {:title "Error catching" :collapsible true}
     [:> UI/Components.Design.Stack {:space 4}
      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(reset! crash-atom true) :class "mb-1"}
        "Exception in render"]
       [:br]
       "handled by ErrorBoundary in layout"
       (when @crash-atom [crash-component])]

      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(dispatch [::throw]) :class "mb-1"}
        "Exception in re-frame event handler"]
       [:br]
       "not handled, bubbles to console"]

      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(dispatch [::error-query]) :class "mb-1"}
        "GraphQL query with error"]
       [:br]
       "silently ignored, must be detected from return data"]

      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(dispatch [::error-mutation]) :class "mb-1"}
        "GraphQL mutation with error"]
       [:br]
       "retry banner, shown on top of layout"]]]

    [:> UI/Components.Design.Section {:title "Error views" :collapsible true}
     [:> UI/Components.Design.Stack {:space 4}
      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(reset! show-error-view true) :class "mb-1"}
        "Error view"]
       [:br]
       "just a placeable component"
       (when @show-error-view [ui/error-view sample-errors])]

      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(reset! show-error-screen true) :class "mb-1"}
        "Error screen"]
       [:br]
       "same as error view, but different title"
       (when @show-error-screen [ui/error-screen sample-errors])]

      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(dispatch [::add-fatal-error]) :class "mb-1"}
        "Fatal error"]
       [:br]
       "error screen, shown on top of layout, triggered via app db"]

      [:div
       [:button.btn.btn-secondary {:type :button :on-click #(reset! show-error-alert true) :class "mb-1"}
        "Error dialog"]
       [:br]
       "not used yet"
       (when @show-error-alert
         [:> UI/Components.Design.ConfirmDialog
          {:shown true :isError true :onConfirm #(reset! show-error-alert false) :title "Error"}
          "Lorem ipsum dolor"])]]]

    [:> UI/Components.Design.Section {:title "Debug info" :collapsible true}
     [:> UI/Components.Design.PropertyTable
      {:properties
       (map (fn [[key value]] {:key key :value value}) (get-debug-page-data))}]]]])
