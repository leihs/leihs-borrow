(ns leihs.borrow.graphql
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.parser :as graphql-parser]
            [com.walmartlabs.lacinia.schema :as graphql-schema]
            [com.walmartlabs.lacinia.util :as graphql-util]
            [leihs.borrow.graphql.scalars :as scalars]
            [leihs.borrow.graphql.resolvers :as resolvers]
            [leihs.borrow.authenticate :as authenticate]
            [leihs.core.ds :as ds]
            [leihs.core.graphql.helpers :as helpers]
            [leihs.core.ring-exception :refer [get-cause]]))

(def lacinia-enable-timing (atom nil))

(defn init [options]
  (reset! lacinia-enable-timing
          (:leihs-borrow-lacinia-enable-timing options))
  (if @lacinia-enable-timing
    (log/info (str "Lacinia timing is enabled."))))

(defn load-schema
  []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (graphql-util/attach-resolvers resolvers/resolvers)
      (graphql-util/attach-scalar-transformers scalars/scalars)
      graphql-schema/compile))

(def schema (load-schema))

(defn exec-query
  [query-string request]
  (log/debug "graphql query" query-string
             "with variables" (-> request
                                  :body
                                  :variables))
  (lacinia/execute schema
                   query-string
                   (-> request
                       :body
                       :variables)
                   (cond-> {:request request}
                     @lacinia-enable-timing
                     (assoc ::lacinia/enable-timing? true))))

(defn base-handler
  [{{query :query} :body, :as request}]
  (binding [ds/after-tx nil]
    (let [result (-> query 
                     (exec-query request)
                     (cond->
                       @lacinia-enable-timing
                       helpers/attach-overall-timing))
          resp {:body result
                :after-tx ds/after-tx}]
      (if (:errors result)
        (do (log/debug result) (assoc resp :graphql-error true))
        resp))))

(defn parse-query-with-exception-handling
  [schema query]
  (try (graphql-parser/parse-query schema query)
       (catch Throwable e*
         (let [e (get-cause e*)
               m (.getMessage e*)
               n (-> e*
                     .getClass
                     .getSimpleName)]
           (log/warn (or m n))
           (log/debug e)
           (helpers/error-as-graphql-object "API_ERROR" m)))))

(defn schema? [query]
  (->> query
       (parse-query-with-exception-handling schema)
       graphql-parser/operations
       :operations
       (= #{:__schema})))

(defn mutation? [query]
  (->> query
       (parse-query-with-exception-handling schema)
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
                 (log/warn "Rolling back transaction because of graphql error")
                 (jdbc/db-set-rollback-only! tx))
               response)
             (catch Throwable th
               (log/warn "Rolling back transaction because of " th)
               (jdbc/db-set-rollback-only! tx)
               (throw th))))
      (base-handler request)))

(defn handler
  [{{query :query} :body, :as request}]
  (if (schema? query)
    (base-handler request)
    (-> handler-with-operation-type-check
        authenticate/wrap-base
        (apply [request]))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
