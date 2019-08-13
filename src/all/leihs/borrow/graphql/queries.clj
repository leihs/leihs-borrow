(ns leihs.borrow.graphql.queries
  (:require [leihs.borrow.resources.inventory-pools :as inventory-pools]
            [leihs.borrow.resources.users :as users]
            [leihs.borrow.resources.contracts :as contracts]))

(def resolvers
  {:users users/get-multiple,
   :contracts contracts/get-multiple,
   :inventory-pool inventory-pools/get-one
   })
