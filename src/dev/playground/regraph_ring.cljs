(ns playground.regraph-ring
  (:refer-clojure :exclude [update])
  (:require [cljs-http.client :as http]
            [reagent.core :as reagent]
            [reagent.cookies :as cookies]
            [re-graph.core :as re-graph]
            [re-graph.internals :as re-graph-internals]
            [re-frame.core :as re-frame]))


(defn setup []
  (def query "{ ping (count: 4 message: \"test\") { message }}")

 (defn dispatch-unsubscribe []
   (re-frame/dispatch [::re-graph/unsubscribe ::ping]))

 (re-frame/dispatch
   [::re-graph/init
    {:ws-url "ws://localhost:3250/graphql-ws"
     :http-url "http://localhost:3250/graphql"}])

 (re-frame/reg-event-db
   ::on-message
   ; [(when ^boolean goog.DEBUG re-frame/debug)]
   (fn [db [_ payload]]
     (assoc db ::result payload)))

 (re-frame/reg-sub ::query-result
                   (fn [db _]
                     (-> db
                         ::result
                         clj->js
                         js/JSON.stringify)))

 (defn dispatch-subscribe []
   (re-frame/dispatch [::re-graph/subscribe
                       ::ping
                       query
                       {}
                       [::on-message]]))

  (defn ui []
    (let [result (re-frame/subscribe [::query-result])]
      [:div
       [:h1 "Re-graph example"]
       [:span
        [:button {:on-click dispatch-subscribe} "subscribe"]
        [:button {:on-click dispatch-unsubscribe} "unsubscribe"]]
       [:br]
       [:br]
       [:div @result]])))

(defn ^:export run []
  (setup)
  (reagent/render [ui] (js/document.getElementById "app")))

(defn ^:export update []
  (re-graph/destroy)
  (setup)
  (reagent/render [ui] (js/document.getElementById "app")))
