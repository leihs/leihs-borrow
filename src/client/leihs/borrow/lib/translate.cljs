(ns leihs.borrow.lib.translate
  (:require-macros [leihs.borrow.lib.translate])
  (:require ["/leihs-ui-client-side-external-react" :as UI]
            ; [leihs.borrow.features.languages.core :as languages]
            ["date-fns/locale" :as date-locale]
            [cljs.test :refer-macros [deftest is testing run-tests]]
            [clojure.string :as string]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.lib.helpers :as h :refer [spy log format]]
            [leihs.borrow.lib.re-frame :refer [dispatch-sync reg-sub reg-event-db reg-event-fx]]
            [leihs.borrow.translations :as translations]
            [re-frame.db :as db]))

(def ^:dynamic *default-path* "Default path to use for locating a key." nil)
(def path-escape-char \!)

(def fallbacks {:gsw-CH :de-CH
                :de-CH :en-GB
                :es :en-GB
                :en-US :en-GB
                :fr-CH :en-GB})

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

(defn missing-translation [path-keys]
  (->> path-keys
       (map name)
       (string/join ".")
       (format "{{ missing: %s }}")))

(defn translate [message locale values]
  (-> message
      (UI/IntlMessageFormat. (name locale))
      (.format (clj->js values))))

(reg-sub ::text-locale
         :<- [::current-user/locale-to-use]
         (fn [l _] (case l
                     :de-CH "de-CH"
                     :gsw-CH "de-CH"
                     "en-GB")))

(reg-sub ::date-locale
         :<- [::current-user/locale-to-use]
         (fn [l _] (case l
                     :de-CH date-locale/de
                     :gsw-CH date-locale/de
                     date-locale/enGB)))


(defn t-base [dict-path values]
  (let [path-keys (dict-path-keys dict-path)]
    (loop [locale (or (current-user/get-locale-to-use @db/app-db) :en-GB)]
      (if locale
        (if-let [message (get-in translations/dict (concat path-keys [locale]))]
          (translate message locale values)
          (recur (locale fallbacks)))
        (missing-translation path-keys)))))

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
    (with-redefs [translations/dict {:test {:gsw-CH "gsw-CH"
                                            :en-GB "en-GB"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) "gsw-CH")
          "Translation exists for language to use."))
    (with-redefs [translations/dict {:test {:de-CH "de-CH"
                                            :en-GB "en-GB"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) "de-CH")
          "Fallback one level."))
    (with-redefs [translations/dict {:test {:en-GB "en-GB"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) "en-GB")
          "Fallback all the way through."))
    (with-redefs [translations/dict {:test {:de-CH "Sie haben {itemCount, number} Gegenstände!"}}
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
    (with-redefs [translations/dict {:test {:fr "fr"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use {:locale "gsw-CH"}}}})]
      (is (= (t-base :test nil) (missing-translation [:test]))
          "No translation for language to use."))
    (with-redefs [translations/dict {:test {:de-CH "de-CH"}}
                  db/app-db (atom {:ls
                                   {::current-user/data
                                    {:language-to-use nil}}})]
      (is (= (t-base :test nil) (missing-translation [:test]))
          "No language to use."))))

(comment (run-tests))
