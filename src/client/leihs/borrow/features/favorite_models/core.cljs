(ns leihs.borrow.features.favorite-models.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.lib.pagination :as pagination]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   [leihs.borrow.features.models.filter-modal :as filter-modal]
   ["/leihs-ui-client-side-external-react" :as UI]
   [leihs.borrow.features.models.core :as models]))

(set-default-translate-path :borrow.favorite-models)

(def EXTRA-VARS {:isFavorited true})

;-; EVENTS 
(reg-event-fx
 ::routes/models-favorites
 (fn-traced [{:keys [db]} _]
   (let [filter-opts (::filter-modal/options db)]
     {:dispatch [::models/get-models filter-opts EXTRA-VARS]})))

(reg-event-fx
 ::clear
 (fn-traced [{:keys [db]} _]
   {:dispatch-n (list [::filter-modal/clear-options]
                      [::models/clear-data]
                      [:routing/navigate [::routes/models-favorites]]
                      [::models/get-models nil EXTRA-VARS])}))

(defn view []
  (let [filter-opts @(subscribe [::filter-modal/options])
        cache-key @(subscribe [::models/cache-key filter-opts EXTRA-VARS])
        models @(subscribe [::models/edges cache-key])]
    [:<>

     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)}]

     [:<>
      (cond
        (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
        (empty? models) [:p.p-6.w-full.text-center (t :!borrow.pagination/nothing-found)]
        :else [:> UI/Components.Design.Section {:title (t :items)}
               [models/models-list models]
               [models/load-more cache-key EXTRA-VARS]])]]))
