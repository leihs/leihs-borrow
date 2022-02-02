(ns leihs.borrow.translations.core
  (:require
    [clojure.string :as string]
    [clojure.edn :as edn]
    [clojure.java.jdbc :as jdbc]
    [leihs.core.sql :as sql]
    [leihs.core.db :as db]
    [leihs.borrow.translations.definitions :refer [definitions]]
    [taoensso.timbre :refer [debug info warn error]]
    ))

(def loaded? (atom false))

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

(defn dump [^String path]
  (info "dumping translations to " path " ...")
  (with-open [w (clojure.java.io/writer path)]
    (let [delete-statement "DELETE FROM translations_default WHERE key like 'borrow.%';\n"]
      (.write w delete-statement))
    (doseq [[k-path translation] (translation-key-paths-with-vals definitions)]
      (let [locale (last k-path)
            k (->> k-path butlast (map name) (string/join "."))
            insert-statement
            (format "INSERT INTO translations_default (key, translation, language_locale) VALUES ('%s', '%s', '%s');\n"
                    k (sanitize translation) (name locale))]
        (.write w insert-statement))))
  (info "dumped translations."))

(defn reload []
  (when-not @loaded?
    (info "Reloading translations...")
    (let [tx (db/get-ds)]
      (delete-all-borrow tx)
      (insert-all-borrow tx))
    (reset! loaded? true)
    (info "Reloading translations done.")))

(comment (reload))
