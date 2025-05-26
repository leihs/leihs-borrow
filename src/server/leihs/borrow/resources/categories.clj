(ns leihs.borrow.resources.categories
  (:require [clojure.tools.logging :as log]

            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]

            [hugsql.core :as hugsql]
            [leihs.core.db :as ds]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.paths :refer [path]]
            [leihs.borrow.resources.images :as images]
            [leihs.borrow.resources.models :as models]
            [taoensso.timbre :as timbre :refer [debug spy]]))

(hugsql/def-sqlvec-fns "sql/reservable_root_categories.sql")
(hugsql/def-sqlvec-fns "sql/reservable_child_categories.sql")
(hugsql/def-sqlvec-fns "sql/reservable_categories.sql")
(hugsql/def-sqlvec-fns "sql/with_all_reservable_categories_snip.sql")
(hugsql/def-sqlvec-fns "sql/category_tree_snip.sql")

(comment
  (->> {:limit nil
        :with-all-reservable-categories
        (with-all-reservable-categories-snip
          {:user-id #uuid "c0777d74-668b-5e01-abb5-f8277baa0ea8"
           :and-pool-ids
           (and-pool-ids-snip {:pool-ids
                               [#uuid "8bd16d45-056d-5590-bc7f-12849f034351"]})})
        :category-tree-snip (category-tree-snip-2)}
       reservable-root-categories-sqlvec
       (jdbc-query (ds/get-ds)))

  (->> {:user-id #uuid "c0777d74-668b-5e01-abb5-f8277baa0ea8"
        :category-id #uuid "94915209-2723-530a-92f8-76c0e8ac7ca4"
        :category-tree-snip (category-tree-snip)
        :with-all-reservable-categories
        (with-all-reservable-categories-snip
          {:user-id #uuid "c0777d74-668b-5e01-abb5-f8277baa0ea8"
           :and-pool-ids
           (and-pool-ids-snip {:pool-ids
                               [#uuid "8d3631ee-818b-56d2-9d08-b9369d62d1e1"]})})}
       reservable-child-categories-sqlvec
       (jdbc-query (ds/get-ds)))

  (and-pool-ids-snip {:pool-ids ["foo"]}))

(defn merge-label [tx ids c]
  (let [parent-id (some #(when (= (second %) (:id c)) (first %))
                        (partition 2 1 ids))
        label (when parent-id
                (-> (sql/select :label)
                    (sql/from :model_group_links)
                    (sql/where [:= :parent_id parent-id])
                    (sql/where [:= :child_id (:id c)])
                    sql-format
                    (->> (jdbc-query tx))
                    first :label))]
    (if label
      (assoc c :name label)
      c)))

(defn get-multiple [{{tx :tx} :request user-id ::target-user/id}
                    {:keys [ids pool-ids raise-if-not-all-ids-found]}
                    _]
  (let [categories
        (-> (cond-> {:with-all-reservable-categories
                     (with-all-reservable-categories-snip
                       (cond-> {:user-id user-id}
                         (seq pool-ids)
                         (assoc :and-pool-ids
                                (and-pool-ids-snip {:pool-ids pool-ids}))))
                     :category-tree-snip (category-tree-snip)}
              (seq ids)
              (assoc :where-ids (where-ids-snip {:ids ids})))
            reservable-categories-sqlvec
            (->> (jdbc-query tx))
            (->> (map #(merge-label tx ids %))))]
    (if (and (seq ids)
             raise-if-not-all-ids-found
             (not-every? #(some (set [%]) (map :id categories))
                         ids))
      (throw
       (ex-info
        "Not all categories where found among the reservable ones."
        {}))
      categories)))

(defn get-roots [{{tx :tx} :request user-id ::target-user/id}
                 {:keys [limit pool-ids]}
                 _]
  (-> {:limit (cond->> (str limit) limit (str "LIMIT "))
       :with-all-reservable-categories
       (with-all-reservable-categories-snip
         (cond-> {:user-id user-id}
           (seq pool-ids)
           (assoc :and-pool-ids (and-pool-ids-snip {:pool-ids pool-ids}))))
       :category-tree-snip (category-tree-snip)}
      reservable-root-categories-sqlvec
      (->> (jdbc-query tx))))

(defn get-children [{{tx :tx} :request user-id ::target-user/id}
                    {:keys [pool-ids]}
                    value]
  (-> {:category-id (:id value)
       :with-all-reservable-categories
       (with-all-reservable-categories-snip
         (cond-> {:user-id user-id}
           (seq pool-ids)
           (assoc :and-pool-ids (and-pool-ids-snip {:pool-ids pool-ids}))))
       :category-tree-snip (category-tree-snip)}
      reservable-child-categories-sqlvec
      (->> (jdbc-query tx))))

(def base-sqlmap
  (-> (sql/select-distinct :model_groups.id [:model_groups.name :name])
      (sql/from :model_groups)
      (sql/where [:= :model_groups.type "Category"])
      (sql/order-by [:model_groups.name :asc])))

(defn get-one [{{tx :tx} :request} {:keys [id parent-id]} _]
  (-> base-sqlmap
      (cond-> parent-id
        (-> (sql/select :model_groups.id
                        [[:coalesce
                          :model_group_links.label
                          :model_groups.name] :name])
            (sql/join :model_group_links
                      [:=
                       :model_groups.id
                       :model_group_links.child_id])
            (sql/where [:=
                        :model_group_links.parent_id
                        parent-id])))
      (sql/where [:= :model_groups.id id])
      sql-format
      (->> (jdbc-query tx))
      first))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
