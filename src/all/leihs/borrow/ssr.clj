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
                                   "/borrow/css/theme/bootstrap-leihs.css"))
        (hiccup.page/include-css
          "/borrow/css/fontawesome-free-5.0.13/css/fontawesome-all.css"))
      [:body {:class "bg-paper"}
       [:noscript "This application requires Javascript."]
       inner-html
       (hiccup.page/include-js (cache-buster/cache-busted-path
                                 "/borrow/leihs-shared-bundle.js"))])))
