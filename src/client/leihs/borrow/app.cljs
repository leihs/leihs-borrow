(ns leihs.borrow.app
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    #_[shadow.resource :as rc]
    [leihs.borrow.components :as ui]
    [leihs.borrow.ui.main-nav :as main-nav]

    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.client.routes :as routes]

    [leihs.borrow.features.home-page.core :as home-page]
    [leihs.borrow.features.about-page.core :as about-page]
    [leihs.borrow.features.models.core :as models]
    [leihs.borrow.features.shopping-cart.core :as shopping-cart]
    [leihs.borrow.features.shopping-cart.timeout :as timeout]
    [leihs.borrow.features.categories.show :as category-show]
    [leihs.borrow.features.categories.index :as category-index]
    [leihs.borrow.features.model-show.core :as model-show]
    [leihs.borrow.features.favorite-models.core :as favorite-models]
    [leihs.borrow.features.customer-orders.index :as customer-orders-index]
    [leihs.borrow.features.customer-orders.show :as customer-orders-show]
    [leihs.borrow.features.pools.index :as pools-index]
    [leihs.borrow.features.pools.show :as pools-show]
    ))

(def re-graph-config {:ws-url nil :http-url "/app/borrow/graphql" :http-parameters {:with-credentials? true}})

;-; INIT APP & DB
(reg-event-fx
  ::load-app
  (fn [{:keys [db]}]
    {:db (-> db
             ; NOTE: clear the routing instance on (re-)load,
             ; otherwise the event wont re-run when hot reloading!
             (dissoc , :routing/routing)
             (assoc , :meta {:app {:debug false}}))}))

;-; EVENTS
(reg-event-db :set-debug (fn [db [_ mode]] (js/console.log mode) (assoc-in db [:meta :app :debug] mode)))


;-; SUBSCRIPTIONS
(reg-sub :app/fatal-errors (fn [db] (get-in db [:meta :app :fatal-errors])))

(reg-sub :is-debug? (fn [db] (get-in db [:meta :app :debug] false)))


;-; VIEWS

(defn main-view [views]
  (let [errors @(subscribe [:app/fatal-errors])]
    [:main
     [main-nav/navbar]
     (when errors [ui/fatal-error-screen errors])
     [routing/routed-view views]]))

(defn- route-is-loading-view
  []
  [:div.app-loading-view
   [:h1.text-monospace.text-center.p-8.show-after-3sec
    [:p.text-5xl [ui/spinner-clock]]
    [:p.font-black.text-xl "loading…"]
    [:p.text-base "if this takes a long time something went wrong."]
    [:p.mt-4
     [:button.border-black.border-2.rounded-full.py-1.px-3
      {:type :button, :on-click #(-> js/window (.-location) (.reload))}
      "RELOAD"]]
    ]])

; (defn- not-found-view [] [:h1.font-black.text-monospace.text-5xl.text-center.p-8 "404 NOT FOUND!"])
(defn- wip-models-index-view [] [:h1.font-black.text-monospace.text-5xl.text-center.p-8 "WIP MODELS INDEX!"])

;-; CORE APP
(def views {::routes/home home-page/view
            ::routes/about-page about-page/view
            ::routes/categories-index category-index/view
            ::routes/categories-show category-show/view
            ::routes/models models/view
            ::routes/models-show model-show/view
            ::routes/models-favorites favorite-models/view
            ::routes/shopping-cart shopping-cart/view
            ::routes/orders-index customer-orders-index/view
            ::routes/orders-show customer-orders-show/view
            ::routes/pools-show pools-show/view
            ::routes/pools-index pools-index/view

            ; FIXME: this is used for "loading" AND "not found", find a way to distinguish.
            ;        *should* not be a real problem – if the routing is working correctly
            ;        we can never end up on "not found" client-side!
            ; ::routes/not-found not-found-view
            :else route-is-loading-view})

; when going to '/' instead of '/borrow', do a redirect.
; this is only ever going to happen in development mode.
(reg-event-fx
  ::routes/absolute-root
  (fn [_ _] {:routing/navigate [::routes/home]}))

(reg-fx :alert (fn [msg] (js/alert msg)))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [main-view views]
            (.getElementById js/document "app")))

(defn ^:export main []
  ; start the app framework; NOTE: order is important!
  (dispatch [::re-graph/init re-graph-config])
  (dispatch [::load-app])
  (dispatch [:routing/init-routing routes/routes-map])

  ; start the ui
  (mount-root))

(main)