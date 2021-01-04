(ns leihs.borrow.resources.favorites
  (:require [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.executor :as executor]
            [leihs.core.sql :as sql]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.models :as models]))

(defn create
  [{{:keys [tx]} :request user-id ::target-user/id}
   {model-id :id}
   _]
  (-> (sql/insert-into :favorite_models)
      (sql/values [{:user_id user-id, :model_id model-id}])
      (sql/on-conflict :user_id :model_id)
      sql/do-nothing
      sql/format
      (->> (jdbc/execute! tx)))
  (models/get-one-by-id tx model-id))

(defn delete
  [{{:keys [tx]} :request user-id ::target-user/id}
   {model-id :id}
   _]
  (-> (sql/delete-from :favorite_models)
      (sql/where [:and
                  [:= :user_id user-id]
                  [:= :model_id model-id]])
      sql/format
      (->> (jdbc/execute! tx)))
  (models/get-one-by-id tx model-id))
