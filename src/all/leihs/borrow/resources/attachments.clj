(ns leihs.borrow.resources.attachments
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [compojure.core :as cpj]
            [leihs.borrow.paths :refer [path]]
            [leihs.core.sql :as sql])
  (:import java.util.Base64))

(def attachment-base-query
  (-> (sql/select :attachments.*)
      (sql/from :attachments)))

(defn get-one [tx id]
  (-> attachment-base-query
      (sql/where [:= :attachments.id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn merge-attachment-url [attachment]
  (->> attachment
       :id
       (hash-map :attachment-id)
       (path :attachment)
       (assoc attachment :url)))

(defn get-multiple [{{:keys [tx]} :request} _ value]
  (-> attachment-base-query
      (sql/merge-where [:= :attachments.model_id (:id value)])
      sql/format
      (as-> <> (jdbc/query tx <> {:row-fn merge-attachment-url}))))

(defn handler-one
  [{tx :tx, {attachment-id :attachment-id} :route-params}]
  (if-let [attachment (get-one tx attachment-id)]
    (->> attachment
         :content
         (.decode (Base64/getMimeDecoder))
         (hash-map :body)
         (merge {:headers {"Content-Type" (:content_type attachment),
                           "Content-Transfer-Encoding" "binary"}}))
    {:status 404}))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
