(ns app-setup
  (:require [leihs.borrow.main :as main]
            [leihs.core.http-server :as http-server-1]
            [playground.http-server :as http-server-2]
            [playground.sse-example :as sse]
            playground.lacinia-pedestal))

(defn stop []
  (http-server-1/stop)
  (http-server-2/stop)
  (sse/stop)
  (playground.lacinia-pedestal/stop))

(defn run [& args]
  (apply main/-main "run" args))
