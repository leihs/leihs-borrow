(ns leihs.borrow.resources.options
  (:require [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [logbug.debug :as debug]
            [leihs.core.sql :as sql]
            [leihs.core.core :refer [spy-with]]))

(def base-sqlmap
  (-> (sql/select :options.*
                  [(sql/raw "trim(both ' ' from concat_ws(' ', options.product, options.version))")
                   :name])
      (sql/from :options)
      (sql/order-by [:name :asc])))

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one [{{:keys [tx]} :request} {:keys [id]} value]
  (-> base-sqlmap
      (sql/where [:= :id (or id (:option-id value))])
      sql/format
      (->> (jdbc/query tx))
      first))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
