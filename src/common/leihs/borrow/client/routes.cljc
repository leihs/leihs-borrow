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
   "current-user" ::current-user-show
   "delegations" {"" ::delegations-index
                  "/" ::delegations-index
                  ["/" :delegation-id] ::delegations-show}
   "models" {"" ::models
             "/" ::models
             "/favorites" ::models-favorites
             ["/" :model-id] ::models-show}
   "order" {"" ::shopping-cart
            "/draft" ::draft-order}
   "orders" {"/" ::orders-index
             ["/" :order-id] ::orders-show}
   "templates" {"/" ::templates-index
                ["/" :template-id] ::templates-show}
   "pickups" {"" ::pickups-index
              "/" ::pickups-index}
   "inventory-pools" {"" ::inventory-pools-index
            "/" ::inventory-pools-index
            ["/" :inventory-pool-id] ::inventory-pools-show}
   "returns" {"" ::returns-index
              "/" ::returns-index}
   :else ::not-found})

(def routes-map
  ["/"
   {"" ::absolute-root ; only applicable in dev, does a redirect
    "app/borrow/" client-routes}])
