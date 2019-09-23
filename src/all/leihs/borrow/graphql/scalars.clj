(ns leihs.borrow.graphql.scalars
  (:import [java.util UUID]))

(def scalars
  {:uuid-parse #(UUID/fromString %)
   :uuid-serialize str})
