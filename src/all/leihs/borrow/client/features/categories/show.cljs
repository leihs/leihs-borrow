(ns leihs.borrow.client.features.categories.show
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [re-frame.std-interceptors :refer [path]]
   [clojure.string :refer [join split #_replace-first]]
   [leihs.borrow.client.lib.localstorage :as ls]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.features.search-models.core :as search-models]))

(def query
  (rc/inline "leihs/borrow/client/features/categories/getCategoryShow.gql"))

; is kicked off from router when this view is loaded
(rf/reg-event-fx
  ::routes/categories-show
  (fn [_ [_ args]]
    (let [categories-path (get-in args [:route-params :categories-path])
          categories (split categories-path #"/")
          category-id (last categories)
          parent-id (last (butlast categories))]
      {:dispatch [::re-graph/query
                  query
                  {:categoryId category-id
                   :parentId parent-id}
                  [::on-fetched-data category-id]]})))

(ls/reg-event-db
  ::on-fetched-data
  [(path :ls ::categories :index)]
  (fn [cs [_ category-id {:keys [data errors]}]]
    (-> cs
        (update category-id (fnil identity {}))
        (assoc-in [category-id :errors] errors)
        (assoc-in [category-id :data] data))))

(rf/reg-sub
  ::category-data
  (fn [db [_ id]] (get-in db [:ls ::categories :index id])))

(defn view []
  (let [routing @(rf/subscribe [:routing/routing])
        cat-ids (-> routing
                    (get-in [:bidi-match :route-params :categories-path])
                    (split #"/"))
        category-id (last cat-ids)
        prev-ids (butlast cat-ids)
        parent-id (first prev-ids)
        fetched @(rf/subscribe [::category-data category-id])
        {:keys [children] {models :edges} :models :as category} (get-in fetched [:data :category])
        errors (:errors fetched)
        is-loading? (not category)]

    [:<>
     (cond
       is-loading? [:div.p-4.text-center [:div.p-2 [ui/spinner-clock]] [:pre "loading category" [:samp category-id] "…"]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header.mb-4.mx-3.mt-4
         [:h1.text-3xl.font-extrabold.leading-none
          (:name category)]

         (when parent-id
           [:span.mt-2.text-color-muted.text-sm
            [:a.text-color-content-link {:href (str (routing/path-for ::routes/categories-show
                                              :categories-path (join "/" prev-ids)))} "← back"]])]

        [:ul.font-semibold.mx-3.mb-4
         (doall
          (for [{:keys [id] :as child} children]
            [:<> {:key id}
              [:li.d-inline-block.mb-2.mr-1
              [:a.text-color-content.border.rounded.py-1.px-2.mb-1
                {:class "text-gray-800 bg-content border-gray-800 hover:bg-gray-200"
                :href (-> js/window .-location .-pathname (str "/" id))}
                (:name child)]]]))]

        [search-models/models-list models]
        [:hr]
        [:div.p-3.text-center.mx-3
         [:button.border.border-black.p-2.rounded
          {:on-click #(rf/dispatch [::pagination/get-more
                                    query
                                    {:categoryId category-id :parentId parent-id}
                                    [:categories category-id :data :category :models]
                                    [:category :models]])}
          "LOAD MORE"]]])]))
