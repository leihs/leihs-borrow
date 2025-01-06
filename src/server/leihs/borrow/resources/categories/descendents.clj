(ns leihs.borrow.resources.categories.descendents
  (:require
   [hugsql.core :as hugsql]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(hugsql/def-sqlvec-fns "sql/category_tree_snip.sql")
(hugsql/def-sqlvec-fns "sql/descendent_ids.sql")

(defn descendent-ids [tx parent-id]
  (assert (uuid? parent-id))
  (-> {:category-id parent-id
       :category-tree-snip (category-tree-snip)}
      descendent-ids-sqlvec
      (->> (jdbc-query tx))
      (->> (map :id))))
