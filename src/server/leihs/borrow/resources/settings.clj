(ns leihs.borrow.resources.settings
  (:refer-clojure :exclude [get])
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn get [tx]
  (-> (sql/select :*)
      (sql/from :settings)
      sql/format
      (->> (jdbc/query tx))
      first))
