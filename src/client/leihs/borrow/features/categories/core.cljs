(ns leihs.borrow.features.categories.core
  (:require
   [clojure.string :refer [join]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.features.models.core :as models]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.pagination :as pagination]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.models.model-filter :as filter-modal]
   ["/borrow-ui" :as UI]
   [reagent.core :as r]))

(set-default-translate-path :borrow.catalog)

(reg-event-fx
 ::fetch-index
 (fn-traced [{:keys [db]} _]
   {:db (dissoc db ::errors)
    :dispatch [::re-graph/query
               (rc/inline "leihs/borrow/features/categories/getRootCategories.gql")
               {:userId (current-user/get-current-profile-id db)
                :poolIds (when-let [pool-id (-> db ::filter-modal/options :pool-id)]
                           [pool-id])}
               [::on-fetched-categories-index]]}))

(reg-event-fx
 ::on-fetched-categories-index
 (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (assoc-in db [::errors] errors)}
     {:db (assoc-in db [:ls ::data] data)})))

(reg-sub
 ::categories
 (fn [db] (get-in db [:ls ::data :categories])))

(reg-sub
 ::has-templates?
 (fn [db] (->> (get-in db [:ls ::data :inventory-pools])
               (some #(% :has-templates)))))

(reg-sub
 ::errors
 (fn [db] (get-in db [::errors])))

(defn categories-list [model-filters]
  (let [categories @(subscribe [::categories])
        errors @(subscribe [::errors])
        has-templates? @(subscribe [::has-templates?])
        category-items
        (doall
         (for [category (or categories [])]
           {:id (:id category)
            :href (or (:url category)
                      (routing/path-for ::routes/categories-show
                                        :categories-path (:id category)
                                        :query-params model-filters))
            :caption (:name category)
            :imgSrc (get-in category [:images 0 :image-url])}))
        all-items (if has-templates?
                    (conj (into [] category-items)
                          {:id "templates"
                           :href (routing/path-for ::routes/templates-index)
                           :placeholder (r/as-element [:div {:style {:width "5rem"}} [:> UI/Components.Design.Icons.TemplateIcon]])
                           :caption (t :templates)})
                    category-items)]

    [:<>
     (when errors
       [ui/error-view errors])
     [:> UI/Components.Design.SquareImageGrid {:className "ui-category-list" :list all-items}]]))

(defn category-breadcrumbs [ancestors model-filters]
  [:> UI/Components.CategoryBreadcrumbs
   {:ancestorCats (h/camel-case-keys
                   (map-indexed
                    (fn [index cat]
                      (let [path-cats (take (+ index 1) ancestors)]
                        {:id (:id cat)
                         :name (:name cat)
                         :url (routing/path-for
                               ::routes/categories-show
                               :categories-path (join "/" (map :id path-cats))
                               :query-params model-filters)}))
                    ancestors))}])

(defn path-for [id-path model-filters]
  (routing/path-for ::routes/categories-show
                    :categories-path (join "/" id-path)
                    :query-params model-filters))

(defn sub-categories-list [ancestor-ids current-cat categories model-filters]
  [:> UI/Components.Design.ListCard.Stack
   (doall
    (for [cat categories]
      (let [id-path (concat ancestor-ids [(:id current-cat) (:id cat)])]
        [:<> {:key (:id cat)}
         [:> UI/Components.Design.ListCard {:href (path-for id-path model-filters) :one-line true}
          [:> UI/Components.Design.ListCard.Title (:name cat)]]])))])

(defn sub-categories-list-menu [ancestors current-cat children model-filters]
  (let [show-siblings? (boolean (and (-> children empty?)
                                     (-> ancestors seq)))
        cats (if show-siblings? (-> ancestors last :children) children)]
    [:> UI/Components.Design.ListMenu
     ; root
     [:> UI/Components.Design.ListMenu.Link
      {:isBreadcrumb true
       :href (routing/path-for ::routes/home)}
      (t :!borrow.categories.category-root)]

     ; breadcrumbs
     (doall
      (for [[cat path-cats is-last] (map-indexed
                                     (fn [index cat]
                                       [cat
                                        (take (+ index 1) ancestors)
                                        (= (+ index 1) (count ancestors))])
                                     ancestors)]
        [:<> {:key (:id cat)}
         [:> UI/Components.Design.ListMenu.Link
          {:isBreadcrumb true
           :isSelected (and show-siblings? is-last)
           :href (path-for (map :id path-cats) model-filters)
           :style (when (and show-siblings? is-last) {:margin-bottom "9px"})}
          (:name cat)]]))

     ; current
     (when (not show-siblings?)
       [:<> {:key (:id current-cat)}
        [:> UI/Components.Design.ListMenu.CurrentItem
         (:name current-cat)]])

     ; children or siblings
     (doall
      (for [cat cats]
        (let [id-path (concat (map :id ancestors)
                              (when (not show-siblings?) [(:id current-cat)])
                              [(:id cat)])
              has-children? (-> cat :children seq boolean)]
          [:<> {:key (:id cat)}
           [:> UI/Components.Design.ListMenu.Link
            {:href (path-for id-path model-filters)
             :isSelected (= (:id cat) (:id current-cat))
             :hasChildren has-children?}
            (:name cat)]])))]))