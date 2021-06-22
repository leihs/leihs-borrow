(ns leihs.borrow.graphql.queries
  (:require
    [leihs.borrow.resources.attachments :as attachments]
    [leihs.borrow.resources.availability :as availability]
    [leihs.borrow.resources.categories :as categories]
    [leihs.borrow.resources.contracts :as contracts]
    [leihs.borrow.resources.delegations :as delegations]
    [leihs.borrow.resources.images :as images]
    [leihs.borrow.resources.inventory-pools :as inventory-pools]
    [leihs.borrow.resources.items :as items]
    [leihs.borrow.resources.languages :as languages]
    [leihs.borrow.resources.models :as models]
    [leihs.borrow.resources.options :as options]
    [leihs.borrow.resources.orders :as orders]
    [leihs.borrow.resources.properties :as properties]
    [leihs.borrow.resources.reservations :as reservations]
    [leihs.borrow.resources.suspensions :as suspensions]
    [leihs.borrow.resources.users :as users]
    [leihs.borrow.resources.visits :as visits]
    [leihs.borrow.resources.templates :as templates]
    [leihs.borrow.resources.shared.core :as shared]
    [leihs.borrow.testing :as testing]
    ))

(def resolvers
  {:attachments attachments/get-multiple
   :available-quantity-in-date-range models/available-quantity-in-date-range
   :category categories/get-one
   :categories categories/get-multiple
   :contract contracts/get-one
   :contracts-connection contracts/get-connection
   :cover-image images/get-cover
   :current-user users/get-current
   :child-categories categories/get-children
   :delegation delegations/get-one
   :delegations delegations/get-multiple
   :favorite-models-connection models/get-favorites-connection
   :has-reservable-items inventory-pools/has-reservable-items?
   :images images/get-multiple
   :inventory-pool inventory-pools/get-one
   :inventory-pools inventory-pools/get-multiple
   :is-favorited-model models/favorited?
   :is-reservable-model models/reservable?
   :item items/get-one
   :language languages/get-one
   :language-to-use languages/one-to-use
   :maximum-reservation-time inventory-pools/maximum-reservation-time
   :members delegations/get-members
   :model models/get-one
   :model-availability models/get-availability
   :models-connection models/get-connection
   :option options/get-one
   :order orders/get-one
   :orders-connection orders/get-connection
   :pickups visits/get-pickups
   :pool-order orders/get-one-by-pool
   :pool-orders orders/get-multiple-by-pool
   :properties properties/get-multiple
   :reservations reservations/get-multiple
   :responsible delegations/responsible
   :returns visits/get-returns
   :root-categories categories/get-roots
   :suspensions suspensions/get-multiple
   :thumbnails images/get-multiple-thumbnails
   :unsubmitted-order orders/get-unsubmitted
   :user users/get-one
   :users users/get-multiple
   :draft-order orders/get-draft
   :template templates/get-one
   :templates templates/get-multiple
   :template-lines templates/get-lines
   :testing-query testing/query
   :total-borrowable-quantities models/total-borrowable-quantities
   :url shared/url
   })
