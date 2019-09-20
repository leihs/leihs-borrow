(ns leihs.borrow.resources.models
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn get-multiple
  [context _ value]
  (-> (sql/select :models.* [(sql/call :concat_ws
                                       " "
                                       :models.product
                                       :models.version)
                             :name])
      (sql/from :models)
      (sql/merge-join :model_links
                      [:=
                       :models.id
                       :model_links.model_id])
      (sql/merge-where [:=
                        :model_links.model_group_id
                        (:id value)])
      sql/format
      log/spy
      (->> (jdbc/query (-> context :request :tx)))
      log/spy))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)

