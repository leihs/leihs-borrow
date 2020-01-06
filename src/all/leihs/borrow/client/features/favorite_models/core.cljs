(ns leihs.borrow.client.features.favorite-models.core
  (:require-macros [leihs.borrow.client.macros :refer [spy]])
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.lib.pagination :as pagination]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.client.components :as ui]
   #_[leihs.borrow.client.features.shopping-cart.core :as cart]))

(def query
  (rc/inline "leihs/borrow/client/queries/getFavoriteModels.gql"))

;-; EVENTS 
(rf/reg-event-fx
 ::routes/models-favorites
 (fn [_ _]
   {:dispatch [::re-graph/query
               query
               nil
               [::on-fetched-models-favorites]]}))

(rf/reg-event-fx
 ::on-fetched-models-favorites
 (fn [{:keys [db]} [_ {:keys [data errors]}]]
   (if errors
     {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
     {:db (merge db data)})))

(rf/reg-sub ::favorite-models (fn [db] (:models db)))

(defn model-grid-item [model]
  (let [routing @(rf/subscribe [:routing/routing])
        href (routing/path-for ::routes/models-show :model-id (:id model))]
    [:div.ui-model-grid-item.max-w-sm.rounded.overflow-hidden.bg-white.px-2.mb-3
     {:style {:opacity 1}}
     [ui/image-square-thumb (get-in model [:images 0]) href]
     [:div.mx-0.mt-1.leading-snug
      [:a {:href href}
       [:span.block.truncate.font-bold (:name model)]
       [:span.block.truncate (:manufacturer model)]]]]))

(defn products-list [models]
  (let [debug? @(rf/subscribe [:is-debug?])]
    [:div.mx-1.mt-2
     [:div.w-full.px-0
      [:div.ui-models-list.flex.flex-wrap
       (doall
        (for [m models]
          (let [model (:node m)]
            [:div {:class "w-1/2 min-h-16" :key (:id model)}
             (model-grid-item model)])))]]
     (when debug? [:p (pr-str @(rf/subscribe [::favorite-models]))])]))

(defn view []
  (let [models @(rf/subscribe [::favorite-models])]
    [:section.mx-3.my-4
     [:<>
      [:header
       [:h1.text-3xl.font-extrabold.leading-none
        "My favorites"]]
      #_[:p (pr-str models)]
      (cond
        (nil? models) [:p.p-6.w-full.text-center.text-xl [ui/spinner-clock]]
        (empty? models) [:p.p-6.w-full.text-center "nothing found!"]
        :else
        [:<>
         (products-list (:edges models))
         [:hr]
         [:div.p-3.text-center
          [:button.border.border-black.p-2.rounded
           {:on-click #(rf/dispatch [::pagination/get-more
                                     query
                                     {}
                                     [:models]
                                     [:models]])}
           "LOAD MORE"]]])]]))
