(ns leihs.borrow.html
  (:require [leihs.core.http-cache-buster2 :as cache-buster]
            #_[leihs.core.json :refer [to-json]]
            #_[leihs.core.ssr :as ssr]
            #_[leihs.core.url.core :as url]
            #_[clojure.tools.logging :as log]
            [hiccup.page :refer [html5 #_include-js]]
            #_[ring.util.response :refer [resource-response content-type status]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]

   (hiccup.page/include-css (cache-buster/cache-busted-path "/borrow/css/borrow-theme/borrow-theme.css"))])

(defn not-found-handler [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html5
          (head)
          [:body
           [:div.p-4.text-center {:style "height: 100vh"}
            [:div.mb-4.mt-5
             "Error 404 - Not Found"]
            [:div.mb-4
             [:a.decorate-links.fw-bold {:href "/borrow/"}
              "Leihs Home"]]]])})

(defn html-handler [_request]
  {:headers {"Content-Type" "text/html"}
   :body (html5
          (head)
          [:body
           [:div#app
            [:noscript
             [:div.p-4.text-center {:style "height: 100vh;"}
              [:h1 "Leihs Borrow"]
              [:p "This application requires JavaScript."]]]
            [:div.p-4.text-center {:style "line-height: 100vh"}
             "Loading..."]]]

          (hiccup.page/include-js
           (cache-buster/cache-busted-path "/borrow/js/main.js")))})


;#### debug ###################################################################
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
