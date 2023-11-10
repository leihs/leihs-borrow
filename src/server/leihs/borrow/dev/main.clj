(ns leihs.borrow.dev.main
  "For development purposes only. Implements helper function for
  reloading namespaces according to their dependencies."
  (:require [clojure.tools.namespace.repl :as ns-tools]
            [leihs.core.db :as db]
            [leihs.borrow.dev.helper :as helper]
            [leihs.borrow.main :as main]))

(defn start []
  (reset! main/args* @helper/main-args*)
  (main/main))

(defn reload []
  (db/close)
  (db/close-next)
  (reset! helper/main-args* @main/args*)
  (ns-tools/refresh :after 'leihs.borrow.dev.main/start))

(comment #_(init)
         (reload))
