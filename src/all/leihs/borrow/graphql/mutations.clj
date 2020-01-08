(ns leihs.borrow.graphql.mutations
  (:require [leihs.borrow.resources.favorites :as favorites]
            [leihs.borrow.resources.reservations :as reservations]
            [leihs.borrow.resources.orders :as orders]))

(def resolvers
  {:create-reservation reservations/create
   :favorite-model favorites/create
   :delete-reservations reservations/delete
   :refresh-timeout orders/refresh-timeout
   :submit-order orders/submit
   :unfavorite-model favorites/delete})
