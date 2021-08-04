;; NOTE: not used in current design spec (all categories are on the homepage)

;; (ns leihs.borrow.features.categories.index
;;   (:require
;;     [day8.re-frame.tracing :refer-macros [fn-traced]]
;;     #_[reagent.core :as reagent]
;;     [re-frame.core :as rf]
;;     [re-graph.core :as re-graph]
;;     [shadow.resource :as rc]
;;     #_[clojure.string :refer [join split replace-first]]
;;     #_[leihs.borrow.features.models.core :as models]
;;     #_[leihs.borrow.lib.routing :as routing]
;;     #_[leihs.borrow.lib.pagination :as pagination]
;;     [leihs.borrow.lib.re-frame :refer [reg-event-fx
;;                                        reg-event-db
;;                                        reg-sub
;;                                        reg-fx
;;                                        subscribe
;;                                        dispatch]]
;;     [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
;;     [leihs.borrow.lib.localstorage :as ls]
;;     [leihs.borrow.client.routes :as routes]
;;     [leihs.borrow.features.categories.core :as categories]
;;     ["/leihs-ui-client-side-external-react" :as UI]))

;; (set-default-translate-path :borrow.categories)

;; ; is kicked off from router when this view is loaded
;; (reg-event-fx ::routes/categories-index
;;               categories/dispatch-fetch-index-handler)

;; (defn view []
;;   (fn []
;;     (let [cats @(subscribe [::categories/categories-index])]
;;       [:> UI/Components.AppLayout.Page
;;        {:title (t :title)}

;;        (categories/categories-list cats)])))
