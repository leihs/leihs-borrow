(ns leihs.borrow.resources.delegations
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]

            [clojure.tools.logging :as log]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.core :refer [raise]]
            [taoensso.timbre :refer [debug info warn error spy]]))

(defn delegation? [user]
  (:delegator_user_id user))

(defn responsible
  [{{tx :tx-next} :request} _ {responsible-id :delegator-user-id}]
  (-> (sql/select :users.*)
      (sql/from :users)
      (sql/where [:= :users.id responsible-id])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn get-members
  [{{tx :tx-next} :request} _ {:keys [id]}]
  (-> (sql/select :users.*)
      (sql/from :users)
      (sql/join [:delegations_users :du]
                [:= :du.user_id :users.id])
      (sql/where [:= :du.delegation_id id])
      sql-format
      (->> (jdbc-query tx))))

(defn member? [tx user-id delegation-id]
  (-> (sql/select
       [[:exists
         (-> (sql/select true)
             (sql/from [:delegations_users :du])
             (sql/where [:and
                         [:= :du.delegation_id delegation-id]
                         [:= :du.user_id user-id]]))]])
      sql-format
      (->> (jdbc-query tx))
      first
      :exists))

(defn get-multiple-by-user-id [tx user-id]
  (-> (sql/select [:users.id :id] [:firstname :name] :delegator_user_id)
      (sql/from :users)
      (sql/join [:delegations_users :du]
                [:= :du.delegation_id :users.id])
      (sql/where [:= :du.user_id user-id])
      (sql/order-by [:name :asc])
      sql-format
      (->> (jdbc-query tx))))

(defn get-one
  [{{tx :tx-next {auth-user-id :id} :authenticated-entity} :request} {:keys [id]} _]
  (-> (sql/select [:users.id :id] [:firstname :name] :delegator_user_id)
      (sql/from :users)
      (sql/where [:= :users.id id])
      (sql/where [:is-not-null :users.delegator_user_id])
      (sql/where [:exists (-> (sql/select true)
                              (sql/from :delegations_users)
                              (sql/where [:= :delegation_id :users.id])
                              (sql/where [:= :user_id auth-user-id]))])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn get-multiple
  [{{tx :tx-next} :request user-id ::target-user/id} _ _]
  (get-multiple-by-user-id tx user-id))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
