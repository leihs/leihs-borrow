(ns leihs.borrow.resources.options
  (:require [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [logbug.debug :as debug]))

(def base-sqlmap
  (-> (sql/select :options.*
                  [[:raw "trim(both ' ' from concat_ws(' ', options.product, options.version))"]
                   :name])
      (sql/from :options)
      (sql/order-by [:name :asc])))

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn get-one [{{tx :tx} :request} {:keys [id]} value]
  (-> base-sqlmap
      (sql/where [:= :id (or id (:option-id value))])
      sql-format
      (->> (jdbc-query tx))
      first))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
