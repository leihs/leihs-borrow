(ns leihs.borrow.graphql.queries
  (:require
   [leihs.borrow.resources.attachments :as attachments]
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
   [leihs.borrow.testing :as testing]))

(def resolvers
  {:approve-fulfillment visits/approve-fulfillment
   :approved-pool-orders-count orders/approved-pool-orders-count
   :attachments attachments/get-multiple
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
   :expired-unapproved-rental-quantity orders/expired-unapproved-rental-quantity
   :expired-rental-quantity orders/expired-rental-quantity
   :favorite-models-connection models/get-favorites-connection
   :has-reservable-items inventory-pools/has-reservable-items?
   :images images/get-multiple
   :inventory-pool inventory-pools/get-one
   :inventory-pool-has-templates inventory-pools/has-templates?
   :inventory-pools inventory-pools/get-multiple
   :is-favorited-model models/favorited?
   :is-reservable-model models/reservable?
   :item items/get-one
   :language languages/get-one
   :languages languages/get-multiple
   :language-to-use languages/one-to-use
   :members delegations/get-members
   :model models/get-one
   :model-availability models/get-availability
   :models-connection models/get-connection
   :option options/get-one
   :order orders/get-one
   :orders-connection orders/get-connection
   :overdue-rental-quantity orders/overdue-rental-quantity
   :pickup-fulfillment visits/pickup-fulfillment
   :pool-order orders/get-one-by-pool
   :pool-orders orders/get-multiple-by-pool
   :pool-orders-count orders/pool-orders-count
   :print-url contracts/print-url
   :properties properties/get-multiple
   :rejected-pool-orders-count orders/rejected-pool-orders-count
   :rejected-rental-quantity orders/rejected-rental-quantity
   :reservations reservations/get-multiple
   :responsible delegations/responsible
   :return-fulfillment visits/return-fulfillment
   :root-categories categories/get-roots
   :submitted-pool-orders-count orders/submitted-pool-orders-count
   :suspensions suspensions/get-multiple
   :thumbnails images/get-multiple-thumbnails
   :cart orders/get-cart
   :user users/get-one
   :user-navigation users/get-navigation
   :user-settings users/get-settings
   :template templates/get-one
   :templates templates/get-multiple
   :template-lines templates/get-lines
   :testing-query testing/query
   :total-reservable-quantities models/total-reservable-quantities
   :total-rental-quantity orders/total-rental-quantity})
