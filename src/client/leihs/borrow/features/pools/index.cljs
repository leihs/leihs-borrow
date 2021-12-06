(ns leihs.borrow.features.pools.index
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [re-frame.std-interceptors :refer [path]]
   [shadow.resource :as rc]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-event-ctx
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.client.routes :as routes]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.pools)

; is kicked off from router when this view is loaded
(reg-event-ctx ::routes/inventory-pools-index
               (fn-traced [ctx _] ctx))

(defn pool-line [pool suspensions]
  (let [href (routing/path-for ::routes/inventory-pools-show
                               :inventory-pool-id
                               (:id pool))]
    [:> UI/Components.Design.ListCard {:href href}

     [:> UI/Components.Design.ListCard.Title
      [:a {:href href :class "stretched-link"} (:name pool)]]

     [:> UI/Components.Design.ListCard.Body
      (cond
        (-> pool :has-reservable-items not)
        [:div  (t :no-reservable-models)]
        (-> pool :maximum-reservation-time)
        [:div  (t :maximum-reservation-time {:days (-> pool :maximum-reservation-time)})])]

     (when (some #(= (get-in % [:inventory-pool :id]) (:id pool)) suspensions)
       [:> UI/Components.Design.ListCard.Foot
        [:> UI/Components.Design.Badge {:colorClassName "bg-danger"}
         (t :access-suspended)]])]))

(defn view []
  (let [pools @(subscribe [::current-user/pools])
        is-loading? (not pools)
        suspensions @(subscribe [::current-user/suspensions])]
    [:<>
     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)}]

     (cond
       is-loading? [ui/loading]
       ; errors [ui/error-view errors]
       :else
       [:<>

        [:> UI/Components.Design.Section {:title (t :available-pools) :collapsible true}
         [:> UI/Components.Design.ListCard.Stack
          (doall
           (for [pool pools]
             [:<> {:key (:id pool)}
              [pool-line pool suspensions]]))]]])]))
