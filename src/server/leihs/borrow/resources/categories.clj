(ns leihs.borrow.resources.categories
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.borrow.paths :refer [path]]
            [leihs.borrow.resources.images :as images]
            [leihs.borrow.resources.models :as models]))

(hugsql/def-sqlvec-fns "sql/root_categories.sql")
(hugsql/def-sqlvec-fns "sql/child_categories.sql")

(comment
  (->> {:user-id "c0777d74-668b-5e01-abb5-f8277baa0ea8"}
       all-reservable-root-categories-sqlvec
       (jdbc/query (ds/get-ds)))
  (->> {:user-id "c0777d74-668b-5e01-abb5-f8277baa0ea8"
        :category-id "94915209-2723-530a-92f8-76c0e8ac7ca4"}
       all-reservable-child-categories-sqlvec
       (jdbc/query (ds/get-ds))))

(defn get-roots [{{:keys [tx] user-id :target-user-id} :request}
                 {:keys [limit]}
                 _]
  (-> {:user-id user-id
       :limit (cond->> (str limit) limit (str "LIMIT "))}
      all-reservable-root-categories-sqlvec
      (->> (jdbc/query tx))))

(defn get-children [{{:keys [tx] user-id :target-user-id} :request}
                    _
                    value]
  (->> {:user-id user-id :category-id (:id value)}
       all-reservable-child-categories-sqlvec
       (jdbc/query tx)))

(def base-sqlmap
  (-> (sql/select :model_groups.id [:model_groups.name :name])
      (sql/modifiers :distinct)
      (sql/from :model_groups)
      (sql/merge-where [:= :model_groups.type "Category"])
      (sql/order-by [:name :asc])))

(defn get-one [{{:keys [tx]} :request} {:keys [id parent-id]} _]
  (-> base-sqlmap
      (cond-> parent-id
        (-> (sql/select :model_groups.id
                        [(sql/call :coalesce
                                   :model_group_links.label
                                   :model_groups.name) :name])
            (sql/merge-join :model_group_links
                            [:=
                             :model_groups.id
                             :model_group_links.child_id])
            (sql/merge-where [:=
                              :model_group_links.parent_id
                              parent-id])))
      (sql/merge-where [:= :model_groups.id id])
      sql/format
      (->> (jdbc/query tx))
      first))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
