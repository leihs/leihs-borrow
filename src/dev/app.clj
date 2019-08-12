(ns app
  (:require [clojure.repl :as repl]
            [clojure.tools.namespace.repl :as ctnr]
            app-setup))

(ctnr/disable-reload!)

(defn stop
  []
  "As this namespace is excluded from reloading we have to resolve vars
  at runtime, in order not to keep stale references."
  (some-> (find-ns 'app-setup)
          (ns-resolve 'stop)
          (apply [])))

(defn reset
  []
  (stop)
  (if-let [ex (ctnr/refresh :after 'app-setup/run)]
    (repl/pst ex)))
