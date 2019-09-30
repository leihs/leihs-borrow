(ns app-setup
  (:require [leihs.borrow.main :as main]
            [leihs.borrow.graphql :as graphql]
            [leihs.core.http-server :as http-server]))

(defn stop []
  (http-server/stop))

(defn run [& args]
  (alter-var-root #'graphql/schema
                  (constantly (graphql/load-schema)))
  (apply main/-main "run" args))
