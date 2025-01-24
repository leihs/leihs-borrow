(ns leihs.borrow.resources.workdays
  (:refer-clojure :exclude [get])
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def DAYS [:MONDAY :TUESDAY :WEDNESDAY :THURSDAY :FRIDAY :SATURDAY :SUNDAY])

(def columns [:workdays.monday
              :workdays.monday_info
              :workdays.tuesday
              :workdays.tuesday_info
              :workdays.wednesday
              :workdays.wednesday_info
              :workdays.thursday
              :workdays.thursday_info
              :workdays.friday
              :workdays.friday_info
              :workdays.saturday
              :workdays.saturday_info
              :workdays.sunday
              :workdays.sunday_info
              :workdays.max_visits])

(defn base-sqlmap [pool-id]
  (-> (apply sql/select columns)
      (sql/from :workdays)
      (sql/where [:= :inventory_pool_id pool-id])))

(defn with-workdays-sqlmap [sqlmap]
  (-> sqlmap
      (as-> sqlmap (apply sql/select sqlmap columns))
      (sql/join :workdays [:= :workdays.inventory_pool_id :inventory_pools.id])))

(defn transform [w]
  (map (fn [day]
         (let [day-lc (-> day name string/lower-case)]
           (hash-map :day (string/upper-case day-lc)
                     :open ((keyword day-lc) w)
                     :info ((keyword (str day-lc "_info")) w))))
       DAYS))

(defn get-multiple [{{tx :tx} :request} _ {pool-id :id}]
  (-> pool-id
      base-sqlmap
      sql-format
      (->> (jdbc-query tx))
      first
      transform))

(comment
  (require '[leihs.core.db :as db])
  (let [pool-id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"
        tx (db/get-ds)]
    (get-multiple {:tx tx} nil {:id pool-id})))
