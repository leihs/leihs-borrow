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

(defn category-grid-item [category]
  (let [href (routing/path-for ::routes/categories-show
                               :categories-path (:id category))]
    [:div.ui-category-grid-item.max-w-sm.rounded.overflow-hidden.bg-white.px-2.mb-3
     {:style {:opacity 1}}
     [ui/image-square-thumb (get-in category [:images 0]) href]
     [:div.mx-0.mt-1.leading-snug
      [:a {:href href}
       [:span.block.truncate.font-bold (:name category)]]]]))

(defn categories-list [categories]
  (let
   [debug? @(rf/subscribe [:is-debug?])]
    [:div.mx-1.mt-2
     [:div.w-full.px-0
      [:div.ui-models-list.flex.flex-wrap
       (doall
        (for [category categories]
          [:div {:class "w-1/2 min-h-16" :key (:id category)}
           [category-grid-item category]]))]]
     (when debug? [:p (pr-str @(rf/subscribe [::search-results]))])]))

