(ns leihs.borrow.resources.holidays
  (:refer-clojure :exclude [get])
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def columns [:holidays.start_date
              :holidays.end_date
              :holidays.name])

(defn base-sqlmap [pool-id]
  (-> (apply sql/select columns)
      (sql/from :holidays)
      (sql/where [:= :inventory_pool_id pool-id])
      (sql/where [:>= :holidays.end_date [:now]]))) ; only future holidays

(defn get-by-pool-id [tx pool-id]
  (-> pool-id
      base-sqlmap
      sql-format
      (->> (jdbc-query tx))))

(defn get-multiple [{{tx :tx} :request} _ {pool-id :id}]
  (get-by-pool-id tx pool-id))

(comment
  (require '[leihs.core.db :as db])
  (let [pool-id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"
        tx (db/get-ds)]
    (get-multiple {:request {:tx tx}} nil {:id pool-id})))
