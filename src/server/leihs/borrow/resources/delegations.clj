(ns leihs.borrow.resources.delegations
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.sql :as sql]))

(defn responsible
  [{{tx :tx} :request} _ {responsible-id :delegator-user-id}]
  (-> (sql/select :users.*)
      (sql/from :users)
      (sql/where [:= :users.id responsible-id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-members
  [{{tx :tx} :request} _ {:keys [id]}]
  (-> (sql/select :users.*)
      (sql/from :users)
      (sql/join [:delegations_users :du]
                [:= :du.user_id :users.id])
      (sql/where [:= :du.delegation_id id])
      sql/format
      (->> (jdbc/query tx))))

(defn member? [tx user-id delegation-id]
  (-> (sql/select
        {:exists
         (-> (sql/select true)
             (sql/from [:delegations_users :du])
             (sql/where [:and
                         [:= :du.delegation_id delegation-id]
                         [:= :du.user_id user-id]]))})
      sql/format
      (->> (jdbc/query tx))
      first
      :exists))

(defn get-one
  [{{tx :tx} :request} {:keys [id]} _]
  (-> (sql/select [:users.id :id] [:firstname :name] :delegator_user_id)
      (sql/from :users)
      (sql/where [:= :users.id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-multiple
  [{{tx :tx} :request user-id ::target-user/id} _ _]
  (-> (sql/select [:users.id :id] [:firstname :name] :delegator_user_id)
      (sql/from :users)
      (sql/join [:delegations_users :du]
                [:= :du.delegation_id :users.id])
      (sql/where [:= :du.user_id user-id])
      (sql/order-by [:name :asc])
      sql/format
      (->> (jdbc/query tx))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
