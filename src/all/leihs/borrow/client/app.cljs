(ns leihs.borrow.client.app
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.client.components :as ui]
   
   [leihs.borrow.client.features.search :as search]
   [leihs.borrow.client.features.shopping-cart :as cart]
   ))

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
                              :filters {:current {:start-date "2019-11-01", :end-date "2019-11-02"}}})
            (assoc , :meta {:app {:debug false}}))
    :dispatch [::re-graph/init re-graph-config]}))

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
(defn fatal-error-screen [errors]
  [:section.p-4
   {:style {:white-space "pre-wrap" :background "salmon" :padding "1rem"}}
   [:h1 "FATAL ERROR :("]
   [:p [:button.border-black.border-2.rounded-full.py-1.px-3 {:type :button, :on-click #(-> js/window (.-location) (.reload))} "RELOAD"]]
   (doall
    (for
     [[idx error] (map-indexed vector errors)]
      [:small.code {:key idx} (js/JSON.stringify (clj->js error) 0 2)]))])

(def product-card-width-in-rem 12)
(def product-card-margins-in-rem 1)

(defn product-card [model width-in-rem]
  [:div.ui-product-card
   {:style {:width (str width-in-rem "rem")
            :min-height "15rem"
            :overflow-y "scroll"
            :border "1px solid tomato"
            :padding "1rem"
            :display "inline-block"
            :margin-right "1rem"}}
   [:h2 (:name model)]
   (if-let [img (get-in model [:images 0 :imageUrl])] 
     [:img {:src img :style {:width "100%"}}])
   #_[:p (pr-str model)]
   [:button
    {:type :button :on-click #(rf/dispatch [::cart/add-item (:id model)])}
    "+"]])

(defn model-grid-item [model]
  [:div.ui-model-grid-item.max-w-sm.rounded.overflow-hidden.bg-white.px-4.mb-2
   [:div.square-container.relative.rounded.overflow-hidden.border.border-gray-200
    (if-let [img (get-in model [:images 0 :imageUrl])]
      [:img.absolute.object-contain.object-center.h-full.w-full.p-1 {:src img}]
      [:div.absolute.h-full.w-full.bg-gray-400 " "])]
   [:div.mx-0.mt-1.leading-snug
    [:p.truncate.font-bold (:name model)]
    [:p.truncate (:manufacturer model)]
    ]])

(defn products-list []
  (let
   [models @(rf/subscribe [::search/found-models])]
    [:div.mx-3
     [:div.w-full.px-0
      [:div.ui-models-list.flex.flex-wrap.-mx-4
       (doall
        (for [model models]
          [:div {:class "w-1/2 min-h-16" :key (:id model)}
           [model-grid-item model]]))]]
     (when @(rf/subscribe [:is-debug?]) [:p (pr-str @(rf/subscribe [::search/found-models]))])]))

(defn main-view []
(fn []
  (let [errors @(rf/subscribe [:app/fatal-errors])]
    [:main
     [:div.ui-main-nav.px-2.py-1.border-b-2
      [:h1.font-black "AUSLEIHE"]]

     (if errors
       [fatal-error-screen errors]
       ; else
       [:<>
        [search/search-panel]
        [:hr.border-b-2]
        [products-list]
        #_[:hr]
        #_[shopping-cart]])])))

;-; CORE APP
(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [main-view]
            (.getElementById js/document "app")))


(defn ^:export main []
  (rf/dispatch-sync [::load-app])
  (mount-root)
  
  (rf/dispatch-sync
   [::re-graph/query
    (rc/inline "leihs/borrow/client/queries/getSearchFilters.gql")
    {}
    [::search/on-fetched-search-filters]]))

(main)
