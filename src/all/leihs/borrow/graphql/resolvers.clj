(ns leihs.borrow.graphql.resolvers
  (:require [clojure.tools.logging :as log]
            [leihs.borrow.graphql.mutations :as mutations]
            [leihs.borrow.graphql.queries :as queries]
            [leihs.core.graphql.helpers :refer [transform-resolvers
                                                wrap-resolver-with-error
                                                wrap-resolver-with-kebab-case
                                                wrap-resolver-with-camelCase]]))

(def resolvers
  (-> queries/resolvers
      (merge mutations/resolvers)
      (transform-resolvers (comp wrap-resolver-with-error
                                 wrap-resolver-with-camelCase
                                 wrap-resolver-with-kebab-case))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
