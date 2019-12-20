(ns leihs.borrow.client.features.category-show
  (:require-macros [leihs.borrow.client.macros :refer [spy]])
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [clojure.string :refer [join split replace-first]]
   [leihs.borrow.client.features.search-models :as search-models]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui]))

; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/categories-show
 (fn [_ [_ args]]
   (let [categories-path (get-in args [:route-params :categories-path])
         categories (split categories-path #"/")
         category-id (last categories)
         parent-id (last (butlast categories))]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/client/queries/getCategoryShow.gql")
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

(rf/reg-event-fx
 ::get-more-models
 (fn [{:keys [db]} [_ category-id parent-id]]
   (let [page-info (get-in db [:categories category-id :data :category :models :pageInfo])
         has-next? (get page-info :hasNextPage)
         query-vars {:categoryId category-id
                     :parentId parent-id
                     :afterCursor (get page-info :endCursor)}]
     (when has-next?
       {:dispatch
        [::re-graph/query
         (rc/inline "leihs/borrow/client/queries/getCategoryShow.gql")
         query-vars
         [::on-fetched-more-models]]}))))

(rf/reg-event-fx
 ::on-fetched-more-models
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (let [category-id (get-in data [:category :id])
         more-edges (get-in data [:category :models :edges])
         path-to-models [:categories category-id :data :category :models]]
     (if errors
       {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
       {:db (-> db
                (assoc-in (conj path-to-models :pageInfo)
                          (get-in data [:category :models :pageInfo]))
                (update-in (conj path-to-models :edges)
                           concat
                           more-edges)) }))))

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
    [:section.mx-3.my-4
     (cond
       is-loading? [:div [:div [ui/spinner-clock]] [:pre "loading category" [:samp category-id] "…"]]
       errors [ui/error-view errors]
       :else
       [:<>
        [:header
         [:h1.text-3xl.font-extrabold.leading-none
          (:name category)]]

        (if parent-id
          [:a {:href (str (routing/path-for ::routes/categories-show
                                            :categories-path (join "/" prev-ids)))}
           "<<-"])
        [:ul (doall
               (for [{:keys [id] :as child} children]
                 [:li [:a {:href (-> js/window .-location .-pathname (str "/" id))}
                       (:name child)]]))]

        #_[:p.debug (pr-str category)]

        [products-list models]
        [:hr]
        [:div.p-3.text-center
         [:button.border.border-black.p-2.rounded
          {:on-click #(rf/dispatch [::get-more-models category-id parent-id])}
          "LOAD MORE"]]])]))
