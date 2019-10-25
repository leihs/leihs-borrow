(ns leihs.borrow.client.app
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]))

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

;-; EVENTS - search
(rf/reg-event-fx
 :search/on-fetched-search-filters
 (fn [{:keys [db]} [_ {:keys [data errors] }]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:search :filters :available] data)})))

(rf/reg-event-db
 :search/set-filter
 (fn [db [_ key value]]
   (assoc-in db [:search :filters :current key] value)))

(rf/reg-event-fx
 :search/get-models
 (fn [_ [_ filters]]
   (let
    [query-vars
     {:searchTerm (get filters :term)
      :startDate (get filters :start-date)
      :endDate (get filters :end-date)
      ;( TODO: cats & pools :categories (map :id (get filters :end-date)))
      }]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/client/queries/searchModels.gql")
                 query-vars
                 [:search/on-fetched-models]]})))

(rf/reg-event-fx
 :search/on-fetched-models
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:search :results] (get-in data [:models]))})))


;-; EVENTS - shopping-cart
(rf/reg-event-db
 :cart/add-item
 (fn [db [_ pid]]
   (-> db
       (update-in , [:cart :items :index pid] (fnil inc 0))
       (update-in , [:cart :items :order]
                    (fn [order]
                      (if (get-in db [:cart :items :index pid])
                        order
                        (conj order pid)))))))

(rf/reg-event-db
 :cart/decrease-item-quantity
 (fn [db [_ pid]]
   (let [current-count (get-in db [:cart :items :index pid] 0)]
     ; decrease by 1, BUT if last one, remove from index-map and order-vec
     (if (<= current-count 1)
       (-> db
           (update-in , [:cart :items :index] dissoc pid)
           (update-in , [:cart :items :order] (fn [ids] (vec (filter #(not= pid %) ids)))))
       (update-in db [:cart :items :index pid] dec)))))


;-; SUBSCRIPTIONS
(rf/reg-sub :app/fatal-errors (fn [db] (get-in db [:meta :app :fatal-errors])))

;-; SUBSCRIPTIONS -- search
(rf/reg-sub :search/available-filters (fn [db] (get-in db [:search :filters :available] nil)))
(rf/reg-sub :search/current-filters (fn [db] (get-in db [:search :filters :current] nil)))

;-; SUBSCRIPTIONS -- models
(rf/reg-sub :search/found-models (fn [db] (get-in db [:search :results] nil)))

;-; SUBSCRIPTIONS -- shopping cart
(rf/reg-sub :is-debug? (fn [db] (get-in db [:meta :app :debug] false)))

(rf/reg-sub
 :products/index
 (fn [] [(rf/subscribe [:products-fake])])
 (fn [[products]] (get-in products [:index] {})))
 
(rf/reg-sub :cart (fn [db] (get-in db [:cart] {})))
(rf/reg-sub 
 :cart/items
 (fn [] [(rf/subscribe [:cart])])
 (fn [[cart]] (get-in cart [:items] {})))

(rf/reg-sub 
 :cart/items-index
 (fn [] [(rf/subscribe [:cart/items])])
 (fn [[items]] (get-in items [:index] {})))

(rf/reg-sub
 :cart/items-list
 (fn [] [(rf/subscribe [:cart/items]) (rf/subscribe [:products/index])])
 (fn [[items products-index]]
   (map
      (fn [pid]
        {:product (get products-index pid)
         :quantity (get-in items [:index pid])})
      (:order items))))

(rf/reg-sub
 :cart/counts
 (fn [] [(rf/subscribe [:cart/items-index])])
 (fn [[index]] 
   {:products (count index)
    :items (reduce + (vals index))}))

;-; VIEWS
(defn merge-props [defaults givens]
  (merge defaults givens {:style (merge (get defaults :style)
                                        (get givens :style))}))

(defn fatal-error-screen [errors]
  [:section
   {:style {:white-space "pre-wrap" :background "salmon" :padding "1rem"}}
   [:h1 "FATAL ERROR :("]
   [:p [:button {:type :button, :on-click #(-> js/window (.-location) (.reload))} "RELOAD"]]
   (doall
    (for
     [[idx error] (map-indexed vector errors)]
      [:small.code {:key idx} (js/JSON.stringify (clj->js error) 0 2)]))])

(defn button [label active? props]
  [:button
   (merge-props
    {:type :button
     :disabled (not active?)
     :style {:background (if active? "black" "grey")
             :color "white"
             :border "2px solid black"
             :border-color (if active? "black" "grey")
             :border-radius "1rem"}}
    props)
   label])

(defn form-line [name label input-props]
  [:label {:style {:display :table-row}}
   [:span {:style {:display :table-cell :padding-right "0.5rem"}} (str label " ")]
   [:input
    (merge
     input-props
     {:name name
      :placeholder label
      :style (merge {:display :table-cell} (get input-props :style))})]])

(defn search-panel []
  (fn []
    (let
     [current @(rf/subscribe [:search/current-filters])]
      [:form
       {:on-submit #((.preventDefault %) (rf/dispatch [:search/get-models current]))}
       [:fieldset {:style {:display :table}}
        [:legend "SUCHE"]
        [form-line :search-term "Text"
         {:type :text
          :value (get current :term)
          :on-change #(rf/dispatch [:search/set-filter :term (-> % .-target .-value)])}]

        [form-line :start-date "Start-datum"
         {:type :date
          :required true
          :value (get current :start-date)
          :on-change #(rf/dispatch [:search/set-filter :start-date (-> % .-target .-value)])}]

        [form-line :end-date "End-datum"
         {:type :date
          :required true
          :min (get current :start-date)
          :value (get current :end-date)
          :on-change #(rf/dispatch [:search/set-filter :end-date (-> % .-target .-value)])}]

        #_[form-line :category "Kategorie"
           {:type :select
            :value (get current :categories)
            :on-change #(rf/dispatch [:search/set-filter :categories (-> % .-target .-value)])}]
        
        #_[form-line :pool "Pool"
           {:type :select
            :value (get current :pools)
            :on-change #(rf/dispatch [:search/set-filter :pools (-> % .-target .-value)])}]
        
        (let [active? (boolean (and (get current :start-date) (get current :end-date)))]
          [button "Get Results" 
           active?
           {:type :submit 
            :title (when-not active? "Select start- and end-date to search!")
            :style {:margin-top "0.5rem"}}])]])))

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
    {:type :button :on-click #(rf/dispatch [:cart/add-item (:id model)])}
    "+"]])

(defn model-grid-item [model]
  [:div.ui-model-grid-item
   [:div.content
    #_{:style {:position "absolute" :width "100%" :height "100%"}}
    [:div.img-wrapper
     (if-let [img (get-in model [:images 0 :imageUrl])]
       [:img {:src img}])]

    [:p {:style {:font-weight "bold" :margin "0.5rem 0"}} (:name model)]]])

(defn products-list []
  (let [models @(rf/subscribe [:search/found-models])]
    [:<>
     #_[:h2 "RESULTS"]
     [:div.ui-models-list.square-grid
      (doall
       (for [model models]
         [:<> {:key (:id model)}
          [model-grid-item model]]))]
     (when @(rf/subscribe [:is-debug?]) [:p (pr-str @(rf/subscribe [:search/found-models]))])]))


(defn shopping-cart []
  (fn []
    (let [counts @(rf/subscribe [:cart/counts])]
      [:<>
       [:h2 (str "SHOPPING CART (" (:products counts) "/" (:items counts) ")")]
       [:ul
        (doall
          (for [line @(rf/subscribe [:cart/items-list])]
            (let [pid (get-in line [:product :id])]
              [:li  {:key pid}
               (get-in line [:product :name]) " / "
               [:b (:quantity line)] " "
               [:button
                {:type :button :on-click #(rf/dispatch [:cart/decrease-item-quantity pid])}
                "-"]
              [:button
               {:type :button :on-click #(rf/dispatch [:cart/add-item pid])}
               "+"]])))]

       (when @(rf/subscribe [:is-debug?]) [:p (pr-str @(rf/subscribe [:cart]))])])))

(defn main-view []
(fn []
  (let [errors @(rf/subscribe [:app/fatal-errors])]
    [:main {:style {:padding "2rem"}}
     [:h1 "AUSLEIHE"]
     [:hr]
     (if errors
       [fatal-error-screen errors]
       [:<>
        [search-panel]
        [:hr]
        [products-list]
        [:hr]
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
    [:search/on-fetched-search-filters]]))

(main)
