(ns leihs.borrow.resources.images
  (:require 
    [clojure.java.jdbc :as jdbc ]
    [clojure.tools.logging :as log]
    [compojure.core :as cpj]
    [leihs.borrow.paths :refer [path]]
    [leihs.core.sql :as sql]
    [logbug.debug :as debug]
    [taoensso.timbre :refer [debug error info spy warn]])
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

(defn set-cache-control-header [response request]
  (-> response
      (assoc-in 
        [:headers "Cache-Control"] 
        (if (get-in request  [:settings :public_image_caching_enabled])
          "public, max-age=31536000, immutable"
          "private"))))

(defn handler-one
  [{tx :tx, {image-id :image-id} :route-params :as request}]
  (if-let [image (get-one tx image-id)]
    (-> image
         :content
         (->> (.decode (Base64/getMimeDecoder))
              (hash-map :body))
         (merge {:headers {"Content-Type" (:content_type image),
                           "Content-Transfer-Encoding" "binary"}})
         (set-cache-control-header request))
    {:status 404}))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
