(ns leihs.borrow.app
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [leihs.borrow.lib.re-graph :as re-graph]
   [leihs.borrow.components :as ui]
   [leihs.borrow.ui.main-nav :as main-nav]

   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch
                                      dispatch-sync]]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.requests :as requests]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :as translate :refer [t]]
   [leihs.borrow.client.routes :as routes]

   [leihs.borrow.features.debug-page.core :as debug-page]
   [leihs.borrow.features.categories.show :as category-show]
   [leihs.borrow.features.current-user.show :as current-user-show]
   [leihs.borrow.features.current-user.profile-tracker :refer [track-last-delegation-id]]
   [leihs.borrow.features.customer-orders.index :as customer-orders-index]
   [leihs.borrow.features.customer-orders.show :as customer-orders-show]
   [leihs.borrow.features.favorite-models.core :as favorite-models]
   [leihs.borrow.features.home-page.core :as home-page]
   [leihs.borrow.features.languages.core :as languages]
   [leihs.borrow.features.model-show.core :as model-show]
   [leihs.borrow.features.models.core :as models]
   [leihs.borrow.features.pools.index :as pools-index]
   [leihs.borrow.features.pools.show :as pools-show]
   [leihs.borrow.features.shopping-cart.core :as shopping-cart]
   [leihs.borrow.features.templates.index :as templates-index]
   [leihs.borrow.features.templates.show :as templates-show]
   ["/leihs-ui-client-side-external-react" :as UI]
   [leihs.borrow.testing.step-1 :as testing-step-1]
   [leihs.borrow.testing.step-2 :as testing-step-2]
   [leihs.borrow.translations :as translations]
   ))

;-; INIT APP & DB
(reg-event-fx
 ::init-app-db
 (fn-traced [{:keys [db]} [_]]
   {:db (-> db
             ; NOTE: clear the routing instance on (re-)load,
             ; otherwise the event wont re-run when hot reloading!
            (dissoc , :routing/routing)
            (assoc , :meta {:app {:debug false}})
            (assoc-in , [::languages/data] translations/dict))}))

;-; EVENTS
(reg-event-db :set-debug
              (fn-traced [db [_ mode]]
                (js/console.log mode)
                (assoc-in db [:meta :app :debug] mode)))

;-; SUBSCRIPTIONS
(reg-sub :is-debug? (fn [db] (get-in db [:meta :app :debug] false)))

;-; VIEWS

(defn main-view [views]
  (let [errors @(subscribe [::errors/errors])
        menu-data @(subscribe [::main-nav/menu-data])
        current-menu (:current-menu menu-data)]
    [:> UI/Components.Design.PageLayout
     {:topBar (r/as-element [main-nav/top])
      :nav (r/as-element [main-nav/side])
      :navShown (= current-menu "main")
      :errorBoundaryTxt {:title (t :borrow.errors.render-error)
                         :reload (t :borrow.errors.reload)
                         :goToStart (t :borrow.errors.go-to-start)}
      :flyout (r/as-element (case current-menu
                              "user" [main-nav/user-profile-nav]
                              "app" [main-nav/app-nav]
                              nil))
      :flyoutShown (or (= current-menu "user") (= current-menu "app"))
      :onContentClick #(dispatch [::main-nav/set-current-menu nil])}
     #_[requests/retry-banner]
     [routing/routed-view views]
     [ui/error-notification errors]]))

(defn- route-is-loading-view
  []
  [:div.app-loading-view
   [:h1.text-monospace.text-center.p-5.show-after-3sec
    [:p {:style {:font-size "3rem"}} [ui/spinner-clock]]
    [:p "loading…"]
    [:p.fw-light "if this takes a long time something went wrong."]
    [:p.mt-4
     [:button.btn.btn-lg.btn-dark.rounded-pill.px-4
      {:type :button, :on-click #(-> js/window (.-location) (.reload))}
      "RELOAD"]]]])

;-; CORE APP
(def views {::routes/home home-page/view
            ::routes/debug-page debug-page/view
            ::routes/categories-show category-show/view
            ::routes/current-user-show current-user-show/view
            ::routes/models models/view
            ::routes/models-show model-show/view
            ::routes/models-favorites favorite-models/view
            ::routes/shopping-cart shopping-cart/view
            ::routes/rentals-index customer-orders-index/view
            ::routes/rentals-show customer-orders-show/view
            ::routes/inventory-pools-show pools-show/view
            ::routes/inventory-pools-index pools-index/view
            ::routes/templates-show templates-show/view
            ::routes/templates-index templates-index/view
            ::routes/testing-step-1 testing-step-1/view
            ::routes/testing-step-2 testing-step-2/view

            ; FIXME: this is used for "loading" AND "not found", find a way to distinguish.
            ;        *should* not be a real problem – if the routing is working correctly
            ;        we can never end up on "not found" client-side!
            ; ::routes/not-found not-found-view
            :else route-is-loading-view})

; when going to '/' instead of '/borrow', do a redirect.
; this is only ever going to happen in development mode.
(reg-event-fx
 ::routes/absolute-root
 (fn-traced [_ _] {:routing/navigate [::routes/home]}))

(reg-fx :alert (fn [msg] (js/alert msg)))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (rdom/render [main-view views]
               (.getElementById js/document "app")))

(defn ^:export ^:dev/after-load main []
  ; start the app framework; NOTE: order is important!
  (re-graph/init) ; dispatch-sync
  (dispatch-sync [::init-app-db])
  (dispatch-sync [:routing/init-routing routes/routes-map])
  (track-last-delegation-id)
  ; start the ui
  (mount-root))

; ??? braucht es imho nicht (mehr) TS
;(main)
