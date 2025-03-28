(ns leihs.borrow.features.favorite-models.core
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.routing :as routing]
   ["/borrow-ui" :as UI]
   [leihs.borrow.features.models.core :as models]))

(set-default-translate-path :borrow.favorite-models)

; "extra-vars" filter for the models query
(def fav-filter {:isFavorited true})

(reg-event-fx
 ::routes/models-favorites
 (fn-traced [{:keys [db]} _]
   {:dispatch [::models/get-models {} fav-filter]}))

(defn view []
  (let [cache-key @(subscribe [::models/cache-key {} fav-filter])
        models @(subscribe [::models/edges cache-key])
        errors @(subscribe [::models/errors cache-key])]
    [:> UI/Components.Design.PageLayout.ContentContainer

     [:> UI/Components.Design.PageLayout.Header
      {:title (t :title)}]

     [:<>
      (cond
        (not (or models errors)) [ui/loading]
        errors [ui/error-view errors]
        (empty? models) [:div.d-grid.gap-4.text-center
                         (t :no-favorites)
                         [:a.decorate-links {:href (routing/path-for ::routes/home)}
                          (t :go-to-catalog)]]
        :else [:> UI/Components.Design.Section {:title (t :items)}
               [models/models-list models]
               [models/load-more cache-key fav-filter]])]]))
