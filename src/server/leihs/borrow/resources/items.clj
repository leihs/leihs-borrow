(ns leihs.borrow.resources.items
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def base-sqlmap
  (-> (sql/select :items.*)
      (sql/from :items)
      (sql/order-by [:inventory_code :asc])))

(defn get-one [{{tx :tx} :request} {:keys [id]} value]
  (-> base-sqlmap
      (sql/where [:= :id (or id (:item-id value))])
      sql-format
      (->> (jdbc-query tx))
      first))
