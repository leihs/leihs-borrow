(ns leihs.borrow.dev.helper
  (:require [clojure.tools.namespace.repl :as ns-tools]
            [leihs.core.http-server :as http-server]
            [leihs.borrow.main :as main]))

(ns-tools/disable-reload!)

(def main-args* @main/args*)

(def http-server* @http-server/stop-server*)
