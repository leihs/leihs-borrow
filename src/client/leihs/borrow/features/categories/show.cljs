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

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/categories-show
  (fn-traced [{:keys [db]} [_ {:keys [query-params] :as args}]]
    (let [categories-path (get-in args [:route-params :categories-path])
          categories (split categories-path #"/")
          category-id (last categories)
          parent-id (last (butlast categories))
          user-id (filters/user-id db)]
      {:dispatch-n (list [::filters/set-multiple query-params]
                         [::re-graph/query
                          query
                          {:categoryId category-id
                           :parentId parent-id
                           :poolIds (when-let [pool-id (filters/pool-id db)]
                                      [pool-id])
                           :userId user-id}
                          [::on-fetched-category-data category-id]]
                         [::models/get-models {:categoryId category-id}])})))

(reg-event-db
  ::on-fetched-category-data
  (fn-traced [db [_ category-id {:keys [data errors]}]]
    (-> db
        (update-in [:ls ::data category-id] (fnil identity {}))
        (cond->
          errors
          (assoc-in [::errors category-id] errors))
        (assoc-in [:ls ::data category-id] (:category data)))))

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
     " | "
     (loop [as-reversed (reverse cat-ancestors)
            bc-links []]
       (if (empty? as-reversed)
         bc-links
         (recur (rest as-reversed)
                (let [current-as (reverse as-reversed)
                      href (->> current-as
                                (map :id)
                                (join "/")
                                (routing/path-for ::routes/categories-show
                                                  :categories-path)
                                str)
                      link [:a {:href href}
                            (-> current-as last :name)]]
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
        is-loading? (not category)
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
