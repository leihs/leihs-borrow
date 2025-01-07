(ns leihs.borrow.resources.categories.descendents
  (:require
   [hugsql.core :as hugsql]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(hugsql/def-sqlvec-fns "sql/descendent_ids.sql")

(defn descendent-ids [tx category-id]
  (-> {:category-id category-id}
      descendent-ids-sqlvec
      (->> (jdbc-query tx))
      (->> (map :id))))
