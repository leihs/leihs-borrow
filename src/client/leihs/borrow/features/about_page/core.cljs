(ns leihs.borrow.features.about-page.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [clojure.string :as string]
   [reagent.core :as reagent]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      dispatch]]
   [leihs.borrow.lib.translate :refer [t]]
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.routing :as routing]
   ["/leihs-ui-client-side-external-react" :as UI]))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/about-page
 (fn-traced [_ [_ _]] {}))

(defn matches-media-query? [media-query]
  (-> (js/window.matchMedia media-query) .-matches))

(defn deco-bool [bool] (if bool "yes" "no"))

(defn- get-about-page-data []
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

(defn view []
  [:<>

   [:> UI/Components.Design.PageLayout.Header
    {:title (t :borrow.about-page/title)}]

   [:> UI/Components.Design.Stack {:space 4}
    [:> UI/Components.Design.Section {:title "Links which are not in menu (some of them maybe should)" :collapsible true}
     [:> UI/Components.Design.Menu
      [:> UI/Components.Design.Menu.Group
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/about-page)}
        (t :borrow.about-page/title)]
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/draft-order)}
        (t :borrow.shopping-cart.draft/title)]
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/delegations-index)}
        (t :borrow.delegations/title)]
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/templates-index)}
        (t :borrow.templates/title)]
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/inventory-pools-index)}
        (t :borrow.pools/title)]]]]


    [:> UI/Components.Design.Section {:title "Dev nav" :collapsible true}
     [:> UI/Components.Design.Menu
      [:> UI/Components.Design.Menu.Group
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/categories-show
                                                                   :categories-path "09ac0343-0d83-5c7f-b112-d5921e9479fd")}
        "a sample category"]
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/models)}
        "model index"]
       [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/models-show
                                                                   :model-id "1c18b3d3-88e8-57ac-8c28-24d3f8f77604")}
        "a sample model"]
       [:> UI/Components.Design.Menu.Link {:href "/app/borrow/graphiql/index.html"}
        "Graph" [:i "i"] "QL API console"]]]]


    [:div
     [:button.btn.btn-secondary {:type :button :on-click #(dispatch [::ls/clear])} "Clear :ls"]
     [:span " (clear local storage)"]]
    [:div [:button.btn.btn-secondary {:type :button :on-click #(reset! crash-atom true)} "Crash test"]
     (when @crash-atom [crash-component])]

    [:> UI/Components.Design.Section {:title "Debug info" :collapsible true}
     [:> UI/Components.Design.PropertyTable
      {:properties
       (map (fn [[key value]] {:key key :value value}) (get-about-page-data))}]]]])

