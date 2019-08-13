(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn get-one
  [context _ value]
  (-> (sql/select :*)
      (sql/from :inventory_pools)
      (sql/merge-where [:= :id (:inventory_pool_id value)])
      sql/format
      (->> (jdbc/query (-> context :request :tx)))
      first))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
