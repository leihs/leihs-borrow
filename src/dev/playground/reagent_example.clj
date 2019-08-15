(ns playground.reagent-example
  (:require [leihs.core.http-cache-buster2 :refer [cache-busted-path]]
            [hiccup.page :refer [html5 include-js include-css]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
   (include-css "/borrow/css/playground/reagent-example.css")])

(defn handler [request]
  {:headers {"Content-Type" "text/html"}
   :body (html5
           (head)
           [:body#app
            (include-js "/borrow/js/playground/reagent-example.js")])})

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
