(ns leihs.borrow.features.categories.show
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [re-frame.std-interceptors :refer [path]]
    [clojure.string :refer [join split #_replace-first]]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.helpers :refer [spy]]
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.pagination :as pagination]
    [leihs.borrow.components :as ui]
    ["/leihs-ui-client-side-external-react" :as UI]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.features.models.core :as models]))

(def query
  (rc/inline "leihs/borrow/features/categories/getCategoryShow.gql"))

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
          parent-id (last ancestor-ids)
          user-id (filters/user-id db)]
      {:dispatch-n (list [::filters/set-multiple query-params]

                         ; We include the actual category itself because the
                         ; backend validates if all categories in the path
                         ; are still among the reservable ones.
                         [::re-graph/query categories-query
                          {:ids category-ids
                           :poolIds (when-let [pool-id (filters/pool-id db)]
                                      [pool-id])}
                          [::on-fetched-categories-data]]

                         ; We fetch the actual category again to get the correct
                         ; label in the context of the parent category.
                         [::re-graph/query
                          query
                          {:categoryId category-id :parentId parent-id}
                          [::on-fetched-category-data category-id]]

                         [::models/get-models {:categoryId category-id}])})))

(reg-event-db
  ::on-fetched-categories-data
  (fn-traced [db [_ {:keys [data errors]}]]
    (-> db
        (cond-> errors (assoc-in [::errors] errors))
        (update-in [:ls ::data]
                   #(apply merge %1 %2)
                   (->> data
                        :categories
                        (map #(hash-map (:id %) %)))))))

(reg-event-db
  ::on-fetched-category-data
  (fn-traced [db [_ category-id {:keys [data errors]}]]
    (-> db
        (cond-> errors
          (assoc-in [::errors category-id] errors))
        (update-in [:ls ::data category-id]
                   merge
                   (:category data)))))

(reg-event-fx
  ::clear
  (fn-traced [_ [_ nav-args extra-vars]]
    {:dispatch-n (list [::filters/clear-current]
                       [::models/clear-data]
                       [:routing/navigate [::routes/categories-show nav-args]]
                       [::models/get-models extra-vars])}))

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

(defn breadcrumbs [cat-ancestors]
  [:div#breadcrumbs
   (interpose
     " > "
     (loop [ancs cat-ancestors, bc-links []]
       (if (empty? ancs)
         bc-links
         (recur (butlast ancs)
                (let [href (->> ancs
                                (map :id)
                                (join "/")
                                (routing/path-for ::routes/categories-show
                                                  :categories-path)
                                str)
                      link [:a {:href href}
                            (-> ancs last :name)]]
                  (cons link bc-links))))))])

(defn view []
  (let [routing @(subscribe [:routing/routing])
        categories-path (get-in routing [:bidi-match :route-params :categories-path])
        cat-ids (split categories-path #"/")
        category-id (last cat-ids)
        prev-ids (butlast cat-ids)
        cat-ancestors @(subscribe [::ancestors prev-ids])
        {:keys [children] :as category} @(subscribe [::category-data category-id])
        errors @(subscribe [::errors category-id])
        is-loading? (not (and category
                              (every? some? cat-ancestors)))
        extra-args {:categoryId category-id}]

    [:<>
     (cond
       is-loading? [:div.p-4.text-center
                    [:div.p-2 [ui/spinner-clock]]
                    [:pre "loading category" [:samp category-id] "…"]]
       errors [ui/error-view errors]
       :else
       [:> UI/Components.AppLayout.Page {:title (:name category)}
        (when-not (empty? cat-ancestors)
          [breadcrumbs cat-ancestors])
        [:ul#children.font-semibold.my-3
         (doall
           (for [{:keys [id] :as child} children]
             [:<> {:key id}
              [:li.d-inline-block.mb-2.mr-1
               [:a.text-color-content.border.rounded.py-1.px-2.mb-1
                {:class "text-gray-800 bg-content border-gray-800 hover:bg-gray-200"
                 :href (-> js/window .-location .-pathname (str "/" id))}
                (:name child)]]]))]
        [models/search-and-list
         #(dispatch [:routing/navigate
                     [::routes/categories-show 
                      {:categories-path categories-path :query-params %}]])
         #(dispatch [::clear
                     {:categories-path categories-path}
                     {:categoryId category-id}])
         extra-args]])]))
