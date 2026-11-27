(ns leihs.borrow.resources.suspensions
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [leihs.borrow.graphql.target-user :as target-user]))

(defn get-multiple
  [{{tx :tx} :request user-id ::target-user/id}
   _
   {value-user-id :id}]
  (-> (sql/select :*)
      (sql/from :suspensions)
      (sql/where [:= :user_id (or value-user-id user-id)])
      (sql/where [:<= :current_date :suspended_until])
      sql-format
      (->> (jdbc-query tx))))
