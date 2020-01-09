(ns leihs.borrow.client.features.categories.show
  (:require-macros [leihs.borrow.client.lib.macros :refer [spy]])
  (:require
    #_[reagent.core :as reagent]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [clojure.string :refer [join split replace-first]]
    #_[leihs.borrow.client.features.search-models.core :as search-models]
    [leihs.borrow.client.lib.routing :as routing]
    [leihs.borrow.client.lib.pagination :as pagination]
    [leihs.borrow.client.components :as ui]
    [leihs.borrow.client.routes :as routes]
    #_[leihs.borrow.client.components :as ui]))

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

(rf/reg-event-db
  ::on-fetched-data
  (fn [db [_ category-id {:keys [data errors]}]]
    (-> db
        (update-in , [:categories category-id] (fnil identity {}))
        (assoc-in , [:categories category-id :errors] errors)
        (assoc-in , [:categories category-id :data] data))))

(rf/reg-sub
 ::category-data
 (fn [db [_ id]]
   (get-in db [:categories id])))

(defn category-grid-item [category]
  (let [href (routing/path-for ::routes/categories-show
                               :categories-path (:id category))]
    [:div.ui-category-grid-item.max-w-sm.rounded.overflow-hidden.bg-white.px-2.mb-3
     {:style {:opacity 1}}
     [ui/image-square-thumb (get-in category [:images 0]) href]
     [:div.mx-0.mt-1.leading-snug
      [:a {:href href}
       [:span.block.truncate.font-bold (:label category)]]]]))

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

(defn model-grid-item [model]
  (let [routing @(rf/subscribe [:routing/routing])
        params (get-in routing [:bidi-match :query-params])
        max-quant (:availableQuantityInDateRange model)
        available? (> max-quant 0)
        model-show-params {:end (:end-date params) :start (:start-date params) :maxQuantity max-quant}
        href (routing/path-for ::routes/models-show
                               :model-id (:id model)
                               :query-params model-show-params)]
    [:div.ui-model-grid-item.max-w-sm.rounded.overflow-hidden.bg-white.px-2.mb-3
     {:style {:opacity 1 #_(if available? 1 0.35)}}
     [ui/image-square-thumb (get-in model [:images 0]) href]
     [:div.mx-0.mt-1.leading-snug
      [:a {:href href}
       [:span.block.truncate.font-bold (:name model)]
       [:span.block.truncate (:manufacturer model)]]]]))

(defn products-list [models]
  (let
   [debug? @(rf/subscribe [:is-debug?])]
    [:div.mx-1.mt-2
     [:div.w-full.px-0
      [:div.ui-models-list.flex.flex-wrap
       (doall
        (for [m models]
          (let [model (:node m)]
            [:div {:class "w-1/2 min-h-16" :key (:id model)}
             [model-grid-item model]])))]]
     (when debug? [:p (pr-str @(rf/subscribe [::search-results]))])]))

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
        is-loading? (not (or category errors))]

    [:<>
     (cond
       is-loading? [:div [:div [ui/spinner-clock]] [:pre "loading category" [:samp category-id] "…"]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header.mb-4.mx-3.mt-4
         [:h1.text-3xl.font-extrabold.leading-none
          (:name category)]

         (if parent-id
           [:span.mt-2.text-color-muted.text-sm
            [:a {:href (str (routing/path-for ::routes/categories-show
                                              :categories-path (join "/" prev-ids)))} "← back"]])]

        [:ul.font-semibold.mx-3.mb-4
         (doall
          (for [{:keys [id] :as child} children]
            [:<> {:key id}
              [:li.inline-block.mb-2.mr-1
              [:a.border.rounded.py-1.px-2.mb-1
                {:class "text-gray-800 bg-content border-gray-800 hover:bg-gray-200 Xhover:border-transparent"
                :href (-> js/window .-location .-pathname (str "/" id))}
                (:name child)]]]))]

        #_[:p.debug (pr-str category)]

        [products-list models]
        [:hr]
        [:div.p-3.text-center.mx-3
         [:button.border.border-black.p-2.rounded
          {:on-click #(rf/dispatch [::pagination/get-more
                                    query
                                    {:categoryId category-id :parentId parent-id}
                                    [:categories category-id :data :category :models]
                                    [:category :models]])}
          "LOAD MORE"]]])]))
