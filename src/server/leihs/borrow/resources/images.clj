(ns leihs.borrow.resources.images
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [compojure.core :as cpj]
            [leihs.borrow.paths :refer [path]]
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

(defn merge-image-url [image]
  (->> image
       :id
       (hash-map :image-id)
       (path :image)
       (assoc image :image-url)))

(defn query [sql tx]
  (jdbc/query tx sql {:row-fn merge-image-url}))

(defn get-cover [{{:keys [tx]} :request} _ {model-id :id}]
  (-> image-base-query
      (sql/join :models [:= :images.id :models.cover_image_id])
      (sql/where [:= :models.id model-id])
      sql/format
      (query tx)
      first))

(defn get-multiple [{{:keys [tx]} :request} _ value]
  (-> image-base-query
      (sql/merge-where [:= :images.target_id (:id value)])
      (sql/merge-where [:= :images.parent_id nil])
      sql/format
      (query tx)))

(defn get-multiple-thumbnails [{{:keys [tx]} :request} _ value]
  (-> image-base-query
      (sql/merge-where [:= :images.parent_id (:id value)])
      sql/format
      (query tx)))

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
