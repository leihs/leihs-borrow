(ns leihs.borrow.resources.languages
  (:require [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [leihs.borrow.resources.users.shared :as users]))

(def base-sqlmap (-> (sql/select :languages.*)
                     (sql/from :languages)
                     (sql/where [:= :active true])
                     (sql/order-by [:name :asc])))

(defn get-by-locale [tx locale]
  (-> base-sqlmap
      (sql/where [:= :locale locale])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn default [tx]
  (-> base-sqlmap
      (sql/where [:= :languages.default true])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn get-the-one-to-use [tx user-id]
  (or (some->> user-id
               (users/get-by-id tx)
               :language_locale
               (get-by-locale tx))
      (default tx)))

(defn one-to-use [{{tx :tx} :request} _ {user-id :id}]
  (get-the-one-to-use tx user-id))

(defn get-one [{{tx :tx} :request} _ {:keys [language-locale]}]
  (get-by-locale tx language-locale))

(defn get-multiple [{{tx :tx} :request} _ _]
  (-> base-sqlmap sql-format (->> (jdbc-query tx))))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
