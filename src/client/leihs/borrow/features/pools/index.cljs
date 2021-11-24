(ns leihs.borrow.features.pools.index
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [re-frame.std-interceptors :refer [path]]
   [shadow.resource :as rc]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.pools.core :refer [badge]]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-event-ctx
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [t]]
   [leihs.borrow.client.routes :as routes]
   ["/leihs-ui-client-side-external-react" :as UI]))

; is kicked off from router when this view is loaded
(reg-event-ctx ::routes/inventory-pools-index
               (fn-traced [ctx _] ctx))

(defn pool-line [pool]
  [:<> (:name pool) [badge pool]])

(defn pools-list [pools]
  [:ul.list-group
   (doall
    (for [pool pools]
      [:a.list-group-item.d-flex.justify-content-between.align-items-center
       {:key (:id pool)
        :href (routing/path-for ::routes/inventory-pools-show
                                :inventory-pool-id
                                (:id pool))}
       [pool-line pool]]))])

(defn view []
  (let [pools @(subscribe [::current-user/pools])
        is-loading? (not pools)]
    [:<>
     [:> UI/Components.Design.PageLayout.Header
      {:title (t :borrow.pools/title)}]

     (cond
       is-loading? [ui/loading]
       ; errors [ui/error-view errors]
       :else
       [:<>
        (when-not (empty? pools)
          [:div.mt-3
           [pools-list pools ""]])])]))
