(ns leihs.borrow.testing.step-2
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

(reg-event-fx ::routes/testing-step-2
              (fn-traced [& _]))

(defn view []
  (let [running-mutations-ids @(subscribe [::requests/running-mutations-ids])
        running-requests @(subscribe [::requests/running])]
    [:section.mx-3.my-4
     [:<>
      [:header.mb-3
       [:h1 "Step 2"]
       [:p.mt-2.small
        [:a {:href (routing/path-for ::routes/testing-step-1)} "<- Step 1"]]]
      [:div "running-mutations-ids:"]
      [:pre#mutation-ids.small {:style {:white-space :pre-wrap}}
       (pp (or running-mutations-ids []))]
      [:div "running-requests:"]
      [:pre#requests.very-small {:style {:white-space :pre-wrap}}
       (pp (or running-requests {}))]]]))

