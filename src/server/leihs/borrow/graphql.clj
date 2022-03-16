(ns leihs.borrow.graphql
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.java.jdbc :as jdbc]
    [com.walmartlabs.lacinia :as lacinia]
    [com.walmartlabs.lacinia.parser :as graphql-parser]
    [com.walmartlabs.lacinia.resolve :as graphql-resolve]
    [com.walmartlabs.lacinia.schema :as graphql-schema]
    [com.walmartlabs.lacinia.util :as graphql-util]
    [leihs.borrow.after-tx :as after-tx]
    [leihs.borrow.graphql.resolvers :as resolvers]
    [leihs.borrow.graphql.scalars :as scalars]
    [leihs.core.db :as ds]
    [leihs.core.graphql.helpers :as helpers]
    [leihs.core.ring-exception :refer [get-cause]]
    [taoensso.timbre :refer [debug info warn error spy]]
    ))

(def lacinia-enable-timing (atom nil))

(defn init [options]
  (reset! lacinia-enable-timing
          (:leihs-borrow-lacinia-enable-timing options))
  (if @lacinia-enable-timing
    (info (str "Lacinia timing is enabled."))))

;###############################################################################

(def schema* (atom nil))


(defn load-schema! []
  (or (some-> (io/resource "schema.edn")
              slurp edn/read-string
              (graphql-util/attach-resolvers resolvers/resolvers)
              (graphql-util/attach-scalar-transformers scalars/scalars)
              graphql-schema/compile)
      (throw (ex-info "Failed to load schema" {}))))



(defn init-schema! []
  (reset! schema* (load-schema!))
  (or @schema* ))

(defn schema []
  (or @schema* (throw (ex-info  "Schema not initialized " {}))))

;###############################################################################


(defn parse-query-with-exception-handling
  [schema query]
  (try (graphql-parser/parse-query schema query)
       (catch Throwable e*
         (let [e (get-cause e*)
               m (.getMessage e*)
               n (-> e*
                     .getClass
                     .getSimpleName)]
           (warn (or m n))
           (debug e)
           (helpers/error-as-graphql-object "API_ERROR" m)))))

(defn schema? [query]
  (->> query
       (parse-query-with-exception-handling (schema))
       graphql-parser/operations
       :operations
       (= #{:__schema})))



(defn exec-query
  [query-string request]
  (debug "graphql query" query-string
             "with variables" (-> request
                                  :body
                                  :variables))
  (if (or (:authenticated-entity request) (schema? query-string))
    (lacinia/execute (schema)
                     query-string
                     (-> request
                         :body
                         :variables)
                     (cond-> {:request request}
                       @lacinia-enable-timing
                       (assoc ::lacinia/enable-timing? true)))
    (helpers/error-as-graphql-object 401 "No authenticated user.")))

(def keys-order [:error :data :extensions])

(defn rearrange-keys [m]
  (into (sorted-map-by #(- (.indexOf keys-order %1)
                           (.indexOf keys-order %2)))
        m))

(defn base-handler
  [{{query :query} :body, :as request}]
  (let [result (-> query
                   (exec-query request)
                   (cond-> @lacinia-enable-timing helpers/attach-overall-timing)
                   rearrange-keys)
        resp {:body result}]
    (if (:errors result)
      (do (debug result) (assoc resp :graphql-error true))
      resp)))

(defn mutation? [query]
  (->> query
       (parse-query-with-exception-handling (schema))
       graphql-parser/operations
       :type
       (= :mutation)))

(defn handler-with-operation-type-check [{{query :query} :body, :as request}]
  (if (mutation? query)
      (jdbc/with-db-transaction
        [tx (:tx request)]
        (try (let [response (->> tx
                                 (assoc request :tx)
                                 base-handler)]
               (when (:graphql-error response)
                 (warn "Rolling back transaction because of graphql error "
                       response)
                 (jdbc/db-set-rollback-only! tx))
               response)
             (catch Throwable th
               (warn "Rolling back transaction because of " th)
               (jdbc/db-set-rollback-only! tx)
               (throw th))))
      (base-handler request)))

(defn handler
  [{{query :query} :body, :as request}]
  (if (schema? query)
    (base-handler request)
    (handler-with-operation-type-check request)))


;#### debug ###################################################################

(defn init []
  (info "Initializing graphQL schema...")
  (init-schema!)
  (info "initialized graphQL schema."))



;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
