(ns leihs.borrow.paths
  (:require [bidi.bidi :refer [path-for]]
            [bidi.verbose :refer [branch leaf param]]
            leihs.core.paths))

(def paths
  (branch
    ""
    leihs.core.paths/core-paths
    (leaf "/graphql-ws" :graphql-ws)
    (leaf "/graphql-ws-2" :graphql-ws-2)
    (branch
      "/borrow"
      (leaf "/shutdown" :shutdown)
      (leaf "/graphql" :graphql)
      (leaf "/status" :status)
      
      ;;; DEV
      (leaf "/reagent-example" :reagent-example)
      (leaf "/reframe-example" :reframe-example)
      (leaf "/regraph-example" :regraph-example)
      (leaf "/shadow-example" :shadow-example)
      (leaf "/regraph-ring" :regraph-ring)
      ;;;

      )
    (leaf true :not-found)))

(reset! leihs.core.paths/paths* paths)

(def path leihs.core.paths/path)
