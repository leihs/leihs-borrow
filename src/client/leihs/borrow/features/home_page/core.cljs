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
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.features.models.filter-modal :refer [filter-comp default-dispatch-fn]
    :as filter-modal]
   [leihs.borrow.features.models.core :as models]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.categories.core :as categories]
   [leihs.core.core :refer [remove-nils presence]]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.catalog) ; ("catalog" because it is currently the only feature on the home page)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/home
 (fn-traced [_ _]
   {:dispatch [::categories/fetch-index 4]}))

(reg-sub ::has-any-reservable-item
         :<- [::current-user/current-profile]
         (fn [profile _]
           (->> profile :inventory-pools (filter #(:has-reservable-items %)) first boolean)))

(defn view []
  (fn []
    (let [has-any-reservable-item @(subscribe [::has-any-reservable-item])]
      (if has-any-reservable-item
        [:<>
         [:> UI/Components.Design.PageLayout.Header {:title (t :title)}
          [:div.pt-2 [filter-comp default-dispatch-fn]]]
         [:> UI/Components.Design.Stack
          [:> UI/Components.Design.Section {:title (t :categories)}
           (categories/categories-list {})]]]
        ; else
        [:<>
         [:> UI/Components.Design.PageLayout.Header {:title (t :title)}]
         [:> UI/Components.Design.Stack {:space 4 :class "text-center"}
          [:> UI/Components.Design.Warning {:class "fs-2"} (t :no-reservable-items)]
          [:a.decorate-links {:href (routing/path-for ::routes/inventory-pools-index)}
           (t :check-available-pools)]]]))))
