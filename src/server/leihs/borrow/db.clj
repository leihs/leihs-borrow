(ns leihs.borrow.db
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]))

(defn query [sql tx]
  (jdbc/query tx
              sql
              {:row-fn #(transform-keys csk/->kebab-case %)}))
