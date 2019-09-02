(ns app-setup
  (:require [leihs.borrow.main :as main]
            [leihs.core.http-server :as http-server]
            playground.lacinia-pedestal
            playground.pedestal))

(defn stop []
  (http-server/stop)
  (playground.lacinia-pedestal/stop)
  (playground.pedestal/stop))

(defn run [& args]
  (apply main/-main "run" args))
