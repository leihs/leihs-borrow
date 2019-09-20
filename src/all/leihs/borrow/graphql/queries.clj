(ns leihs.borrow.graphql.queries
  (:require [leihs.borrow.resources.inventory-pools :as inventory-pools]
            [leihs.borrow.resources.users :as users]
            [leihs.borrow.resources.calendar :as calendar]
            [leihs.borrow.resources.contracts :as contracts]
            [leihs.borrow.resources.categories :as categories]))

(def resolvers
  {:users users/get-multiple,
   :contracts contracts/get-multiple,
   :inventory-pool inventory-pools/get-one
   :calendar calendar/get
   :main-categories categories/get-main-multiple
   :sub-categories categories/get-multiple
   })
