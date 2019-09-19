(ns leihs.borrow.paths
  (:require [bidi.bidi :refer [path-for]]
            [bidi.verbose :refer [branch leaf param]]
            leihs.core.paths))

(def paths
  (branch
    ""
    leihs.core.paths/core-paths
    (branch
      "/borrow"
      (leaf "/shutdown" :shutdown)
      (leaf "/graphql" :graphql)
      (leaf "/status" :status))
    (leaf true :not-found)))

(reset! leihs.core.paths/paths* paths)

(def path leihs.core.paths/path)
