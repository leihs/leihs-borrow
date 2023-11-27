(ns leihs.borrow.translate
  (:require [leihs.borrow.translate-base :as t-base]
            [leihs.borrow.translations :as translations]
            [leihs.core.locale :refer [get-selected-language]]))

(def default-path nil)

(defn t [dict-path locale]
  (let [path-keys (t-base/dict-path-keys dict-path default-path)]
    (loop [locale (keyword locale)]
      (if locale
        (or (get-in translations/dict (concat path-keys [locale]))
            (recur (locale t-base/fallbacks)))
        (t-base/missing-translation path-keys)))))

(comment
  (let [dict-path :borrow.mail-templates.received.subject]
   ; (t-base/dict-path-keys dict-path default-path)
    (t dict-path :de-CH)))
