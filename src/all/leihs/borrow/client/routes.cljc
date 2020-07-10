(ns leihs.borrow.client.routes)

; the route prefix is for this app "/app/borrow".
; define the un-prefixed map so it can be more easily used in backend,
; maybe this can be made better because we anyhow want to ensure the same prefix.

(def client-routes
  {"" ::home
   "about" ::about-page
   "categories" {"" ::categories-index
                 "/" ::categories-index
                 ["/" [#".+" :categories-path]] ::categories-show}
   "models" {"" ::models
             "/" ::models
             "/favorites" ::models-favorites
             ["/" :model-id] ::models-show}
   "order" ::shopping-cart
   "orders" {"/" ::orders-index
             ["/" :order-id] ::orders-show}
   "pools" {"" ::pools-index
            "/" ::pools-index
            ["/" :pool-id] ::pools-show}
   :else ::not-found})

(def routes-map
  ["/"
   {"" ::absolute-root ; only applicable in dev, does a redirect
    "app/borrow/" client-routes}])
