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
   [leihs.borrow.lib.localstorage :as ls]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.pagination :as pagination]
   [leihs.borrow.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.models.filter-modal :as filter-modal]
   ["/leihs-ui-client-side-external-react" :as UI]
   #_[leihs.borrow.components :as ui]))

(def dispatch-fetch-index-handler
  (fn-traced [{:keys [db]} _]
    {:dispatch [::re-graph/query
                (rc/inline "leihs/borrow/features/categories/getRootCategories.gql")
                {:userId (current-user/get-current-profile-id db)
                 :poolIds (when-let [pool-id (-> db ::filter-modal/options :pool-id)]
                            [pool-id])}
                [::on-fetched-categories-index]]}))

(reg-event-fx ::fetch-index dispatch-fetch-index-handler)

(reg-event-fx
 ::on-fetched-categories-index
 (fn-traced [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (assoc-in db [:ls ::data] (get-in data [:categories]))})))

(reg-sub
 ::categories-index
 (fn [db] (get-in db [:ls ::data])))

(defn categories-list [categories]
  (let [list
        (doall
         (for [category categories]
           {:id (:id category)
            :href (or (:url category)
                      (routing/path-for ::routes/categories-show
                                        :categories-path (:id category)))
            :caption (:name category)
            :imgSrc (get-in category [:images 0 :image-url])}))]

    [:> UI/Components.Design.SquareImageGrid {:className "ui-category-list" :list list}]))

