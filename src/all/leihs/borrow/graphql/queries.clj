(ns leihs.borrow.graphql.queries
  (:require
    [leihs.borrow.resources.attachments :as attachments]
    [leihs.borrow.resources.availability :as availability]
    [leihs.borrow.resources.categories :as categories]
    [leihs.borrow.resources.contracts :as contracts]
    [leihs.borrow.resources.images :as images]
    [leihs.borrow.resources.inventory-pools :as inventory-pools]
    [leihs.borrow.resources.models :as models]
    [leihs.borrow.resources.users :as users]
    ))

(def resolvers
  {:attachments attachments/get-multiple
   :availability availability/get
   :categories categories/get-multiple
   :contracts contracts/get-multiple,
   :current-user users/get-current
   :images images/get-multiple
   :inventory-pool inventory-pools/get-one
   :inventory-pools inventory-pools/get-multiple
   :models models/get-multiple
   :thumbnails images/get-multiple-thumbnails
   :users users/get-multiple})
