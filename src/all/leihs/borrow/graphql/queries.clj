(ns leihs.borrow.graphql.queries
  (:require [leihs.borrow.resources.user :as user]
            [leihs.borrow.resources.users :as users]))

(def resolvers
  {;:current-user current-user/get-current-user,
   ;:model model/get-model,
   ;:models models/get-models,
   ;:settings settings/get-settings,
   :user user/get-user,
   :users users/get-users})
