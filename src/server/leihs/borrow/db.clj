(ns leihs.borrow.db
  (:require [camel-snake-kebab.core :as csk]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [wharf.core :refer [transform-keys]]))

(defn query [sql tx]
  (->> (jdbc-query tx sql)
       (map #(transform-keys csk/->kebab-case %))))
