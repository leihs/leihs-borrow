(ns leihs.borrow.client.features.categories.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.client.features.search-models.core :as search-models]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   ["/leihs-ui-client-side" :as UI]
   #_[leihs.borrow.client.components :as ui]))

(rf/reg-event-fx
 ::fetch-index
 (fn [_ [_ _how-many]]
   {:dispatch [::re-graph/query
               (rc/inline "leihs/borrow/client/features/categories/getCategories.gql")
               {} #_{:count how-many}
               [::on-fetched-categories-index]]}))

(rf/reg-event-fx
 ::on-fetched-categories-index
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:categories :index] (get-in data [:categories]))})))

(rf/reg-sub 
 ::categories-index
 (fn [db] (get-in db [:categories :index])))

(defn categories-list [categories]
  (let [list
        (doall
         (for [category categories]
           {:id (:id category)
            :href (routing/path-for ::routes/categories-show
                                    :categories-path (:id category))
            :caption (:name category)
            :imgSrc (get-in category [:images 0 :imageUrl])}))]
    
    [:div.mx-1.mt-2
     [:> UI/Components.CategoryList {:list list}]]))

