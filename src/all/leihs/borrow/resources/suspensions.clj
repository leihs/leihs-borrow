(ns leihs.borrow.resources.suspensions
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.sql :as sql]))

(defn get-multiple
  [{{:keys [tx] {user-id :id} :authenticated-entity} :request} _ _]
  (-> (sql/select :*)
      (sql/from :suspensions)
      (sql/where [:= :user_id user-id])
      sql/format
      (->> (jdbc/query tx))))
