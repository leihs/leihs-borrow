(ns leihs.borrow.resources.contracts
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn get-multiple
  [context _ value]
  (jdbc/query
    (-> context :request :tx)
    (-> (sql/select :*)
        (sql/from :contracts)
        (sql/merge-where [:= :user_id (:id value)])
        sql/format)))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
