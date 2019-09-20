(ns leihs.borrow.resources.categories
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn get-main-multiple
  [context _ value]
  (-> (sql/select :*)
      (sql/from :model_groups)
      (sql/merge-where [:= :type "Category"])
      (sql/merge-where
        [:not
         [:exists
          (-> (sql/select true)
              (sql/from :model_group_links)
              (sql/merge-where [:=
                                :model_groups.id
                                :model_group_links.child_id]))]])
      sql/format
      (->> (jdbc/query (-> context :request :tx)))))

(defn get-multiple
  [context _ value]
  (-> (sql/select :model_groups.id
                  [(sql/call :coalesce
                             :model_group_links.label
                             :model_groups.name) :name])
      (sql/from :model_groups)
      (sql/merge-join :model_group_links
                      [:=
                       :model_groups.id
                       :model_group_links.child_id])
      (sql/merge-where [:= :model_groups.type "Category"])
      (sql/merge-where [:=
                        :model_group_links.parent_id
                        (:id value)])
      sql/format
      (->> (jdbc/query (-> context :request :tx)))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)

