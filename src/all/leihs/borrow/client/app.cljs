(ns leihs.borrow.client.app
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]

   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]

   [leihs.borrow.client.features.about-page :as about-page]
   [leihs.borrow.client.features.search-models :as search-models]
   [leihs.borrow.client.features.model-show :as model-show]))

(def re-graph-config {:ws-url nil :http-url "/borrow/graphql" :http-parameters {:with-credentials? true}})

;-; INIT APP & DB
(rf/reg-event-fx
 ::load-app
 (fn [{:keys [db]}]
   {:db (-> db
            (assoc , :products {:index {0 {:id 0 :name "Kamera"}
                                        1 {:id 1 :name "Mikrofon"}
                                        2 {:id 2 :name "Stativ"}}
                                :order [2 0 1]})
            (assoc , :cart {:items {:index {} :order []}})
            (assoc , :search {:results []
                              :filters {:current {:start-date "2020-12-01", :end-date "2020-12-02"}}})
            (assoc , :meta {:app {:debug false}}))
    :dispatch-n (list [::re-graph/init re-graph-config]
                      #_[::search-models/fetch-search-filters])}))

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
  [:main
   [ui/main-nav]
   [routing/routed-view views]])

(defn home-view []
  (fn []
    (let [errors @(rf/subscribe [:app/fatal-errors])]
      [:<>
       (if errors
         [ui/fatal-error-screen errors]
       ; else
         [:<>
          [search-models/search-panel]
          [:hr.border-b-2]
          [ui/tmp-nav]])])))

(defn- route-is-loading-view
  []
  [:div.app-loading-view
   [:h1 "loading…"]])

(defn- models-index-view [] [:h1.font-xl "MODELS INDEX"])

;-; CORE APP
(def views {::routes/home home-view
            ::routes/search search-models/view
            ::routes/about-page about-page/view
            ::routes/models-index models-index-view
            ::routes/models-show model-show/view
            ; FIXME: this is used for "loading" AND "not found", find a way to distinguish.
            ;        *should* not be a real problem – if the routing is working correctly 
            ;        we can never end up on "not found" client-side!
            :else route-is-loading-view})

(defn- dummy-route-event-handler
  [_ [_ route-match]]
  (js/console.log "Router navigated to: " route-match))

(rf/reg-event-fx ::routes/home dummy-route-event-handler)

; when going to '/' instead of '/borrow', do a redirect.
; this is only ever going to happen in development mode.
(rf/reg-event-fx 
 ::routes/absolute-root 
 (fn [_ _] {:routing/navigate [::routes/home]}))

;; tmp: attach handler for wip views to silence warnings
(rf/reg-event-fx ::routes/models-index dummy-route-event-handler)

(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [main-view views]
            (.getElementById js/document "app")))

(defn ^:export main []
  (rf/dispatch-sync [:routing/init-routing routes/routes-map])
  (rf/dispatch-sync [::load-app])
  (mount-root)
  
  ; FIXME: should be dispatched from `load-app` but that doesnt work :/
  (search-models/fetch-search-filters))
  

(main)
