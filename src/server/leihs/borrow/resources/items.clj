(ns leihs.borrow.resources.items
  (:require [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [logbug.debug :as debug]
            [leihs.core.sql :as sql]
            [leihs.core.core :refer [spy-with]]))

(def columns [:items.id :items.inventory_code :items.model_id])

(def base-sqlmap
  (-> (sql/select :items.*)
      (sql/from :items)
      (sql/order-by [:inventory_code :asc])))

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/where [:= :id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one [{{:keys [tx]} :request} {:keys [id]} value]
  (-> base-sqlmap
      (sql/where [:= :id (or id (:item-id value))])
      sql/format
      (->> (jdbc/query tx))
      first))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
