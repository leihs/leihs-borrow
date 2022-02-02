(ns leihs.borrow.resources.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [clojure.tools.logging :as log]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.settings :as settings]
            [leihs.core.sql :as sql]
            [leihs.core.user.queries :refer [merge-search-term-where-clause]]))

(defn sql-order-users
  [sqlmap]
  (sql/order-by
   sqlmap
   (sql/call :concat :users.firstname :users.lastname :users.login :users.id)))

(def base-sqlmap
  (-> (sql/select
       :users.*
       [(sql/raw "users.firstname || ' ' || users.lastname") :name])
      (sql/from :users)
      sql-order-users))

(defn get-multiple [context {:keys [offset limit order-by search-term]} _]
  (jdbc/query
   (-> context :request :tx)
   (-> (cond-> base-sqlmap
         search-term
         (merge-search-term-where-clause search-term)
         (seq order-by)
         (-> (sql/order-by (helpers/treat-order-arg order-by :users)))
         offset
         (sql/offset offset)
         limit
         (sql/limit limit))
       sql/format)))

(defn get-by-id [tx id]
  (-> base-sqlmap
      (sql/merge-where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one
  [{{:keys [tx]} :request target-user-id ::target-user/id} _ {:keys [user-id]}]
  (get-by-id tx (or user-id target-user-id)))

(defn get-current
  [{{tx :tx {session-id :user_session_id} :authenticated-entity} :request
    user-id ::target-user/id}
   _
   _]
  {:id user-id
   :user (get-by-id tx user-id)
   :session-id session-id})

(defn get-navigation [{{:keys [tx]} :request} _ {user-id :id}]
  {:legacy-url
   (str (:external_base_url (settings/get-system-and-security tx))
        "/borrow/")
   :documentation-url
   (:documentation_link (settings/get tx))})

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
