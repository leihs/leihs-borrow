(ns leihs.borrow.ssr
  (:require [hiccup.page :refer [html5 include-js]]
            [leihs.core.shared :refer [head]]
            [leihs.core.http-cache-buster2 :as cache-buster]))

(intern
  'leihs.core.ssr
  'render-page-base
  (fn [inner-html]
    (html5
      (head
        (hiccup.page/include-css (cache-buster/cache-busted-path
                                   "/app/borrow/css/theme/bootstrap-leihs.css")))
      [:body {:class "bg-paper"}
       [:noscript "This application requires Javascript."]
       inner-html
       (hiccup.page/include-js (cache-buster/cache-busted-path
                                 "/app/borrow/leihs-shared-bundle.js"))])))
