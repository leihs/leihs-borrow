(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(def base-sqlmap (-> (sql/select :*)
                     (sql/from :inventory_pools)
                     (sql/merge-where [:= :is_active true])))

(defn get-multiple [context _ _]
  (-> base-sqlmap
      sql/format
      (->> (jdbc/query (-> context :request :tx)))))

(defn get-one
  ([tx id]
   (-> base-sqlmap
       (sql/merge-where [:= :id id])
       sql/format
       (->> (jdbc/query tx))
       first))
  ([context _ value]
   (get-one (-> context :request :tx)
            (:inventory_pool_id value))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
