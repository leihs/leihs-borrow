(ns leihs.borrow.features.categories.show
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [re-frame.std-interceptors :refer [path]]
   [clojure.string :refer [join split #_replace-first]]
   [cemerick.url]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.helpers :refer [spy]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.pagination :as pagination]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.helpers :as h]
   ["/leihs-ui-client-side-external-react" :as UI]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.models.filter-modal :refer [filter-comp] :as filter-modal]
   [leihs.borrow.features.models.core :as models]
   [leihs.borrow.features.categories.core :as categories]
   [leihs.core.core :refer [dissoc-in]]))

(set-default-translate-path :borrow.categories)

(def query
  (rc/inline "leihs/borrow/features/categories/getCategoryShow.gql"))

(def categories-query
  (rc/inline "leihs/borrow/features/categories/getCategories.gql"))

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/categories-show
 (fn-traced [{:keys [db]} [_ {:keys [query-params] :as args}]]
   (let [categories-path (get-in args [:route-params :categories-path])
         category-ids (split categories-path #"/")
         ancestor-ids (butlast category-ids)
         category-id (last category-ids)
         parent-id (last ancestor-ids)]

     {:db (dissoc-in db [::errors category-id])
      :dispatch-n (list

                   ; We include the actual category itself because the
                   ; backend validates if all categories in the path
                   ; are still among the reservable ones.
                   [::re-graph/query categories-query
                    {:ids category-ids
                     :poolIds (when-let [pool-id (-> db ::filter-modal/options :pool-id)]
                                [pool-id])}
                    [::on-fetched-categories-data category-id]]

                   [::models/get-models query-params {:categoryId category-id}])})))

(reg-event-db
 ::on-fetched-categories-data
 (fn-traced [db [_ category-id {:keys [data errors]}]]
   (if errors
     (assoc-in db [::errors category-id] errors)
     (update-in db [:ls ::data]
                #(apply merge %1 %2)
                (->> data
                     :categories
                     (map #(hash-map (:id %) %)))))))

(reg-event-fx
 ::clear
 (fn-traced [_ [_ nav-args extra-vars]]
   {:dispatch-n (list [::filter-modal/clear-options]
                      [::models/clear-data]
                      [:routing/navigate [::routes/categories-show nav-args]]
                      [::models/get-models extra-vars])}))

(reg-sub ::has-any-reservable-item
         :<- [::current-user/current-profile]
         (fn [profile _]
           (->> profile :inventory-pools (filter #(:has-reservable-items %)) first boolean)))

(reg-sub
 ::ancestors
 (fn [db [_ ids]]
   (map #(get-in db [:ls ::data %]) ids)))

(reg-sub
 ::category-data
 (fn [db [_ id]] (get-in db [:ls ::data id])))

(reg-sub
 ::errors
 (fn [db [_ id]] (get-in db [::errors id])))

(reg-event-db
 ::set-child-cats-collapsed
 (fn [db [_ id collapsed?]]
   (if collapsed?
     (dissoc-in db [:ls ::open-category-ids id])
     (assoc-in db [:ls ::open-category-ids id] true))))

(reg-sub
 ::child-cats-collapsed?
 (fn [db [_ id]]
   (not (get-in db [:ls ::open-category-ids id]))))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        has-any-reservable-item @(subscribe [::has-any-reservable-item])
        categories-path (get-in routing [:bidi-match :route-params :categories-path])
        cat-ids (split categories-path #"/")
        category-id (last cat-ids)
        prev-ids (butlast cat-ids)
        cat-ancestors @(subscribe [::ancestors prev-ids])
        {:keys [children] :as category} @(subscribe [::category-data category-id])
        errors @(subscribe [::errors category-id])
        categories-loaded (and category (every? some? cat-ancestors))
        model-filters @(subscribe [::filter-modal/options])
        extra-search-args {:categoryId category-id}

        ;; FIXME: get current url from router (bidi-match)!
        child-cats  (for [cat children] (merge cat {:url (-> js/window .-location .-pathname
                                                             (str "/" (:id cat) "?" (cemerick.url/map->query model-filters)))}))
        child-cats-collapsed? @(subscribe [::child-cats-collapsed? category-id])]
    [:<>
     (cond
       (and (not errors) (not categories-loaded)) [ui/loading]
       (and errors (not categories-loaded)) [ui/error-view errors]
       :else
       [:<>
        [:> UI/Components.Design.PageLayout.Header
         {:title (:name category)
          :preTitle (when (seq cat-ancestors)
                      (r/as-element
                       [:> UI/Components.CategoryBreadcrumbs
                        {:className "text-center"
                         :ancestorCats (h/camel-case-keys cat-ancestors)
                       ;; FIXME: either add url to all ancestor cats OR extend router to take list of ids (instead of prejoined path)
                         :getPathForCategory (fn [path] (routing/path-for
                                                         ::routes/categories-show
                                                         :categories-path path
                                                         :query-params model-filters))}]))}
         (when has-any-reservable-item
           [:div.pt-2
            [filter-comp
             #(dispatch [:routing/navigate
                         [::routes/categories-show
                          {:categories-path categories-path :query-params %}]])
             #_extra-search-args]])]
        (when errors [ui/error-view errors])
        (if has-any-reservable-item
          [:> UI/Components.Design.Stack {:space 4}
           (when-not (empty? child-cats)
             [:> UI/Components.Design.Section.Controlled
              {:title (t :sub-categories)
               :collapsible true
               :collapsed child-cats-collapsed?
               :class (when (not child-cats-collapsed?) "mb-5")
               :on-toggle-collapse #(dispatch [::set-child-cats-collapsed category-id %])}
              (categories/sub-categories-list child-cats model-filters)])
           [:> UI/Components.Design.Section
            {:title (t :items) :collapsible false}
            [models/search-results extra-search-args]]]
          ; else
          [:> UI/Components.Design.Stack {:space 4 :class "text-center"}
           [:> UI/Components.Design.Warning {:class "fs-2"} (t :!borrow.catalog.no-reservable-items)]
           [:a.decorate-links {:href (routing/path-for ::routes/inventory-pools-index)}
            (t :!borrow.catalog.check-available-pools)]])])]))
