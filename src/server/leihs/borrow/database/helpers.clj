(ns leihs.borrow.database.helpers
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(defn columns [tx table-name]
  (-> (sql/select :column_name)
      (sql/from :information_schema.columns)
      (sql/where [:= :table_name table-name])
      sql-format
      (->> (jdbc-query tx)
           (map (comp keyword
                      (partial str table-name ".")
                      :column_name)))))
