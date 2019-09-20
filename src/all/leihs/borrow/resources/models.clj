(ns leihs.borrow.resources.models
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [leihs.borrow.resources.categories :as categories]))

(defn get-multiple
  [context _ value]
  (let [tx (-> context :request :tx)]
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
      (sql/merge-where [:in
                        :model_links.model_group_id
                        (categories/descendent-ids tx (:id value))])
      sql/format
      (->> (jdbc/query tx)))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)

