(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [com.walmartlabs.lacinia :as lacinia]
            [hugsql.core :as hugsql]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.settings :as settings]
            [leihs.borrow.resources.workdays :as workdays]
            [leihs.core.sql :as sql]))

(hugsql/def-sqlvec-fns "sql/pools_to_reserve_from.sql")

(def base-sqlmap
  (-> (sql/select :inventory_pools.*)
      (sql/modifiers :distinct)
      (sql/from :inventory_pools)
      (sql/where [:= :inventory_pools.is_active true])))

(defn accessible-to-user-condition [sqlmap user-id]
  (-> sqlmap
      (sql/merge-join :access_rights
                      [:=
                       :access_rights.inventory_pool_id
                       :inventory_pools.id])
      (sql/merge-where [:= :access_rights.user_id user-id])))

(defn accessible-to-user [tx user-id]
  (-> base-sqlmap
      (accessible-to-user-condition user-id)
      sql/format
      (->> (jdbc/query tx))))

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
  [{{tx :tx} :request user-id ::target-user/id}
   {:keys [order-by ids]}
   {value-user-id :id}]
  (-> base-sqlmap
      (accessible-to-user-condition (or value-user-id user-id))
      (cond-> (seq ids)
        (-> (sql/merge-where [:in :inventory_pools.id ids])))
      (cond-> (seq order-by)
        (-> (sql/order-by (helpers/treat-order-arg order-by :inventory_pools))
            (sql/merge-order-by [:inventory_pools.name :asc])))
      sql/format
      (->> (jdbc/query tx))))

(defn get-by-id
  ([tx id]
   (get-by-id tx id false))
  ([tx id include-inactives]
   (-> base-sqlmap
       (cond-> include-inactives
         (sql/where [:in :inventory_pools.is_active [true, false]]))
       with-workdays-sqlmap
       (sql/merge-where [:= :inventory_pools.id id])
       sql/format
       (->> (jdbc/query tx))
       first)))

(defn get-one [context {:keys [id]} value]
  (get-by-id (-> context :request :tx)
             (or id (:inventory-pool-id value))
             (contains? #{:PoolOrder :Contract :Reservation :Suspension}
                        (::lacinia/container-type-name context))))

(defn has-reservable-items? [{{:keys [tx]} :request} _ {:keys [id]}]
  (-> (sql/select
       (sql/call :exists
                 (-> (sql/select :*)
                     (sql/from :items)
                     (sql/where [:and
                                 [:= :inventory_pool_id id]
                                 [:is :retired nil]
                                 :is_borrowable
                                 [:is :parent_id nil]]))))
      sql/format
      (->> (jdbc/query tx))
      first
      :exists))

(defn maximum-reservation-time [{{:keys [tx]} :request} _ _]
  (-> tx settings/get :maximum_reservation_time))

(defn reservation-advance-days [{{:keys [tx]} :request} _ {:keys [id]}]
  (-> tx (workdays/get id) :reservation_advance_days))


;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
