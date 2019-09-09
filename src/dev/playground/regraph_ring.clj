(ns playground.regraph-ring
  (:require [leihs.core.http-cache-buster2 :refer [cache-busted-path]]
            [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.element :refer [javascript-tag]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]])

(defn handler [request]
  {:headers {"Content-Type" "text/html"}
   :body (html5
           (head)
           [:body
            [:div#app]
            (include-js "/borrow/js/playground.js")
            (javascript-tag "playground.regraph_ring.run();")])})

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)

