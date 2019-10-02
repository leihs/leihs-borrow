(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn get-one
  ([tx id]
   (-> (sql/select :*)
       (sql/from :inventory_pools)
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
