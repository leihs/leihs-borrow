(ns leihs.borrow.resources.models
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [leihs.borrow.resources.categories :as categories]))

(defn get-multiple
  [context _ value]
  (let [tx (-> context :request :tx)
        parent-category-id (:id value)
        category-ids (as-> parent-category-id <>
                       (categories/descendent-ids tx <>)
                       (conj <> parent-category-id))]
    (-> (sql/select :models.*
                    [(sql/call :concat_ws
                               " "
                               :models.product
                               :models.version)
                     :name])
        (sql/from :models)
        (sql/merge-join :model_links
                        [:=
                         :models.id
                         :model_links.model_id])
        (cond-> (seq category-ids)
          (sql/merge-where [:in
                            :model_links.model_group_id
                            category-ids]))
        sql/format
        (->> (jdbc/query tx)))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)

