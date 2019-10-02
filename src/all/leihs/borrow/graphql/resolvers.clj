(ns leihs.borrow.graphql.resolvers
  (:require [clojure.tools.logging :as log]
            [leihs.borrow.graphql.mutations :as mutations]
            [leihs.borrow.graphql.queries :as queries]
            [leihs.core.graphql.helpers :refer [transform-values
                                                wrap-resolver-with-error
                                                wrap-resolver-with-camelCase]]))

(def resolvers
  (-> queries/resolvers
      (merge mutations/resolvers)
      (transform-values (comp wrap-resolver-with-error
                              wrap-resolver-with-camelCase))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
