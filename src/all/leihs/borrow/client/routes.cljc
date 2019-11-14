(ns leihs.borrow.client.routes)

; the route prefix is for this app "/borrow".
; define the un-prefixed map so it can be more easliy used in backend,
; maybe this can be made better because we anyhow want to ensure the same prefix.

(def client-routes
  {"" ::home
   "search" ::search
   :else ::not-found})

(def routes-map
  ["/"
   {"" ::absolute-root ; only applicable in dev, does a redirect
    "borrow/" client-routes}])