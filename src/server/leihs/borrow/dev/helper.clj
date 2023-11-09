(ns leihs.borrow.dev.helper
  "For development purposes only. Implements helper function for
  reloading namespaces according to their dependencies."
  (:require [clojure.tools.namespace.repl :as ns-tools]
            [leihs.core.db :as db]
            [leihs.borrow.dev.main :as dev]
            [leihs.borrow.main :as main]))

(defn reload []
  (db/close)
  (db/close-next)
  (ns-tools/refresh)
  (reset! main/args* @dev/main-args*)
  (main/main))

(comment (ns-tools/refresh)
         (reload))
