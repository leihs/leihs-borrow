(ns leihs.borrow.client.app
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]

   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]

   [leihs.borrow.client.features.home-page.core :as home-page]
   [leihs.borrow.client.features.about-page.core :as about-page]
   [leihs.borrow.client.features.search-models.core :as search-models]
   [leihs.borrow.client.features.shopping-cart.core :as shopping-cart]
   [leihs.borrow.client.features.categories.show :as category-show]
   [leihs.borrow.client.features.categories.index :as category-index]
   [leihs.borrow.client.features.model-show.core :as model-show]
   [leihs.borrow.client.features.favorite-models.core :as favorite-models]
   ))

(def re-graph-config {:ws-url nil :http-url "/app/borrow/graphql" :http-parameters {:with-credentials? true}})

;-; INIT APP & DB
(rf/reg-event-fx
 ::load-app
 (fn [{:keys [db]}]
   {:db (-> db
            ; NOTE: clear the routing instance on (re-)load,
            ; otherwise the event wont re-run when hot reloading!
            (dissoc , :routing/routing)
            (assoc , :meta {:app {:debug false}}))}))

;-; EVENTS
(rf/reg-event-db :set-debug (fn [db [_ mode]] (js/console.log mode) (assoc-in db [:meta :app :debug] mode)))

;-; SUBSCRIPTIONS
(rf/reg-sub :app/fatal-errors (fn [db] (get-in db [:meta :app :fatal-errors])))

(rf/reg-sub :is-debug? (fn [db] (get-in db [:meta :app :debug] false)))


;-; VIEWS

(defn main-view [views]
  (let [errors @(rf/subscribe [:app/fatal-errors])]
    [:main
     [ui/main-nav]
     (when errors [ui/fatal-error-screen errors])
     [routing/routed-view views]]))

(defn- route-is-loading-view
  []
  [:div.app-loading-view
   [:h1.font-mono.text-center.p-8.show-after-3sec
    [:p.text-5xl [ui/spinner-clock]]
    [:p.font-black.text-xl "loading…"]
    [:p.text-base "if this takes a long time something went wrong."]
    [:p.mt-4
     [:button.border-black.border-2.rounded-full.py-1.px-3
      {:type :button, :on-click #(-> js/window (.-location) (.reload))}
      "RELOAD"]]
    ]])

; (defn- not-found-view [] [:h1.font-black.font-mono.text-5xl.text-center.p-8 "404 NOT FOUND!"])
(defn- wip-models-index-view [] [:h1.font-black.font-mono.text-5xl.text-center.p-8 "WIP MODELS INDEX!"])

;-; CORE APP
(def views {::routes/home home-page/view
            ::routes/search search-models/view
            ::routes/about-page about-page/view
            ::routes/categories-index category-index/view
            ::routes/categories-show category-show/view
            ::routes/models-index wip-models-index-view
            ::routes/models-show model-show/view
            ::routes/models-favorites favorite-models/view
            ::routes/shopping-cart shopping-cart/view
            ; FIXME: this is used for "loading" AND "not found", find a way to distinguish.
            ;        *should* not be a real problem – if the routing is working correctly
            ;        we can never end up on "not found" client-side!
            :else route-is-loading-view})

; when going to '/' instead of '/borrow', do a redirect.
; this is only ever going to happen in development mode.
(rf/reg-event-fx
 ::routes/absolute-root
 (fn [_ _] {:routing/navigate [::routes/home]}))

(rf/reg-fx :alert (fn [msg] (js/alert msg)))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [main-view views]
            (.getElementById js/document "app")))

(defn ^:export main []
  ; start the app framework; NOTE: order is important!
  (rf/dispatch [::re-graph/init re-graph-config])
  (rf/dispatch [::load-app])
  (rf/dispatch [:routing/init-routing routes/routes-map])

  ; start the ui
  (mount-root))


(main)
