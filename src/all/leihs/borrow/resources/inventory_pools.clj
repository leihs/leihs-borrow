(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.sql :as sql]))

(hugsql/def-sqlvec-fns "sql/pools_to_reserve_from.sql")

(def base-sqlmap
  (-> (sql/select :inventory_pools.*)
      (sql/modifiers :distinct)
      (sql/from :inventory_pools)
      (sql/merge-where [:= :inventory_pools.is_active true])))

(defn accessible-to-user-sqlmap [user-id]
  (-> base-sqlmap
      (sql/merge-join :access_rights
                      [:=
                       :access_rights.inventory_pool_id
                       :inventory_pools.id])
      (sql/merge-where [:= :access_rights.user_id user-id])))

(defn with-reservation-advance-days-sqlmap [sqlmap]
  (-> sqlmap
      (sql/merge-select :workdays.reservation_advance_days)
      (sql/left-join :workdays [:=
                                :workdays.inventory_pool_id
                                :inventory_pools.id])))

(defn ready-for-hand-over-sqlmap [sqlmap date]
  (-> sqlmap
      with-reservation-advance-days-sqlmap
      (sql/merge-where [:>=
                        (sql/call :- 
                                  (sql/call :cast date :date)
                                  (sql/raw "now()::date"))
                        :workdays.reservation_advance_days])))

(defn ready-for-take-back-sqlmap [sqlmap _]
  sqlmap)

(defn to-reserve-from-sqlmap [user-id start-date end-date]
  (-> (accessible-to-user-sqlmap user-id)
      (ready-for-hand-over-sqlmap start-date)
      (ready-for-take-back-sqlmap end-date)))

(defn to-reserve-from [tx user-id start-date end-date]
  (->> {:user-id user-id, :start-date start-date, :end-date end-date}
       to-reserve-from-sqlvec
       (jdbc/query tx)))

(defn get-multiple
  [{{tx :tx {user-id :id} :authenticated-entity} :request}
   {:keys [order-by ids]}
   _]
  (-> (accessible-to-user-sqlmap user-id)
      (cond-> (seq ids)
        (-> (sql/merge-where [:in :inventory_pools.id ids])))
      (cond-> (seq order-by)
        (-> (sql/order-by (helpers/treat-order-arg order-by))
            (sql/merge-order-by [:inventory_pools.name :asc])))
      sql/format
      (->> (jdbc/query tx))))

(defn get-by-id [tx id]
  (-> base-sqlmap
      with-reservation-advance-days-sqlmap
      (sql/merge-where [:= :inventory_pools.id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one [context _ value]
  (get-by-id (-> context :request :tx)
             (:inventory-pool-id value)))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
