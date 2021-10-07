(ns leihs.borrow.lib.translate
  (:require-macros [leihs.borrow.lib.translate])
  (:require ["/leihs-ui-client-side-external-react" :as UI]
            #_["intl-messageformat" :as intl]
            [ajax.core :refer [GET]]
            [clojure.string :as string]
            [cljs.test :refer-macros [deftest is testing run-tests]]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.lib.helpers :as h :refer [spy log]]
            [leihs.borrow.lib.re-frame :refer [reg-sub]]
            [re-frame.db :as db]
            [shadow.resource :as rc]))

(def ^:dynamic *default-path* "Default path to use for locating a key." nil)
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
      (UI/IntlMessageFormat. (name locale))
      (.format (clj->js values))))

(defn locale-to-use [db]
  (-> db :ls ::current-user/data :language-to-use :locale keyword))

(reg-sub ::locale-to-use
         (fn [db _] (locale-to-use db)))

(defn t-base [dict-path values]
  (let [path-keys (dict-path-keys dict-path)]
    (loop [locale (locale-to-use @db/app-db)]
      (if locale
        (if-let [message (get-in dict (concat path-keys [locale]))]
          (translate message locale values)
          (recur (locale fallbacks)))
        (missing-translation dict-path)))))

; ============================= TESTS ==============================

(deftest test-translate
  (let [m "You have {itemCount, plural,
          =0 {no items}
          one {# item}
          other {# items}
          }."]
    (is (= (translate m :de-CH {:itemCount 1})
           "You have 1 item.")
        "Standard happy path.")))

(deftest test-t-base
  (testing "locales and fallbacks"
    (with-redefs [dict {:test {:gsw-CH "gsw-CH"
                               :en-GB "en-GB"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) "gsw-CH")
          "Translation exists for language to use."))
    (with-redefs [dict {:test {:de-CH "de-CH"
                               :en-GB "en-GB"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) "de-CH")
          "Fallback one level."))
    (with-redefs [dict {:test {:en-GB "en-GB"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) "en-GB")
          "Fallback all the way through."))
    (with-redefs [dict {:test {:de-CH "Sie haben {itemCount, number} Gegenstände!"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "de-CH"}}}})]
      (is (= (-> (UI/IntlMessageFormat. "" "de-CH")
                 .resolvedOptions
                 .-locale)
             "de-CH")
          "Test correct locale resolution.")
      (is (= (t-base :test {:itemCount 1000}) "Sie haben 1’000 Gegenstände!")
          "Test number formatting with locale."))
    (with-redefs [dict {:test {:fr "fr"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) (missing-translation :test))
          "No translation for language to use."))
    (with-redefs [dict {:test {:de-CH "de-CH"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use nil}}})]
      (is (= (t-base :test nil) (missing-translation :test))
          "No language to use."))
    ))

(comment (run-tests))
