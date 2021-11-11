(ns leihs.borrow.resources.workdays
  (:refer-clojure :exclude [get])
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [clojure.tools.logging :as log]))

(defn base-sqlmap [pool-id]
  (-> (sql/select :*)
      (sql/from :workdays)
      (sql/where [:= :inventory_pool_id pool-id])))

(defn get [tx pool-id]
  (-> pool-id
      base-sqlmap
      sql/format
      (->> (jdbc/query tx))
      first))

(comment
 (let [weekdays [:monday :tuesday :wednesday :thursday :friday :saturday :sunday]]
   (-> (get scratch/tx scratch/pool-id)
       (select-keys weekdays)
       (seq)))
 (base-sqlmap scratch/pool-id))
