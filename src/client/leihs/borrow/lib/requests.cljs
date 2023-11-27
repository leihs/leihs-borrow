(ns leihs.borrow.lib.requests
  (:require
   [day8.re-frame.http-fx] ; Not used but don't delete! It registers an interceptor.
   [re-graph.core :as re-graph]
   [re-frame.core :as rf]
   [leihs.core.core :refer [flip dissoc-in]]
   [leihs.borrow.lib.helpers :refer [spy log]]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch
                                      dispatch-sync]]))

(def re-graph-requests-db-path
  [:re-graph :re-graph.internals/default :http :requests])

(reg-event-fx
 ::abort-running-queries
 (fn [{:keys [db]} _]
   (let [req-ids (as-> db <>
                   (get-in <> re-graph-requests-db-path)
                   (keys <>)
                   (remove (-> db ::running-mutations-ids set) <>))
         events (->> req-ids
                     (map (partial vector ::re-graph/abort)))]
     {:dispatch-n events})))

(reg-sub ::running-mutations-ids
         (fn [db _] (::running-mutations-ids db)))

(reg-sub ::running
         (fn [db _] (get-in db re-graph-requests-db-path)))

(reg-sub ::retry-mutation
         (fn [db _] (::retry-mutation db)))

(rf/reg-global-interceptor
 (rf/->interceptor
  :id :track-mutations-ids
  :after (fn [ctx]
           (let [[ev-id :as ev] (-> ctx :coeffects :event)
                 path [:effects :db ::running-mutations-ids]]
             (case ev-id
               ::re-graph/mutate
               (update-in ctx path conj (-> ctx
                                            :effects
                                            :re-graph.internals/send-http
                                            (nth 1)))
               :re-graph.internals/http-complete
               (update-in ctx path (flip remove) #{(nth ev 2)})
               ctx)))))

(rf/reg-global-interceptor
 (rf/->interceptor
  :id :manage-retry-mutation
  :after (fn [ctx]
           (let [[ev-id :as ev] (-> ctx :coeffects :event)
                 path [:effects :db ::retry-mutation]]
             (case ev-id
               ::re-graph/mutate
               (cond-> ctx
                 (not= (last ev) [:leihs.borrow.features.shopping-cart.timeout/on-refresh])
                 (assoc-in path ev))
               :re-graph.internals/http-complete
               (cond-> ctx
                 (empty? (-> ev last :errors))
                 (dissoc-in path))
               ctx)))))

; =============================================================================================

(defn retry-banner []
  (let [retry-mutation @(subscribe [::retry-mutation])
        running-mutations-ids @(subscribe [::running-mutations-ids])]
    (when (and retry-mutation (empty? running-mutations-ids))
      [:section.p-4.bg-info {:style {:white-space "pre-wrap" :background "blue" :padding "1rem"}}
       [:h1 "RETRY"]
       [:p [:button.border-black.border-2.rounded-full.py-1.px-3
            {:type :button,
             :on-click #(dispatch retry-mutation)}
            "BY CLICKING HERE"]]])))
