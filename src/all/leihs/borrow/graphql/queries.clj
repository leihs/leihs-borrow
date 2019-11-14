(ns leihs.borrow.graphql.queries
  (:require
    [leihs.borrow.resources.attachments :as attachments]
    [leihs.borrow.resources.availability :as availability]
    [leihs.borrow.resources.categories :as categories]
    [leihs.borrow.resources.contracts :as contracts]
    [leihs.borrow.resources.images :as images]
    [leihs.borrow.resources.inventory-pools :as inventory-pools]
    [leihs.borrow.resources.models :as models]
    [leihs.borrow.resources.orders :as orders]
    [leihs.borrow.resources.properties :as properties]
    [leihs.borrow.resources.reservations :as reservations]
    [leihs.borrow.resources.users :as users]
    ))

(def resolvers
  {:attachments attachments/get-multiple
   :availability availability/get
   :categories categories/get-multiple
   :contracts-connection contracts/get-connection
   :current-user users/get-current
   :images images/get-multiple
   :inventory-pool inventory-pools/get-one
   :inventory-pools inventory-pools/get-multiple
   :model models/get-one
   :models-connection models/get-connection
   :order orders/get-one
   :orders-connection orders/get-connection
   :pool-orders orders/get-multiple-by-pool
   :properties properties/get-multiple
   :reservations reservations/get-multiple
   :thumbnails images/get-multiple-thumbnails
   :users users/get-multiple})
