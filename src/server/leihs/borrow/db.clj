(ns leihs.borrow.db
  (:require [camel-snake-kebab.core :as csk]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [wharf.core :refer [transform-keys]]))

(defn query [sql tx]
  (->> (jdbc-query tx sql)
       (map #(transform-keys csk/->kebab-case %))))
