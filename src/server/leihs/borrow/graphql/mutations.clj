(ns leihs.borrow.graphql.mutations
  (:require [leihs.borrow.resources.favorites :as favorites]
            [leihs.borrow.resources.reservations :as reservations]
            [leihs.borrow.resources.orders :as orders]
            [leihs.borrow.resources.templates :as templates]
            [leihs.borrow.testing :as testing]))

(def audit-exceptions #{"refreshTimeout"})

(def resolvers
  {:add-to-cart reservations/add-to-cart
   :apply-template templates/apply
   :cancel-order orders/cancel
   :repeat-order orders/repeat-order
   :create-reservation reservations/create
   :favorite-model favorites/create
   :delete-reservations reservations/delete
   :refresh-timeout orders/refresh-timeout
   :submit-order orders/submit
   :testing-mutate testing/mutate
   :unfavorite-model favorites/delete})
