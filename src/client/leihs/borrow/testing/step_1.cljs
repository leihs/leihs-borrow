(ns leihs.borrow.testing.step-1
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :as rf]
    [re-graph.core :as re-graph]
    [shadow.resource :as rc]
    [leihs.borrow.components :as ui]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch]]
    [leihs.borrow.lib.helpers :refer [spy log]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.features.current-user.core :as current-user]))

(reg-event-fx
  ::routes/testing-step-1
  (fn-traced [{:keys [db]} [_ args]]
    (log "foo")))

(defn view []
  (let [routing @(subscribe [:routing/routing])]
    [:section.mx-3.my-4
     [:<>
        [:header.mb-3
         [:h1.text-3xl.font-extrabold.leading-tight "Step 1"]]
        [:div.flex-auto.w-1_2
         [:button.px-4.py-2.w-100.rounded-lg.bg-content-inverse.text-color-content-inverse.font-semibold.text-lg
          {:on-click #(log "apply")}
          "Apply"]]]]))
