(ns leihs.borrow.html
  (:require [leihs.core.http-cache-buster2 :as cache-buster]
            [leihs.core.json :refer [to-json]]
            [leihs.core.ssr :as ssr]
            [leihs.core.url.core :as url]
            [clojure.tools.logging :as log]
            [hiccup.page :refer [html5 include-js]]
            [ring.util.response :refer [resource-response content-type status]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]

   (hiccup.page/include-css (cache-buster/cache-busted-path "/app/borrow/css/tailwind.min.css"))
   (hiccup.page/include-css (cache-buster/cache-busted-path "/app/borrow/css/base-styles.css"))])

(defn not-found-handler [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html5
          (head)
          [:body
           [:div.container-fluid
            [:h1.text-danger "Error 404 - Not Found"]]])})

(defn html-handler [_request]
  {:headers {"Content-Type" "text/html"}
   :body (html5
          (head)
          [:body.font-sans
           [:div#app
            [:noscript
             [:div.p-4.font-mono
              {:style "height: 100vh; text-align: center;"}
              [:h1.pt-4.pb-4 "Leihs New Borrow"]
              [:p.italic.font-black.text-red-800 "This application requires JavaScript."]]]
            [:pre.font-mono.text-center {:style "line-height: 100vh"}
             "loading…"]]]
          #_(hiccup.page/include-js (cache-buster/cache-busted-path
                                     "/borrow/leihs-shared-bundle.js"))
          (hiccup.page/include-js
           (cache-buster/cache-busted-path "/app/borrow/js/app.js")))})


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
