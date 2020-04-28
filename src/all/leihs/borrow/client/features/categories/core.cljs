(ns leihs.borrow.client.features.categories.core
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.client.features.search-models.core :as search-models]
   [leihs.borrow.client.lib.localstorage :as ls]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   ["/leihs-ui-client-side" :as UI]
   #_[leihs.borrow.client.components :as ui]))

(def dispatch-fetch-index-handler
  (fn [_ _]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/client/features/categories/getRootCategories.gql")
                {} #_{:count how-many}
                [::on-fetched-categories-index]]}) )

(rf/reg-event-fx ::fetch-index dispatch-fetch-index-handler)

(ls/reg-event-fx
 ::on-fetched-categories-index
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:ls ::categories :index] (get-in data [:categories]))})))

(rf/reg-sub
  ::categories-index
  (fn [db] (get-in db [:ls ::categories :index])))

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

