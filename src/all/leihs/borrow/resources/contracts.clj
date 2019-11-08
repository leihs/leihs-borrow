(ns leihs.borrow.resources.contracts
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [clojure.string :refer [lower-case]]
            [leihs.core.sql :as sql]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.connections :refer [row-cursor cursored-sqlmap] :as connections]))

(defn get-connection-sql-map
  [_ {:keys [states order-by]} value]
  (-> (sql/select :*)
      (sql/from :contracts)
      (cond-> value
        (sql/merge-where [:= :user_id (:id value)]))
      (cond-> states
        (sql/merge-where
          [:in
           :state
           (map #(-> % name lower-case) states)]))
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))))

(defn get-connection [context args value]
  (connections/wrap get-connection-sql-map
                    context
                    args
                    value)) 

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
