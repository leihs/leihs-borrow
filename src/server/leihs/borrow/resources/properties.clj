(ns leihs.borrow.resources.properties
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def base-sqlmap
  (-> (sql/select :properties.*)
      (sql/from :properties)))

(defn get-multiple [{{tx :tx} :request} _ value]
  (-> base-sqlmap
      (sql/where [:= :properties.model_id (:id value)])
      sql-format
      (->> (jdbc-query tx))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
