(ns leihs.borrow.client.routes)

; the route prefix is for this app "/app/borrow".
; define the un-prefixed map so it can be more easily used in backend,
; maybe this can be made better because we anyhow want to ensure the same prefix.

(def client-routes
  {"" ::home
   "categories" {"" ::categories-index
                 "/" ::categories-index
                 ["/" [#".+" :categories-path]] ::categories-show}
   "current-user" ::current-user-show
   "debug" ::debug-page
   "models" {"" ::models
             "/" ::models
             "/favorites" ::models-favorites
             ["/" :model-id] ::models-show}
   "order" {"" ::shopping-cart}
   "rentals" {"/" ::rentals-index
              ["/" :rental-id] ::rentals-show}
   "templates" {"/" ::templates-index
                ["/" :template-id] ::templates-show}
   "inventory-pools" {"" ::inventory-pools-index
                      "/" ::inventory-pools-index
                      ["/" :inventory-pool-id] ::inventory-pools-show}
   "testing" {"/step-1" ::testing-step-1
              "/step-2" ::testing-step-2}
   :else ::not-found})

(def routes-map
  ["/"
   {"" ::absolute-root ; only applicable in dev, does a redirect
    "app/borrow/" client-routes}])
