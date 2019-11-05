(ns leihs.borrow.client.features.shopping-cart
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   #_[leihs.borrow.client.components :as ui]))

(rf/reg-sub ::cart (fn [db] (get-in db [:cart] {})))
(rf/reg-sub
 ::items
 (fn [] [(rf/subscribe [::cart])])
 (fn [[cart]] (get-in cart [:items] {})))

(rf/reg-sub
 ::items-index
 (fn [] [(rf/subscribe [::items])])
 (fn [[items]] (get-in items [:index] {})))

(rf/reg-sub
 ::items-list
 (fn [] [(rf/subscribe [::items]) (rf/subscribe [:products/index])])
 (fn [[items products-index]]
   (map
    (fn [pid]
      {:product (get products-index pid)
       :quantity (get-in items [:index pid])})
    (:order items))))

(rf/reg-sub
 ::counts
 (fn [] [(rf/subscribe [::items-index])])
 (fn [[index]]
   {:products (count index)
    :items (reduce + (vals index))}))

(rf/reg-event-db
 ::add-item
 (fn [db [_ pid]]
   (-> db
       (update-in , [:cart :items :index pid] (fnil inc 0))
       (update-in , [:cart :items :order]
                    (fn [order]
                      (if (get-in db [:cart :items :index pid])
                        order
                        (conj order pid)))))))

(rf/reg-event-db
 ::decrease-item-quantity
 (fn [db [_ pid]]
   (let [current-count (get-in db [:cart :items :index pid] 0)]
     ; decrease by 1, BUT if last one, remove from index-map and order-vec
     (if (<= current-count 1)
       (-> db
           (update-in , [:cart :items :index] dissoc pid)
           (update-in , [:cart :items :order] (fn [ids] (vec (filter #(not= pid %) ids)))))
       (update-in db [:cart :items :index pid] dec)))))

(defn shopping-cart []
  (fn []
    (let [counts @(rf/subscribe [::counts])]
      [:<>
       [:h2 (str "SHOPPING CART (" (:products counts) "/" (:items counts) ")")]
       [:ul
        (doall
         (for [line @(rf/subscribe [::items-list])]
           (let [pid (get-in line [:product :id])]
             [:li  {:key pid}
              (get-in line [:product :name]) " / "
              [:b (:quantity line)] " "
              [:button
               {:type :button :on-click #(rf/dispatch [::decrease-item-quantity pid])}
               "-"]
              [:button
               {:type :button :on-click #(rf/dispatch [::add-item pid])}
               "+"]])))]

       (when @(rf/subscribe [:is-debug?]) [:p (pr-str @(rf/subscribe [:cart]))])])))