(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.core.settings :refer [settings!]]
            [leihs.core.sql :as sql]))

(hugsql/def-sqlvec-fns "sql/pools_to_reserve_from.sql")

(def base-sqlmap
  (-> (sql/select :inventory_pools.*)
      (sql/modifiers :distinct)
      (sql/from :inventory_pools)
      (sql/merge-where [:= :inventory_pools.is_active true])))

(defn accessible-to-user-condition [sqlmap user-id]
  (-> sqlmap
      (sql/merge-join :access_rights
                      [:=
                       :access_rights.inventory_pool_id
                       :inventory_pools.id])
      (sql/merge-where [:= :access_rights.user_id user-id])))

(defn with-workdays-sqlmap [sqlmap]
  (-> sqlmap
      (sql/merge-select :workdays.*)
      (sql/left-join :workdays [:=
                                :workdays.inventory_pool_id
                                :inventory_pools.id])))

(defn to-reserve-from [tx user-id start-date end-date]
  (->> {:user-id user-id, :start-date start-date, :end-date end-date}
       to-reserve-from-sqlvec
       (jdbc/query tx)))

(defn get-multiple
  [{{tx :tx user-id :target-user-id} :request}
   {:keys [order-by ids]}
   _]
  (-> base-sqlmap
      (accessible-to-user-condition user-id)
      (cond-> (seq ids)
        (-> (sql/merge-where [:in :inventory_pools.id ids])))
      (cond-> (seq order-by)
        (-> (sql/order-by (helpers/treat-order-arg order-by))
            (sql/merge-order-by [:inventory_pools.name :asc])))
      sql/format
      (->> (jdbc/query tx))))

(defn get-by-id [tx id]
  (-> base-sqlmap
      with-workdays-sqlmap
      (sql/merge-where [:= :inventory_pools.id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-one [context {:keys [id]} value]
  (get-by-id (-> context :request :tx)
             (or id (:inventory-pool-id value))))

(defn has-reservable-items? [{{:keys [tx]} :request} _ {:keys [id]}]
  (-> (sql/select :*)
      (sql/from :items)
      (sql/where [:and
                  [:= :inventory_pool_id id]
                  :is_borrowable
                  [:is :parent_id nil]])
      sql/format
      (->> (jdbc/query tx))
      empty?
      not))

(defn maximum-reservation-time [{{:keys [tx]} :request} _ _]
  (-> tx settings! :maximum_reservation_time))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
