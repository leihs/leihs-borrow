(ns leihs.borrow.resources.categories
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.borrow.paths :refer [path]]
            [leihs.borrow.resources.images :as images]
            [leihs.borrow.resources.models :as models]))

(def base-sqlmap
  (-> (sql/select :model_groups.id :model_groups.name)
      (sql/modifiers :distinct)
      (sql/from :model_groups)
      (sql/merge-join :model_links
                      [:= :model_groups.id :model_links.model_group_id])
      (sql/merge-join :models
                      [:= :model_links.model_id :models.id])
      (sql/merge-where [:= :model_groups.type "Category"])))

(defn extend-based-on-args [sqlmap {:keys [limit offset ids root-only]}]
  (-> sqlmap
      (cond-> root-only
        (sql/merge-where
          [:not
           [:exists
            (-> (sql/select true)
                (sql/from :model_group_links)
                (sql/merge-where [:=
                                  :model_groups.id
                                  :model_group_links.child_id]))]]))
      (cond-> limit (sql/limit limit))
      (cond-> offset (sql/offset offset))
      (cond-> (seq ids)
        (sql/merge-where [:in :model_groups.id ids]))))

(defn get-children
  [{{:keys [tx authenticated-entity]} :request} args value]
  (-> base-sqlmap
      (models/merge-reservable-conditions (:id authenticated-entity))
      (extend-based-on-args args)
      (cond-> value
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
                              (:id value)])))
      sql/format
      (->> (jdbc/query tx))))

(defn get-parents
  [{{:keys [tx authenticated-entity]} :request} args value]
  (-> base-sqlmap
      (sql/select :model_groups.id
                  [(sql/call :coalesce
                             :model_group_links.label
                             :model_groups.name) :name])
      (sql/merge-join :model_group_links
                      [:=
                       :model_groups.id
                       :model_group_links.parent_id])
      (sql/merge-where [:=
                        :model_group_links.child_id
                        (:id value)])
      (models/merge-reservable-conditions (:id authenticated-entity))
      (extend-based-on-args args)
      sql/format
      (->> (jdbc/query tx))))

(defn labels [tx cat-id]
  (-> (sql/select :model_groups.id
                  [(sql/call :coalesce
                             :model_group_links.label
                             :model_groups.name) :label]
                  :model_group_links.parent_id)
      (sql/from :model_groups)
      (sql/left-join :model_group_links
                     [:=
                      :model_group_links.child_id
                      :model_groups.id])
      (sql/where [:= :model_groups.id cat-id])
      (sql/format)
      (->> (jdbc/query tx))))

(defn get-vertices
  [{{:keys [tx]} :request :as context} args value]
  (case (::lacinia/container-type-name context)
    :Model (let [cat-ids (-> (sql/select :model_groups.id)
                             (sql/from :model_links)
                             (sql/join :model_groups
                                       [:=
                                        :model_groups.id
                                        :model_links.model_group_id])
                             (sql/where [:and
                                         [:= :model_links.model_id (:id value)]
                                         [:= :model_groups.type "Category"]])
                             sql/format
                             (->> (jdbc/query tx)
                                  (map :id)))]
             (if (not-empty cat-ids)
               (->> cat-ids
                    (map (partial labels tx))
                    flatten)
               []))
    :CategoryVertex (if-let [parent-id (:parent-id value)]
                      (labels tx parent-id)
                      [])))

(defn get-multiple
  [{{:keys [tx authenticated-entity]} :request :as context} args value]
  (-> base-sqlmap
      (models/merge-reservable-conditions (:id authenticated-entity))
      (extend-based-on-args args)
      (cond-> (and value
                   (= (::lacinia/container-type-name context) :Model))
        (-> (sql/select :model_groups.id
                        [(sql/call :coalesce
                                   :model_group_links.label
                                   :model_groups.name) :name])
            (sql/merge-where [:= :models.id (:id value)])))
      sql/format
      (->> (jdbc/query tx))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
