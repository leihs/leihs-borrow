(ns leihs.borrow.lib.translate
  (:require-macros [leihs.borrow.lib.translate])
  (:require ["intl-messageformat" :as intl]
            [ajax.core :refer [GET]]
            [clojure.string :as string]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.lib.helpers :as h :refer [spy]]
            [re-frame.db :as db]
            [shadow.resource :as rc]))

(def ^:dynamic *default-path* "Default path to use for locating a key." nil)
(def default-locale :en-GB)
(def path-escape-char \!)
(declare dict)
(def fallbacks {:gsw-CH :de-CH
                :de-CH :en-US
                :en-US :en-GB})

(defn fetch-and-init
  "Fetch translations from server, store them under window property,
  build the translate function using the translations and call the callback."
  [callback]
  (GET (str js/window.location.origin "/my/user/me/translations")
       {:format :json
        :params {:prefix "borrow"}
        :handler #(let [dict (h/keywordize-keys %)]
                    (do (set! js/window.leihsBorrowTranslations dict)
                        (def dict dict)
                        (callback)))}))

(def remove-first-char #(-> % str rest string/join))

(defn qualify [p]
  (cond (= (first p) path-escape-char)
          (remove-first-char p)
        *default-path*
          (str (remove-first-char *default-path*) "." p)
        :else p))

(defn dict-path-keys [dict-path]
  (-> dict-path
      remove-first-char
      qualify
      (string/split #"[\./]")
      (->> (map keyword))))

(defn missing-translation [dict-path]
  (str "{{ missing: " dict-path " }}"))

(defn translate [message locale values]
  (-> message
      (intl/IntlMessageFormat. locale)
      (.format (clj->js values))))

(defn t-base [dict-path values]
  (let [path-keys (dict-path-keys dict-path)]
    (loop [locale (current-user/locale-to-use @db/app-db)]
      (if locale
        (if-let [message (get-in dict (concat path-keys [locale]))]
          (translate message locale values)
          (recur (locale fallbacks)))
        (missing-translation dict-path)))))
