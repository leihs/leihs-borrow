(ns leihs.borrow.connections
  (:refer-clojure :exclude [first])
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn row-cursor [column]
  (as-> column <>
    (sql/call :cast <> :text)
    (sql/call :replace <> "-" "")
    (sql/call :decode <> "hex")
    (sql/call :encode <> "base64")))

(defn with-cursored-result [sqlmap after]
  (-> [[:primary_result sqlmap]
       [:cursored_result
        (-> (sql/select
              :primary_result.*
              [(row-cursor :primary_result.id) :row_cursor]
              [(sql/raw "row_number(*) over ()") :row_number])
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
      sql/format
      (->> (jdbc/query tx))
      empty?
      not))

(defn cursored-sqlmap [sqlmap after limit]
  (-> (with-cursored-result sqlmap after)
      (sql/select :cursored_result.*)
      (sql/from :cursored_result)
      (cond-> after
        (-> (sql/merge-from :cursor_row)
            (sql/merge-where [:>
                              :cursored_result.row_number
                              :cursor_row.row_number])))
      (cond-> limit (sql/limit limit))))

(defn assoc-total-count [result-map tx sqlmap]
  (assoc result-map
         :total-count
         (-> sqlmap
             (sql/select [(sql/call :count :*) :row_count])
             (dissoc :modifiers :order-by) 
             sql/format
             (->> (jdbc/query tx))
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
                               sql/format
                               (->> (jdbc/query tx))
                               empty?
                               not))}))

(defn wrap-in-nodes-and-edges [rows]
  (->> rows
       (map #(hash-map :node %
                       :cursor (:row_cursor %)))
       (hash-map :edges)))

(defn wrap [sqlmap-fn
            {{:keys [tx]} :request :as context}
            {:keys [after first] :as args}
            value
            post-process]
  (let [sqlmap (sqlmap-fn context args value)]
    (if (and after (not (after-cursor-row-exists? tx sqlmap after)))
      (throw (ex-info "After cursor row does not exist!" {}))
      (let [rows (-> sqlmap
                     (cursored-sqlmap after first)
                     sql/format
                     (->> (jdbc/query tx))
                     (post-process context args value))]
        (-> rows
            wrap-in-nodes-and-edges
            (assoc-total-count tx sqlmap)
            (assoc-page-info tx
                             sqlmap
                             first
                             (-> rows last :row_cursor)))))))
(comment
  (-> (sql/select :*)
      (sql/modifiers :distinct)
      (sql/from :foo)
      (sql/order-by :bar)
      (dissoc :order-by :modifiers)
      sql/format))
