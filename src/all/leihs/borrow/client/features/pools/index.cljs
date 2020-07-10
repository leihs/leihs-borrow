(ns leihs.borrow.client.features.pools.index
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [re-frame.std-interceptors :refer [path]]
    [shadow.resource :as rc]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.features.current-user.core :as current-user]
    [leihs.borrow.client.features.pools.core :refer [badge]]
    [leihs.borrow.client.lib.routing :as routing]
    [leihs.borrow.client.routes :as routes]))

; is kicked off from router when this view is loaded
(rf/reg-event-fx ::routes/pools-index
                 (fn [_ _]
                   {:dispatch [::current-user/fetch]}))

(defn pool-line [pool]
  [:<> (:name pool) [badge pool]])

(defn pools-list [pools]
  [:ul.list-group
   (doall
     (for [pool pools]
       [:a.list-group-item.d-flex.justify-content-between.align-items-center
        {:key (:id pool),
         :href (routing/path-for ::routes/pools-show :pool-id (:id pool))}
        [pool-line pool]]))])

(defn view []
  (let [pools @(rf/subscribe [::current-user/pools])
        is-loading? (not pools)]
    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]
       ; errors [ui/error-view errors]
       :else
       [:<>
        [:header.mb-3
         [:h1.text-3xl.font-extrabold.leading-none "Pools"]]

        (when-not (empty? pools)
          [:div.mt-3
           [pools-list pools ""]])])]))
