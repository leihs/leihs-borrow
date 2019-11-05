(ns leihs.borrow.graphql.mutations
  (:require [leihs.borrow.resources.reservations :as reservations]
            [leihs.borrow.resources.orders :as orders]))

(def resolvers
  {:create-reservation reservations/create
   :submit-order orders/submit})
