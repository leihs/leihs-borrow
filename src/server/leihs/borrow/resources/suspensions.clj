(ns leihs.borrow.resources.suspensions
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.database.helpers :as database]
            [leihs.core.sql :as sql]))

(defn get-multiple
  [{{:keys [tx]} :request user-id ::target-user/id}
   _
   {value-user-id :id}]
  (-> (sql/select :*)
      (sql/from :suspensions)
      (sql/where [:= :user_id (or value-user-id user-id)])
      sql/format
      (->> (jdbc/query tx))))
