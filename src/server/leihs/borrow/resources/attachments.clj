(ns leihs.borrow.resources.attachments
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [leihs.borrow.paths :refer [path]])
  (:import java.util.Base64))

(def attachment-base-query
  (-> (sql/select :attachments.*)
      (sql/from :attachments)))

(defn get-one [tx id]
  (-> attachment-base-query
      (sql/where [:= :attachments.id [:cast id :uuid]])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn merge-attachment-url [attachment]
  (as-> attachment <>
    (:id <>)
    (hash-map :attachment-id <>)
    (path :attachment <>)
    (str <> "/" (:filename attachment))
    (assoc attachment :attachment-url <>)))

(defn get-multiple [{{tx :tx} :request} _ value]
  (-> attachment-base-query
      (sql/where [:= :attachments.model_id (:id value)])
      sql-format
      (->> (jdbc-query tx))
      (->> (map merge-attachment-url))))

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
