(ns leihs.borrow.resources.app-settings
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def base-sqlmap (-> (sql/select :settings.*)
                     (sql/from :settings)))

(defn get-app-settings [{{tx :tx} :request} _ _]
  (let [row (-> base-sqlmap
                sql-format
                (->> (jdbc-query tx))
                first)]
    {:logo-light (:logo_light row)
     :logo-dark  (:logo_dark row)}))
