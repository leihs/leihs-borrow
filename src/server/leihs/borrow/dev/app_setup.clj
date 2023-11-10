(ns leihs.borrow.dev.app-setup
  (:require [leihs.borrow.main :as main]
            [leihs.borrow.graphql :as graphql]
            [leihs.core.db :as db]
            [leihs.core.http-server :as http-server]))

(defn stop []
  (db/close)
  (db/close-next)
  (http-server/stop))

(defn run [& args]
  #_(alter-var-root #'graphql/schema
                    (constantly (graphql/load-schema)))
  (apply main/-main "run" args))
