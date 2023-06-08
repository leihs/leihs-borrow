(ns leihs.borrow.translate-base
  #?(:clj (:refer-clojure :exclude [format]))
  (:require [clojure.string :as string]
            [leihs.borrow.translations :as translations]))

(def path-escape-char \!)

(def fallbacks {:gsw-CH :de-CH
                :de-CH :en-GB
                :es :en-GB
                :en-US :en-GB
                :fr-CH :en-GB})

(def remove-first-char #(-> % str rest string/join))

(defn qualify [p default-path] 
  (cond (= (first p) path-escape-char)
        (remove-first-char p)
        default-path
        (str (remove-first-char default-path) "." p)
        :else p))

(defn dict-path-keys [dict-path default-path]
  (-> dict-path
      remove-first-char
      (qualify default-path)
      (string/split #"[\./]")
      (->> (map keyword))))

(def format #?(:clj clojure.core/format :cljs leihs.borrow.lib.helpers/format))

(defn missing-translation [path-keys]
  (->> path-keys
       (map name)
       (string/join ".")
       (format "{{ missing: %s }}")))
