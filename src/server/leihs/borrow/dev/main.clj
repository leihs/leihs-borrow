(ns leihs.borrow.dev.main
  "For development purposes only. Implements helper function for
  reloading namespaces according to their dependencies."
  (:require [clojure.tools.namespace.repl :as ns-tools]
            [leihs.core.db :as db]
            [leihs.core.http-server :as http-server]
            [leihs.borrow.dev.helper :as helper]
            [leihs.borrow.main :as main]))

(defn start []
  (reset! main/args* helper/main-args*)
  (reset! http-server/stop-server* helper/http-server*)
  (main/main))

(defn reload []
  (db/close)
  (db/close-next)
  (ns-tools/refresh :after 'leihs.borrow.dev.main/start))

(comment (ns-tools/refresh-all)
         (ns-tools/refresh)
         (reload))
