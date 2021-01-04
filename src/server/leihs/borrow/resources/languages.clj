(ns leihs.borrow.resources.languages
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [leihs.core.sql :as sql]
            [leihs.borrow.resources.users :as users]))

(defn get-by-locale [tx locale]
  (-> (sql/select :*)
      (sql/from :languages)
      (sql/where [:= :locale locale])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn default [tx]
  (-> (sql/select :languages.*)
      (sql/from :languages)
      (sql/where [:= :languages.default true])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn one-to-use [{{:keys [tx]} :request} _ {user-id :id}]
  (or (some->> user-id
               (users/get-by-id tx)
               :language_locale
               (get-by-locale tx))
      (default tx)))

(defn get-one [{{:keys [tx]} :request} _ {:keys [language-locale]}]
  (get-by-locale tx language-locale))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
