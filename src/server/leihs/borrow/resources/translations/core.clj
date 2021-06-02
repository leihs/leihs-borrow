(ns leihs.borrow.resources.translations.core
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.borrow.resources.translations.definitions :refer [definitions]]))

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

(defn delete-all-borrow [tx]
  (-> (sql/delete-from :default_translations)
      (sql/where ["~~*" :key "borrow.%"])
      sql/format
      (->> (jdbc/execute! tx))))

(defn insert-all-borrow [tx]
  (doseq [[locale t-map] definitions]
    (doseq [[k-path translation] (translation-key-paths-with-vals t-map)]
      (let [k (->> k-path (map name) (string/join "."))]
        (-> (sql/insert-into :default_translations)
            (sql/values [{:key k, :translation translation, :language_locale (name locale)}])
            sql/format
            (->> (jdbc/execute! tx)))))))

(defn reload []
  (when-not @loaded?
    (log/info "Reloading translations...")
    (let [tx (ds/get-ds)]
      (delete-all-borrow tx)
      (insert-all-borrow tx))
    (reset! loaded? true)
    (log/info "Reloading translations done.")))

(comment (reload))
