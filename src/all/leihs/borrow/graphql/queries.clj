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
    [leihs.borrow.resources.visits :as visits]
    ))

(def resolvers
  {:attachments attachments/get-multiple
   :availability availability/get
   :available-quantity-in-date-range models/available-quantity-in-date-range
   :category categories/get-one
   :categories categories/get-multiple
   :contracts-connection contracts/get-connection
   :current-user users/get-current
   :child-categories categories/get-children
   :favorite-models-connection models/get-favorites-connection
   :images images/get-multiple
   :inventory-pool inventory-pools/get-one
   :inventory-pools inventory-pools/get-multiple
   :is-favorited-model models/favorited?
   :is-reservable-model models/reservable?
   :model models/get-one
   :model-availability models/get-availability
   :models-connection models/get-connection
   :order orders/get-one
   :orders-connection orders/get-connection
   :pool-orders orders/get-multiple-by-pool
   :properties properties/get-multiple
   :reservations reservations/get-multiple
   :root-categories categories/get-roots
   :thumbnails images/get-multiple-thumbnails
   :unsubmitted-order orders/get-unsubmitted
   :users users/get-multiple
   :visits visits/get-multiple
   })
