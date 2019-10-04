(ns leihs.borrow.resources.images
  (:require [clojure.java.jdbc :as jdbc]
            [compojure.core :as cpj]
            [leihs.core.sql :as sql])
  (:import java.util.Base64))

(def image-base-query
  (-> (sql/select :images.*)
      (sql/from :images)))

(defn get-one [tx id]
  (-> image-base-query
      (sql/where [:= :images.id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn get-for-category [tx id]
  (-> image-base-query
      (sql/merge-where [:= :images.target_type "ModelGroup"])
      (sql/merge-where [:= :images.target_id id])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn handler-one
  [{tx :tx, {image-id :image-id} :route-params}]
  (if-let [image (get-one tx image-id)]
    (->> image
         :content
         (.decode (Base64/getMimeDecoder))
         (hash-map :body)
         (merge {:headers {"Content-Type" (:content_type image),
                           "Content-Transfer-Encoding" "binary"}}))
    {:status 404}))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
