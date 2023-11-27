(ns leihs.borrow.testing.step-1
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.helpers :refer [spy log pp]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.requests :as requests]))

(def sleep-secs 5)

(reg-event-fx ::routes/testing-step-1
              (fn-traced [& _]))

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
  (let [running-mutations-ids @(subscribe [::requests/running-mutations-ids])
        running-requests @(subscribe [::requests/running])]
    [:section.mx-3.my-4
     [:<>
      [:header.mb-3
       [:h1 "Step 1"]
       [:p.mt-2.small
        [:a {:href (routing/path-for ::routes/testing-step-2)} "-> Step 2"]]]
      [:div "running-mutations-ids:"]
      [:pre#mutation-ids.very-small {:style {:white-space :pre-wrap}}
       (pp (or running-mutations-ids []))]
      [:div "running-requests:"]
      [:pre#requests.very-small {:style {:white-space :pre-wrap}}
       (pp (or running-requests {}))]
      [:br]
      [:div
       [:button.btn.btn-success.px-4.py-2.w-100
        {:on-click #(dispatch [::query sleep-secs])}
        "query"]]
      [:br]
      [:div
       [:button.btn.btn-danger.px-4.py-2.w-100
        {:on-click #(dispatch [::mutation sleep-secs])}
        "mutate"]]]]))
