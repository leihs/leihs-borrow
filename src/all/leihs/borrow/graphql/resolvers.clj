(ns leihs.borrow.graphql.resolvers
  (:require [clojure.tools.logging :as log]
            [leihs.borrow.graphql.mutations :as mutations]
            [leihs.borrow.graphql.queries :as queries]
            [leihs.core.graphql.helpers :refer [wrap-map-with-error]]))

(def resolvers
  (-> queries/resolvers
      (merge mutations/resolvers)
      wrap-map-with-error))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
