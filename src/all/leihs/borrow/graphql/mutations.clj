(ns leihs.borrow.graphql.mutations
  (:require [leihs.borrow.resources.reservations :as reservations]))

(def resolvers
  {:create-reservation reservations/create})
