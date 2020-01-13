(ns app
  (:require [clojure.repl :as repl]
            [clojure.tools.namespace.repl :as ctnr]
            app-setup))

(ctnr/disable-reload!)

(defn stop
  "As this namespace is excluded from reloading we have to resolve vars
  at runtime, in order not to keep stale references."
  []
  (some-> (find-ns 'app-setup)
          (ns-resolve 'stop)
          (apply [])))

(defn scratch
  "If user has a scratch namespace, then find the init function and
  if available then call it."
  []
  (some-> (find-ns 'scratch)
          (ns-resolve 'init)
          (apply [])))

(defn refresh
  "Refresh the actual app."
  []
  (if-let [ex (ctnr/refresh :after 'app-setup/run)]
    (repl/pst ex)) )

(defn reset []
  (stop)
  (refresh)
  (scratch)
  nil)
