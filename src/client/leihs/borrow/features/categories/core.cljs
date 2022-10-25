(ns leihs.borrow.features.categories.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.features.models.core :as models]
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
   [leihs.borrow.features.models.filter-modal :as filter-modal]
   ["/leihs-ui-client-side-external-react" :as UI]
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
                           :placeholder (r/as-element [:div {:style {:width "5rem"}} [:> UI/Components.Design.TemplateIcon]])
                           :caption (t :templates)})
                    category-items)]

    [:<>
     (when errors
       [ui/error-view errors])
     [:> UI/Components.Design.SquareImageGrid {:className "ui-category-list" :list all-items}]]))

(defn sub-categories-list [categories model-filters]
  [:> UI/Components.Design.ListCard.Stack
   (doall
    (for [cat categories]
      (let [href (or (:url cat)
                     (routing/path-for ::routes/categories-show
                                       :categories-path (:id cat)
                                       :query-params model-filters))]
        [:<> {:key (:id cat)}
         [:> UI/Components.Design.ListCard {:href href :one-line true}
          [:> UI/Components.Design.ListCard.Title (:name cat)]]])))])