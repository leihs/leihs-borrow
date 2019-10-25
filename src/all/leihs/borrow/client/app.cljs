(ns leihs.borrow.client.app
  (:require 
   [reagent.core :as r]
   [re-frame.core :as rf]))

;-; DB
(rf/reg-event-fx
 ::load-app
 (fn [{:keys [db]}]
   {:db (-> db
            (assoc , :products {:index {0 {:id 0 :name "Kamera"}
                                        1 {:id 1 :name "Mikrofon"}
                                        2 {:id 2 :name "Stativ"}}
                                :order [2 0 1]})
            (assoc , :cart {:items {:index {} :order []}})
            (assoc , :meta {:app {:debug false}}))}))

;-; EVENTS
(rf/reg-event-db :set-debug (fn [db [_ mode]] (js/console.log mode) (assoc-in db [:meta :app :debug] mode)))
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
(rf/reg-sub :is-debug? (fn [db] (get-in db [:meta :app :debug] false)))
(rf/reg-sub :products (fn [db] (get-in db [:products] {})))

(rf/reg-sub
 :products/index
 (fn [] [(rf/subscribe [:products])])
 (fn [[products]] (get-in products [:index] {})))

(rf/reg-sub
 :products/order
 (fn [] [(rf/subscribe [:products])])
 (fn [[products]] (get-in products [:order] [])))

(rf/reg-sub
 :products/list
 (fn [] [(rf/subscribe [:products/index]) (rf/subscribe [:products/order])])
 (fn [[index order]]
   (map #(get index %) order)))
 
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
   #_items
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
(defn search-panel []
  [:form
   [:fieldset
    [:legend "SUCHE"]
    [:label "Text " [:input {:type :text, :name "search-term"}]] " "
    [:label "Start-datum " [:input {:type :date, :name "start-date"}]] " "
    [:label "End-datum " [:input {:type :date, :name "end-date"}]] " "
    [:br]
    [:label "Kategorie " [:input {:type :select, :name "category"}]] " "
    [:label "Pool " [:input {:type :select, :name "pool"}]] " "]])

(defn products-list []
  [:<>
   [:h2 "PRODUCTS"]
   (if @(rf/subscribe [:is-debug?]) [:p (pr-str @(rf/subscribe [:products]))])
   [:ul
    (doall
     (for [item @(rf/subscribe [:products/list])]
       [:li {:key (:id item)} 
        (:name item) " "
        [:button
         {:type :button :on-click #(rf/dispatch [:cart/add-item (:id item)])}
         "+"]]))]])

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

       (if @(rf/subscribe [:is-debug?]) [:p (pr-str @(rf/subscribe [:cart]))])])))

(defn main-view []
  [:main {:style {:padding "2rem"}}
   [:h1 "AUSLEIHE"]
   [:hr]
   [search-panel]
   [:hr]
   [products-list]
   [:hr]
   [shopping-cart]])

;-; CORE APP
(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [main-view]
            (.getElementById js/document "app")))

(defn ^:export main []
  (rf/dispatch-sync [::load-app])
  (mount-root))

(main)
