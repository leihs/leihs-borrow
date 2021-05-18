(ns leihs.borrow.lib.translate
  (:require-macros [leihs.borrow.lib.translate])
  (:require [tongue.core :as tongue]
            [re-frame.db :as db]
            [ajax.core :refer [GET]]
            [shadow.resource :as rc]
            [cljs.tools.reader.edn :as edn]
            [leihs.borrow.lib.helpers :as h]
            [leihs.borrow.features.current-user.core :as current-user]
            [clojure.string :as string]))

(def ^:dynamic *default-path* "Default path to use for locating a key.")

(def dicts-extensions
  {:tongue/fallback :en-GB})

(defn fetch-and-init
  "Fetch translations from server, store them under window property,
  build the translate function using the translations and call the callback."
  [callback]
  (GET (str js/window.location.origin "/my/user/me/translations")
       {:format :json
        :params {:prefix "borrow"}
        :handler #(do (set! js/window.leihsBorrowTranslations
                            (h/keywordize-keys %))
                      (def translate
                        (-> js/window.leihsBorrowTranslations
                            (merge dicts-extensions)
                            tongue/build-translate))
                      (callback))}))

(defn drop-first-char [s]
  (->> s (drop 1) string/join))

(defn fully-qualify-key [pre k]
  (let [k* (-> k str drop-first-char)
        pre* (-> pre str drop-first-char)]
    (keyword
      (condp re-matches k*
        #"!.*" (drop-first-char k*)
        #".*/.*" (str pre* "." k*)
        (str pre* "/" k*)))))

(defn locale-to-use []
  (or (current-user/locale-to-use @db/app-db)
      :en-GB))

(defn apply-default-path [[a1 & as]]
  (cons (if *default-path*
          (fully-qualify-key *default-path* a1)
          a1)
        as))

(defn t-base [& args]
  (apply translate
         (locale-to-use)
         (apply-default-path args)))

(comment
  ((resolve 'drop-first-char) "Foo")
  (fully-qualify-key :borrow.about-page :title)
  (t :borrow/all))
