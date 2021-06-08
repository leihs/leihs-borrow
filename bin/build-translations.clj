(require '[clojure.edn :as edn]
         '[clojure.string :as string])

(load-file "src/server/leihs/borrow/resources/translations/definitions.clj")
(require '[leihs.borrow.resources.translations.definitions :refer [definitions]])

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

(defn dump []
  (let [f (or (System/getenv "FILE") "bin/translations.sql")]
    (with-open [w (clojure.java.io/writer f)]
      (let [delete-statement "DELETE FROM translations_default WHERE key like 'borrow.%';\n"]
        (print delete-statement)
        (.write w delete-statement))
      (doseq [[locale t-map] definitions]
        (doseq [[k-path translation] (translation-key-paths-with-vals t-map)]
          (let [k (->> k-path (map name) (string/join "."))
                insert-statement (str "INSERT INTO translations_default (key, translation, language_locale) "
                                      "VALUES ('" k "', '" translation "', '" (name locale) "');\n")]
            (print insert-statement)
            (.write w insert-statement)))))))

(dump)
