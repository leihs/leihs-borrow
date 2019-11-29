(ns leihs.borrow.client.app
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]

   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]

   [leihs.borrow.client.features.home-page :as home-page]
   [leihs.borrow.client.features.about-page :as about-page]
   [leihs.borrow.client.features.search-models :as search-models]
   [leihs.borrow.client.features.shopping-cart :as shopping-cart]
   [leihs.borrow.client.features.model-show :as model-show]))

(def re-graph-config {:ws-url nil :http-url "/borrow/graphql" :http-parameters {:with-credentials? true}})

;-; INIT APP & DB
(rf/reg-event-fx
 ::load-app
 (fn [{:keys [db]}]
   {:db (-> db
            ; NOTE: clear the routing instance on (re-)load,
            ; otherwise the even wont re-run when hot reloading!
            (dissoc , :routing/routing)
            
            (assoc , :products {:index {0 {:id 0 :name "Kamera"}
                                        1 {:id 1 :name "Mikrofon"}
                                        2 {:id 2 :name "Stativ"}}
                                :order [2 0 1]})
            (assoc , :search {:results []
                              :filters {:current {:start-date "2020-12-01", :end-date "2020-12-02"}}})
            (assoc , :meta {:app {:debug false}}))}))

;-; EVENTS
(rf/reg-event-db :set-debug (fn [db [_ mode]] (js/console.log mode) (assoc-in db [:meta :app :debug] mode)))

;-; SUBSCRIPTIONS
(rf/reg-sub :app/fatal-errors (fn [db] (get-in db [:meta :app :fatal-errors])))

(rf/reg-sub :is-debug? (fn [db] (get-in db [:meta :app :debug] false)))

(rf/reg-sub
 :products/index
 (fn [] [(rf/subscribe [:products-fake])])
 (fn [[products]] (get-in products [:index] {})))


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
   [:h1.font-black.font-mono.text-5xl.text-center.p-8 
    [ui/spinner-clock] 
    [:p.text-xl "loading…"]]])

(defn- wip-models-index-view [] [:h1.font-black.font-mono.text-5xl.text-center.p-8 "WIP MODELS INDEX"])

;-; CORE APP
(def views {::routes/home home-page/view
            ::routes/search search-models/view
            ::routes/about-page about-page/view
            ::routes/models-index wip-models-index-view
            ::routes/models-show model-show/view
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
