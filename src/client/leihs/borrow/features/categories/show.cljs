(ns leihs.borrow.features.categories.show
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require
    #_[reagent.core :as reagent]
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
    [leihs.borrow.lib.localstorage :as ls]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.pagination :as pagination]
    [leihs.borrow.components :as ui]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.features.models.core :as models]))

(def query
  (rc/inline "leihs/borrow/features/categories/getCategoryShow.gql"))

; is kicked off from router when this view is loaded
(reg-event-fx
  ::routes/categories-show
  (fn [_ [_ args]]
    (let [categories-path (get-in args [:route-params :categories-path])
          categories (split categories-path #"/")
          category-id (last categories)
          parent-id (last (butlast categories))]
      {:dispatch-n (list [::re-graph/query
                          query
                          {:categoryId category-id
                           :parentId parent-id}
                          [::on-fetched-category-data category-id]]
                         [::models/get-models {:categoryId category-id}])})))

(reg-event-db
  ::on-fetched-category-data
  [(path :ls)]
  (fn [ls [_ category-id {:keys [data errors]}]]
    (-> ls
        (update-in [::data category-id] (fnil identity {}))
        (cond->
          errors
          (assoc-in [::errors category-id] errors))
        (assoc-in [::data category-id] data))))

(reg-event-fx
  ::clear
  (fn [_ [_ nav-args]]
    {:dispatch-n (list [::filters/clear-current]
                       [::models/clear-results]
                       [:routing/navigate [::routes/categories-show nav-args]])}))

(reg-sub
  ::category-data
  (fn [db [_ id]] (get-in db [:ls ::data id])))

(reg-sub
  ::errors
  (fn [db [_ id]] (get-in db [:ls ::errors id])))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        categories-path (get-in routing [:bidi-match :route-params :categories-path])
        cat-ids (split categories-path #"/")
        category-id (last cat-ids)
        prev-ids (butlast cat-ids)
        parent-id (first prev-ids)
        fetched @(subscribe [::category-data category-id])
        {:keys [children] :as category} (:category fetched)
        errors @(subscribe [::errors category-id])
        is-loading? (not category)
        extra-args {:categoryId category-id}]

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

        [models/search-and-list
         #(dispatch [:routing/navigate
                     [::routes/categories-show 
                      {:categories-path categories-path :query-params %}]])
         #(dispatch [::clear {:categories-path categories-path}])
         extra-args]])]))
