(ns leihs.borrow.resources.inventory-pools
  (:require [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [com.walmartlabs.lacinia :as lacinia]
            [hugsql.core :as hugsql]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.workdays :as workdays]
            [leihs.core.db :as db]
            [taoensso.timbre :refer [debug info warn error spy]]))

(hugsql/def-sqlvec-fns "sql/pools_to_reserve_from.sql")

(def select-fields
  [:inventory_pools.id
   :inventory_pools.name
   :inventory_pools.description
   :inventory_pools.default_contract_note
   :inventory_pools.shortname
   :inventory_pools.email
   :inventory_pools.print_contracts
   :inventory_pools.automatic_suspension
   :inventory_pools.automatic_suspension_reason
   :inventory_pools.required_purpose
   :inventory_pools.is_active
   :inventory_pools.deliver_received_order_emails
   :inventory_pools.borrow_reservation_advance_days
   [:inventory_pools.borrow_reservation_advance_days
    :reservation_advance_days]
   :inventory_pools.borrow_maximum_reservation_duration
   [:inventory_pools.borrow_maximum_reservation_duration
    :maximum_reservation_duration]])

(def base-sqlmap
  (-> (apply sql/select select-fields)
      (sql/from :inventory_pools)
      (sql/where [:= :inventory_pools.is_active true])))

(comment
  (sql-format (apply sql/select select-fields))
  (sql-format base-sqlmap))

(defn accessible-to-user-condition [sqlmap user-id]
  (-> sqlmap
      (sql/join :access_rights
                [:=
                 :access_rights.inventory_pool_id
                 :inventory_pools.id])
      (sql/where [(if (coll? user-id) :in :=) :access_rights.user_id user-id])))

(defn accessible-to-user [tx user-id]
  (-> base-sqlmap
      (accessible-to-user-condition user-id)
      sql-format
      (->> (jdbc-query tx))))

(defn to-reserve-from [tx user-id start-date end-date]
  (->> {:user-id user-id, :start-date start-date, :end-date end-date}
       to-reserve-from-sqlvec
       (jdbc-query tx)))

(defn get-multiple
  [{{tx :tx} :request user-id ::target-user/id}
   {:keys [order-by ids]}
   {value-user-id :id}]
  (-> base-sqlmap
      (accessible-to-user-condition (or value-user-id user-id))
      (cond-> (seq ids)
        (-> (sql/where [:in :inventory_pools.id ids])))
      (cond-> (seq order-by)
        (-> (as-> sqlmap
                  (apply sql/order-by sqlmap (helpers/treat-order-arg order-by :inventory_pools)))
            (sql/order-by [:inventory_pools.name :asc])))
      sql-format
      (->> (jdbc-query tx))))

(defn get-by-id
  ([tx id]
   (get-by-id tx id false))
  ([tx id include-inactives]
   (-> base-sqlmap
       (cond-> include-inactives
         (-> (dissoc :where)
             (sql/where [:in :inventory_pools.is_active [true, false]])))
       workdays/with-workdays-sqlmap
       (sql/where [:= :inventory_pools.id id])
       sql-format
       (->> (jdbc-query tx))
       first)))

(defn get-one [{{tx :tx} :request :as context} {:keys [id]} value]
  (get-by-id tx
             (or id (:inventory-pool-id value))
             (contains? #{:PoolOrder :Contract :Reservation :Suspension}
                        (::lacinia/container-type-name context))))

(defn has-reservable-items? [{{tx :tx} :request} _ {:keys [id]}]
  (-> (sql/select
       [[:exists
         (-> (sql/select :*)
             (sql/from :items)
             (sql/where [:and
                         [:= :inventory_pool_id id]
                         [:is :retired nil]
                         :is_borrowable
                         [:is :parent_id nil]]))]])
      sql-format
      (->> (jdbc-query tx))
      first
      :exists))

(comment (has-reservable-items? {:request {:tx (db/get-ds)}}
                                nil
                                {:id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"}))

(defn has-templates? [{{tx :tx} :request} _ {:keys [id]}]
  (-> (sql/select :*)
      (sql/from :model_groups)
      (sql/join [:inventory_pools_model_groups :ipmg]
                [:= :ipmg.model_group_id :model_groups.id])
      (sql/where [:and
                  [:= :inventory_pool_id id]
                  [:= :type "Template"]])
      (sql/limit 1)
      sql-format
      (->> (jdbc-query tx))
      count
      (> 0)))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
