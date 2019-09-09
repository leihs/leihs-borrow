(ns leihs.borrow.html
  (:require [leihs.core.http-cache-buster2 :as cache-buster]
            [leihs.core.json :refer [to-json]]
            [leihs.core.ssr :as ssr]
            [leihs.core.url.core :as url]
            [clojure.tools.logging :as log]
            [hiccup.page :refer [html5 include-js]]
            [ring.util.response :refer [resource-response content-type status]]))

(defn include-site-css []
  (hiccup.page/include-css
    (cache-buster/cache-busted-path "/borrow/css/site.css")))

(defn include-font-css []
  (hiccup.page/include-css
    "/borrow/css/fontawesome-free-5.0.13/web-fonts-with-css/css/fontawesome-all.css"))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
   (include-site-css)
   (include-font-css)])

(defn body-attributes [request]
  {:data-user (some-> (:authenticated-entity request) to-json url/encode)
   ; :data-leihsborrowversion (url/encode (to-json release-info/leihs-borrow-version))
   ; :data-leihsversion (url/encode (to-json release-info/leihs-version))
   })

(defn not-found-handler [request]
  (log/info request)
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html5
           (head)
           [:body
            (body-attributes request)
            [:div.container-fluid
             [:h1.text-danger "Error 404 - Not Found"]]])})

(defn html-handler [request]
  (log/debug "HTML")
  {:headers {"Content-Type" "text/html"}
   :body (html5
           #_(head)
           [:body #_(body-attributes request)
            [:div
             #_(ssr/render-navbar request {:borrow false})
             [:br]
             [:div#app.container-fluid
              [:div.alert.alert-warning
               [:h1 "Leihs New Borrow"]
               [:p "This application requires Javascript."]]]]
            #_(hiccup.page/include-js (cache-buster/cache-busted-path
                                      "/borrow/leihs-shared-bundle.js"))
            #_(hiccup.page/include-js
                (cache-buster/cache-busted-path "/borrow/js/app.js"))])})


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
