(ns leihs.borrow.app
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [re-frame.core :as rf]
    [leihs.borrow.lib.re-graph :as re-graph]
    #_[shadow.resource :as rc]
    [leihs.borrow.components :as uic]
    [leihs.borrow.ui.main-nav :as main-nav]

    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.client.routes :as routes]

    [leihs.borrow.features.about-page.core :as about-page]
    [leihs.borrow.features.categories.index :as category-index]
    [leihs.borrow.features.categories.show :as category-show]
    [leihs.borrow.features.current-user.show :as current-user-show]
    [leihs.borrow.features.customer-orders.index :as customer-orders-index]
    [leihs.borrow.features.customer-orders.show :as customer-orders-show]
    [leihs.borrow.features.delegations.index :as delegations-index]
    [leihs.borrow.features.delegations.show :as delegations-show]
    [leihs.borrow.features.favorite-models.core :as favorite-models]
    [leihs.borrow.features.home-page.core :as home-page]
    [leihs.borrow.features.model-show.core :as model-show]
    [leihs.borrow.features.models.core :as models]
    [leihs.borrow.features.pools.index :as pools-index]
    [leihs.borrow.features.pools.show :as pools-show]
    [leihs.borrow.features.shopping-cart.core :as shopping-cart]
    [leihs.borrow.features.shopping-cart.draft :as draft-order]
    [leihs.borrow.features.shopping-cart.timeout :as timeout]
    [leihs.borrow.features.templates.index :as templates-index]
    [leihs.borrow.features.templates.show :as templates-show]
    [leihs.borrow.features.visits.pickups :as pickups-index]
    [leihs.borrow.features.visits.returns :as returns-index]
    ;; [leihs.borrow.shared-ui :as UI]
    ["/leihs-ui-client-side-external-react" :as UI]
    [leihs.borrow.testing.step-1 :as testing-step-1]
    [leihs.borrow.testing.step-2 :as testing-step-2]
    ))

;-; INIT APP & DB
(reg-event-fx
  ::load-app
  (fn-traced [{:keys [db]}]
    {:db (-> db
             ; NOTE: clear the routing instance on (re-)load,
             ; otherwise the event wont re-run when hot reloading!
             (dissoc , :routing/routing)
             (assoc , :meta {:app {:debug false}}))}))

;-; EVENTS
(reg-event-db :set-debug (fn-traced [db [_ mode]] (js/console.log mode) (assoc-in db [:meta :app :debug] mode)))


;-; SUBSCRIPTIONS
(reg-sub :app/fatal-errors (fn [db] (get-in db [:meta :app :fatal-errors])))

(reg-sub :is-debug? (fn [db] (get-in db [:meta :app :debug] false)))


;-; VIEWS

(defn main-view [views]
  (let [errors @(subscribe [:app/fatal-errors])]
    [:> UI/Components.AppLayout.MainView
     {:navbar (r/as-element [main-nav/navbar])}
     (when errors [uic/fatal-error-screen errors])
     [routing/routed-view views]]))

(defn- route-is-loading-view
  []
  [:div.app-loading-view
   [:h1.text-monospace.text-center.p-5.show-after-3sec
    [:p.text-5xl [uic/spinner-clock]]
    [:p.font-black.text-xl "loading…"]
    [:p.text-base "if this takes a long time something went wrong."]
    [:p.mt-4
     [:button.btn.btn-lg.btn-dark.rounded-pill.px-4
      {:type :button, :on-click #(-> js/window (.-location) (.reload))}
      "RELOAD"]]
    ]])

; (defn- not-found-view [] [:h1.font-black.text-monospace.text-5xl.text-center.p-8 "404 NOT FOUND!"])
; (defn- wip-models-index-view [] [:h1.font-black.text-monospace.text-5xl.text-center.p-8 "WIP MODELS INDEX!"])

;-; CORE APP
(def views {::routes/home home-page/view
            ::routes/about-page about-page/view
            ::routes/categories-index category-index/view
            ::routes/categories-show category-show/view
            ::routes/current-user-show current-user-show/view
            ::routes/delegations-index delegations-index/view
            ::routes/delegations-show delegations-show/view
            ::routes/draft-order draft-order/view
            ::routes/models models/view
            ::routes/models-show model-show/view
            ::routes/models-favorites favorite-models/view
            ::routes/shopping-cart shopping-cart/view
            ::routes/orders-index customer-orders-index/view
            ::routes/orders-show customer-orders-show/view
            ::routes/inventory-pools-show pools-show/view
            ::routes/inventory-pools-index pools-index/view
            ::routes/templates-show templates-show/view
            ::routes/templates-index templates-index/view
            ::routes/pickups-index pickups-index/view
            ::routes/returns-index returns-index/view
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

(defn ^:export main []
  ; start the app framework; NOTE: order is important!
  (re-graph/init)
  (dispatch [::load-app])
  (dispatch [:routing/init-routing routes/routes-map])

  ; start the ui
  (mount-root))

(main)
