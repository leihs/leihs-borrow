(ns leihs.borrow.resources.options
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def base-sqlmap
  (-> (sql/select :options.*
                  [[:raw "trim(both ' ' from options.name)"]
                   :name])
      (sql/from :options)
      (sql/order-by [:options.name :asc])))

(defn get-one [{{tx :tx} :request} {:keys [id]} value]
  (-> base-sqlmap
      (sql/where [:= :id (or id (:option-id value))])
      sql-format
      (->> (jdbc-query tx))
      first))
