(ns leihs.borrow.graphql.connections
  (:refer-clojure :exclude [first])
  (:require [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [taoensso.timbre :refer [debug info warn error spy]]))

(def PER-PAGE
  "Standard per-page limit for fetching and displaying results."
  20)

(def intervene-per-page-default
  "The internal per-page limit used by the intervene function."
  (atom nil))

(defn init [{pp :special-per-page-default}]
  (reset! intervene-per-page-default pp))

(defn row-cursor [column]
  (as-> column <>
    [:cast <> :text]
    [:replace <> "-" ""]
    [:decode <> "hex"]
    [:encode <> "base64"]))

(defn with-cursored-result [sqlmap after]
  (-> [[:primary_result sqlmap]
       [:cursored_result
        (-> (sql/select
             :primary_result.*
             [(row-cursor :primary_result.id) :row_cursor]
             [[:raw "row_number(*) over ()"] :row_number])
            (sql/from :primary_result))]]
      (cond-> after
        (conj [:cursor_row
               (-> (sql/select :*)
                   (sql/from :cursored_result)
                   (sql/where [:= :row_cursor after]))]))
      (->> (apply sql/with))))

(defn after-cursor-row-exists? [tx sqlmap after]
  (-> sqlmap
      (with-cursored-result after)
      (sql/select :*)
      (sql/from :cursor_row)
      sql-format
      (->> (jdbc-query tx))
      empty?
      not))

(defn cursored-sqlmap [sqlmap after limit]
  (-> (with-cursored-result sqlmap after)
      (sql/select :cursored_result.*)
      (sql/from :cursored_result)
      (cond-> after
        (-> (sql/from :cursor_row)
            (sql/where [:>
                        :cursored_result.row_number
                        :cursor_row.row_number])))
      (cond-> limit (sql/limit limit))))

(defn assoc-total-count [result-map tx sqlmap]
  (assoc result-map
         :total-count
         (-> (sql/select [[:count :*] :row_count])
             (sql/from [(-> sqlmap (dissoc :order-by))
                        :tmp])
             sql-format
             (->> (jdbc-query tx))
             clojure.core/first
             :row_count)))

(defn assoc-page-info [result-map tx sqlmap first end-cursor]
  (assoc result-map
         :page-info
         {:end-cursor end-cursor
          :has-next-page (if (or (not first) (not end-cursor))
                           false
                           (-> sqlmap
                               (cursored-sqlmap end-cursor 1)
                               sql-format
                               (->> (jdbc-query tx))
                               empty?
                               not))}))

(defn wrap-in-nodes-and-edges [rows]
  (->> rows
       (map #(hash-map :node %
                       :cursor (:row_cursor %)))
       (hash-map :edges)))

(defn wrap
  ([sqlmap-fn context args value]
   (wrap sqlmap-fn context args value nil))
  ([sqlmap-fn
    {{tx :tx} :request :as context}
    {:keys [after first] :as args}
    value
    post-process]
   (let [sqlmap (sqlmap-fn context args value)
         first (or first PER-PAGE)]
     (if (and after (not (after-cursor-row-exists? tx sqlmap after)))
       (throw (ex-info "After cursor row does not exist!" {}))
       (let [rows (-> sqlmap
                      (cursored-sqlmap after first)
                      sql-format
                      (->> (jdbc-query tx))
                      (cond-> post-process post-process))]
         (-> rows
             wrap-in-nodes-and-edges
             (assoc-total-count tx sqlmap)
             (assoc-page-info tx
                              sqlmap
                              first
                              (-> rows last :row_cursor))))))))

(defn intervene
  "Offers additional intervene possibilities for edges. It calls (paginates via) 
  the connection function (`conn-fn`) and adjusts the edges repeatedly (via
  `intervene-fn`) until the per-page `limit` is reached (or no further page is
  available). The internal per-page limit is controlled via `batch-size`."
  ([conn-fn intervene-fn limit]
   (intervene conn-fn intervene-fn @intervene-per-page-default limit))
  ([conn-fn intervene-fn batch-size limit]
   (let [limit (or limit PER-PAGE)]
     (loop [{:keys [page-info edges] :as conn} (conn-fn {:first batch-size})
            result-edges []]
       (let [avail-edges (intervene-fn edges)
             result-edges* (concat result-edges avail-edges)]
         (if (or (>= (count result-edges*) limit) (not (:has-next-page page-info)))
           (-> conn
               (assoc :edges (take limit result-edges*))
               (as-> <> (assoc-in <>
                                  [:page-info :end-cursor]
                                  (-> <> :edges last :cursor)))
               (assoc-in [:page-info :has-next-page]
                         (> (count result-edges*) limit)) ; FIXME: in negative case there is NO GUARANTEE...?
               (assoc :total-count nil))
           (recur (conn-fn {:first batch-size :after (:end-cursor page-info)})
                  result-edges*)))))))
