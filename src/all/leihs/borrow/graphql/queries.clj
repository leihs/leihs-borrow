(ns leihs.borrow.graphql.queries
  (:require [leihs.borrow.resources.inventory-pools :as inventory-pools]
            [leihs.borrow.resources.users :as users]
            [leihs.borrow.resources.availability :as availability]
            [leihs.borrow.resources.contracts :as contracts]
            [leihs.borrow.resources.categories :as categories]
            [leihs.borrow.resources.images :as images]
            [leihs.borrow.resources.models :as models]))

(def resolvers
  {:users users/get-multiple,
   :contracts contracts/get-multiple,
   :inventory-pool inventory-pools/get-one
   :inventory-pools inventory-pools/get-multiple
   :availability availability/get
   :categories categories/get-multiple
   :models models/get-multiple
   :current-user users/get-current
   :images images/get-multiple
   :thumbnails images/get-multiple-thumbnails
   })
