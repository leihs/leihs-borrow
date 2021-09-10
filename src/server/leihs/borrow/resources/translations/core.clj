(ns leihs.borrow.resources.translations.core
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.borrow.resources.translations.definitions :refer [definitions]]))

(def loaded? (atom false))
(def sql-dump-path "resources/all/sql/translations.sql")

(defn keys-in
  "Returns a sequence of all key paths in a given map using DFS walk."
  [m]
  (letfn [(children [node]
            (let [v (get-in m node)]
              (if (map? v)
                (map (fn [x] (conj node x)) (keys v))
                [])))
          (branch? [node] (-> (children node) seq boolean))]
    (->> (keys m)
        (map vector)
         (mapcat #(tree-seq branch? children %)))))

(defn translation-key-paths-with-vals [m]
  (->> (keys-in m)
       (map (fn [k-path]
              (let [v (get-in m k-path)]
                (if (map? v)
                  nil
                  (vector k-path v)))))
       (remove nil?)))

(defn sanitize [s]
  (string/escape s {\' "''"}))

(defn delete-all-borrow [tx]
  (-> (sql/delete-from :translations_default)
      (sql/where ["~~*" :key "borrow.%"])
      sql/format
      (->> (jdbc/execute! tx))))

(defn insert-all-borrow [tx]
  (doseq [[k-path translation] (translation-key-paths-with-vals definitions)]
    (let [locale (last k-path)
          k (->> k-path butlast (map name) (string/join "."))]
      (-> (sql/insert-into :translations_default)
          (sql/values [{:key k, :translation translation, :language_locale (name locale)}])
          sql/format
          (->> (jdbc/execute! tx))))))

(defn dump
  ([] (dump false))
  ([print-detailed-log]
   (print "Dumping translations...")
   (let [f (or (System/getenv "FILE") sql-dump-path)]
     (with-open [w (clojure.java.io/writer f)]
       (let [delete-statement "DELETE FROM translations_default WHERE key like 'borrow.%';\n"]
         (when print-detailed-log (print delete-statement))
         (.write w delete-statement))
       (doseq [[k-path translation] (translation-key-paths-with-vals definitions)]
         (let [locale (last k-path)
               k (->> k-path butlast (map name) (string/join "."))
               insert-statement
               (format "INSERT INTO translations_default (key, translation, language_locale) VALUES ('%s', '%s', '%s');\n"
                       k (sanitize translation) (name locale))]
           (when print-detailed-log (print insert-statement))
           (.write w insert-statement)))))
   (print "done.\n")))

(defn reload []
  (when-not @loaded?
    (log/info "Reloading translations...")
    (let [tx (ds/get-ds)]
      (delete-all-borrow tx)
      (insert-all-borrow tx))
    (reset! loaded? true)
    (log/info "Reloading translations done.")))

(comment (reload))
