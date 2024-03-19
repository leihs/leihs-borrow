(ns leihs.borrow.features.categories.show
  (:require ["/borrow-ui" :as UI]
            [cemerick.url]
            [clojure.string :refer [split]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [leihs.borrow.client.routes :as routes]
            [leihs.borrow.components :as ui]
            [leihs.borrow.features.categories.core :as categories]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.features.models.core :as models]
            [leihs.borrow.features.models.filter-modal :refer [filter-comp] :as filter-modal]
            [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db
                                               reg-event-fx reg-sub subscribe]]
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.lib.translate :refer [set-default-translate-path t]]
            [leihs.core.core :refer [dissoc-in]]
            [re-graph.core :as re-graph]
            [reagent.core :as r]
            [shadow.resource :as rc]))

(set-default-translate-path :borrow.categories)

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
        ancestory-ids (butlast cat-ids)
        ancestors @(subscribe [::ancestors ancestory-ids])
        {:keys [children] :as category} @(subscribe [::category-data category-id])
        errors @(subscribe [::errors category-id])
        categories-loaded (and category (every? some? ancestors))
        model-filters @(subscribe [::filter-modal/options])
        extra-search-args {:categoryId category-id}
        child-cats-collapsed? @(subscribe [::child-cats-collapsed? category-id])]
    [:<>
     (cond
       (and (not errors) (not categories-loaded))
       [:> UI/Components.Design.PageLayout.ContentContainer [ui/loading]]

       (and errors (not categories-loaded))
       [:> UI/Components.Design.PageLayout.ContentContainer [ui/error-view errors]]

       :else
       [:<>
        [:div.row
         [:div.col-md-3.d-none.d-md-block]
         [:div.col-md-9

          ; category breadcrumbs for screens below `md` breakpoint
          [:div.d-md-none
           (categories/category-breadcrumbs ancestors model-filters)]

          [:> UI/Components.Design.PageLayout.Header
           {:title (:name category)}]

          (when errors [ui/error-view errors])]]

        (if has-any-reservable-item
          [:div.row
           [:div.col-md-3.d-none.d-md-block

            ; category navigation and breadcrumbs for md+ screens
            [:div {:style {:margin-top "-8px"}}
             (categories/sub-categories-list-menu ancestors category children model-filters)]]

           [:div.col-md-9

            [:> UI/Components.Design.PageLayout.ContentContainer
             [:> UI/Components.Design.Stack {:space 4}

              [:div.text-center
               [filter-comp
                #(dispatch [:routing/navigate
                            [::routes/categories-show
                             {:categories-path categories-path :query-params %}]])]]

              ; category navigation for screens below `md` breakpoint
              (when-not (empty? children)
                [:> UI/Components.Design.Section.Controlled
                 {:title (t :sub-categories)
                  :collapsible true
                  :collapsed child-cats-collapsed?
                  :class (str "d-md-none " (when (not child-cats-collapsed?) "mb-5"))
                  :on-toggle-collapse #(dispatch [::set-child-cats-collapsed category-id %])}
                 (categories/sub-categories-list ancestory-ids category children model-filters)])

              ; models
              [:> UI/Components.Design.Section
               {:title (r/as-element [:span.d-md-none {:key "key"} (t :items)]) :collapsible false}
               [models/search-results extra-search-args]]]]]]

          ; else (does not have any reservable item)
          [:> UI/Components.Design.PageLayout.ContentContainer
           [:> UI/Components.Design.Stack {:space 4 :class "text-center"}
            [:> UI/Components.Design.Warning {:class "fs-2"} (t :!borrow.catalog.no-reservable-items)]
            [:a.decorate-links {:href (routing/path-for ::routes/inventory-pools-index)}
             (t :!borrow.catalog.check-available-pools)]]])])]))
