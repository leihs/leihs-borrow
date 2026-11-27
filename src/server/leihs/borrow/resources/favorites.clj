(ns leihs.borrow.resources.favorites
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc :refer [execute!] :rename {execute! jdbc-execute!}]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.models.core :as models]))

(defn create
  [{{tx :tx} :request user-id ::target-user/id}
   {model-id :id}
   _]
  (-> (sql/insert-into :favorite_models)
      (sql/values [{:user_id user-id, :model_id model-id}])
      (sql/on-conflict :user_id :model_id)
      sql/do-nothing
      sql-format
      (->> (jdbc-execute! tx)))
  (models/get-one-by-id tx model-id))

(defn delete
  [{{tx :tx} :request user-id ::target-user/id}
   {model-id :id}
   _]
  (-> (sql/delete-from :favorite_models)
      (sql/where [:and
                  [:= :user_id user-id]
                  [:= :model_id model-id]])
      sql-format
      (->> (jdbc-execute! tx)))
  (models/get-one-by-id tx model-id))
