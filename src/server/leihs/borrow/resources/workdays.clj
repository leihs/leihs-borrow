(ns leihs.borrow.resources.workdays
  (:refer-clojure :exclude [get])
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [clojure.tools.logging :as log]))

(def columns [:workdays.monday
              :workdays.tuesday
              :workdays.wednesday
              :workdays.thursday
              :workdays.friday
              :workdays.saturday
              :workdays.sunday
              :workdays.max_visits])

(defn base-sqlmap [pool-id]
  (-> (apply sql/select columns)
      (sql/from :workdays)
      (sql/where [:= :inventory_pool_id pool-id])))

(defn with-workdays-sqlmap [sqlmap]
  (-> sqlmap
      (as-> sqlmap (apply sql/select sqlmap columns))
      (sql/join :workdays [:= :workdays.inventory_pool_id :inventory_pools.id])))

(defn get [tx pool-id]
  (-> pool-id
      base-sqlmap
      sql-format
      (->> (jdbc-query tx))
      first))

(comment
  (let [weekdays [:monday :tuesday :wednesday :thursday :friday :saturday :sunday]]
    (-> (get scratch/tx scratch/pool-id)
        (select-keys weekdays)
        (seq)))
  (base-sqlmap scratch/pool-id))
