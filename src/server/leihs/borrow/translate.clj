(ns leihs.borrow.translate
  (:require [leihs.borrow.translate-base :as t-base]
            [leihs.borrow.translations :as translations]))

(def default-path nil)

(defn t [dict-path locale]
  (let [path-keys (t-base/dict-path-keys dict-path default-path)]
    (loop [locale (keyword locale)]
      (if locale
        (or (get-in translations/dict (concat path-keys [locale]))
            (recur (locale t-base/fallbacks)))
        (t-base/missing-translation path-keys)))))
