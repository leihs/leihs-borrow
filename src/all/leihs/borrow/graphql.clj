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
            [leihs.core.graphql.helpers :as helpers]
            [leihs.core.ring-exception :refer [get-cause]]))

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
                   {:request request}))

(defn pure-handler
  [{{query :query} :body, :as request}]
  (let [result (exec-query query request)
        resp {:body result}]
    (if (:errors result)
      (do (log/debug result) (assoc resp :graphql-error true))
      resp)))

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

(defn handler
  [{{query :query} :body, :as request}]
  (let [mutation? (->> query
                       (parse-query-with-exception-handling schema)
                       graphql-parser/operations
                       :type
                       (= :mutation))]
    (if mutation?
      (jdbc/with-db-transaction
        [tx (:tx request)]
        (try (let [response (->> tx
                                 (assoc request :tx)
                                 pure-handler)]
               (when (:graphql-error response)
                 (log/warn "Rolling back transaction because of graphql error")
                 (jdbc/db-set-rollback-only! tx))
               response)
             (catch Throwable th
               (log/warn "Rolling back transaction because of " th)
               (jdbc/db-set-rollback-only! tx)
               (throw th))))
      (pure-handler request))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
