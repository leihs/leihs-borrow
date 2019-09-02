(ns app-setup
  (:require [leihs.borrow.main :as main]
            [leihs.core.http-server :as http-server]
            playground.lacinia-pedestal))

(defn stop []
  (http-server/stop)
  (playground.lacinia-pedestal/stop))

(defn run [& args]
  (apply main/-main "run" args))
