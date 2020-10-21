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
    [leihs.borrow.lib.helpers :refer [spy log pp]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.lib.filters :as filters]
    [leihs.borrow.lib.requests :as requests]
    [leihs.borrow.features.current-user.core :as current-user]))

(def sleep-secs 5)

(reg-event-fx
  ::routes/testing-step-1
  (fn-traced [& _]
    {:dispatch [::query]}))

(reg-event-fx
  ::query
  (fn-traced [_ [_ sleep-secs]]
    {:dispatch [::re-graph/query
                (str "query { testing(sleepSecs: " sleep-secs ")}")
                nil
                [::on-query-completed]]}))

(reg-event-fx
  ::on-query-completed
  (fn-traced [& _] (log "query completed")))

(reg-event-fx
  ::mutation
  (fn-traced [_ [_ sleep-secs]]
    {:dispatch [::re-graph/mutate
                (str "mutation { testing(sleepSecs: " sleep-secs ")}")
                nil
                [::on-mutation-completed]]}))

(reg-event-fx
  ::on-mutation-completed
  (fn-traced [& _] (log "mutation completed")))

(defn view []
  (let [routing @(subscribe [:routing/routing])
        running-mutations-ids @(subscribe [::requests/running-mutations-ids])
        running-requests @(subscribe [::requests/running])]
    [:section.mx-3.my-4
     [:<>
      [:header.mb-3
       [:h1.text-3xl.font-extrabold.leading-tight "Step 1"]]
      [:div "running-mutations-ids:"]
      [:pre.text-xs {:style {:white-space :pre-wrap}}
       (pp (or running-mutations-ids []))]
      [:div "running-requests:"]
      [:pre.text-xs {:style {:white-space :pre-wrap}}
       (pp (or running-requests {}))]
      [:br]
      [:div.flex-auto.w-1_2
       [:button.px-4.py-2.w-100.rounded-lg.bg-success.text-color-content-inverse.font-semibold.text-lg
        {:on-click #(dispatch [::query sleep-secs])}
        "query"]]
      [:br]
      [:div.flex-auto.w-1_2
       [:button.px-4.py-2.w-100.rounded-lg.bg-danger.text-color-content-inverse.font-semibold.text-lg
        {:on-click #(dispatch [::mutation sleep-secs])}
        "mutate"]]]]))
