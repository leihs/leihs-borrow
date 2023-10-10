(ns leihs.borrow.graphql
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia :as lacinia]
    [com.walmartlabs.lacinia.parser :as graphql-parser]
    [com.walmartlabs.lacinia.resolve :as graphql-resolve]
    [com.walmartlabs.lacinia.schema :as graphql-schema]
    [com.walmartlabs.lacinia.util :as graphql-util]
    [leihs.borrow.graphql.resolvers :as resolvers]
    [leihs.borrow.graphql.mutations :as mutations]
    [leihs.borrow.graphql.scalars :as scalars]
    [leihs.core.db :as ds]
    [leihs.core.graphql :as core-graphql]
    [leihs.core.graphql.helpers :as helpers]
    [taoensso.timbre :refer [debug info warn error spy]]
    ))

(def lacinia-enable-timing* (atom false))

(defn load-schema []
  (or (some-> (io/resource "schema.edn")
              slurp edn/read-string
              (graphql-util/attach-resolvers resolvers/resolvers)
              (graphql-util/attach-scalar-transformers scalars/scalars)
              graphql-schema/compile)
      (throw (ex-info "Failed to load schema" {}))))

(defn init [options]
  (core-graphql/init-schema! (load-schema))
  (core-graphql/init-audit-exceptions! mutations/audit-exceptions))

;###############################################################################

(defn exec-query [{{query :query vars :variables} :body :as request}]
  (debug "graphql query" query "with variables" vars)
  (if (or (:authenticated-entity request) (core-graphql/get-schema? request))
    (lacinia/execute (core-graphql/schema)
                     query
                     vars
                     (cond-> {:request request}
                       @lacinia-enable-timing*
                       (assoc ::lacinia/enable-timing? true)))
    (helpers/error-as-graphql-object 401 "No authenticated user.")))

(def keys-order [:error :data :extensions])

(defn rearrange-keys [m]
  (into (sorted-map-by #(- (.indexOf keys-order %1)
                           (.indexOf keys-order %2)))
        m))

(defn handler [request]
  (let [result (-> request
                   exec-query
                   (cond-> @lacinia-enable-timing* helpers/attach-overall-timing)
                   rearrange-keys)
        resp {:status 200, :body result}]
    (if (:errors result)
      (do (debug result) (assoc resp :graphql-error true))
      resp)))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
