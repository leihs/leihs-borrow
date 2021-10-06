(ns leihs.borrow.features.home-page.core
  (:require
   [clojure.string :as string]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   #_[re-graph.core :as re-graph]
   #_[shadow.resource :as rc]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path with-translate-path]]
   [leihs.borrow.lib.helpers :refer [log spy]]
   [leihs.borrow.lib.filters :as filters]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.features.filter-modal.core :refer [filter-comp default-dispatch-fn]]
   [leihs.borrow.features.models.core :as models]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.categories.core :as categories]
   [leihs.core.core :refer [remove-nils presence]]
   ["date-fns" :as date-fns]
   ["date-fns/locale" :as locale]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.home-page)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/home
 (fn-traced [_ [_ {:keys [query-params]}]]
   {:dispatch-n (list [::filters/init]
                      [::filters/set-multiple query-params]
                      [::categories/fetch-index 4])}))

(defn view []
  (let [modal-shown? (r/atom false)]
    (fn []
      (let [cats @(subscribe [::categories/categories-index])]
        [:> UI/Components.AppLayout.Page
         [:> UI/Components.Design.PageLayout.Header {:title (t :catalog)}
          [filter-comp default-dispatch-fn]]
         [:> UI/Components.Design.Stack
          [:> UI/Components.Design.Section {:title (t :!borrow.categories.title)}
           (categories/categories-list cats)]]]))))
