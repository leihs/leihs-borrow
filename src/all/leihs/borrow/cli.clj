(ns leihs.borrow.cli
  (:refer-clojure :exclude [str keyword])
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :as cli]
            [clojure.repl :refer [doc]]
            [leihs.core.core :refer [presence keyword str]]
            [leihs.core.url.http :as http-url]
            [leihs.core.shutdown :as shutdown]
            [leihs.core.url.jdbc :as jdbc-url]))

(def defaults
  {:LEIHS_BORROW_HTTP_BASE_URL "http://localhost:3250"
   :LEIHS_DATABASE_URL "jdbc:postgresql://leihs:leihs@localhost:5432/leihs?min-pool-size=1&max-pool-size=5"}) 

(defn- get-from-env
  [kw]
  (-> (System/getenv)
      (get (str kw) nil)
      presence))

(defn env-or-default
  [kw]
  (or (get-from-env kw) (get defaults kw nil)))

(defn extend-pg-params
  [params]
  (assoc params
    :password (or (:password params) (System/getenv "PGPASSWORD"))
    :username (or (:username params) (System/getenv "PGUSER"))
    :port (or (:port params) (System/getenv "PGPORT"))))

(def cli-options
  [["-h" "--help"],
   ["-b" "--http-base-url LEIHS_BORROW_HTTP_BASE_URL"
    (->> defaults
         :LEIHS_BORROW_HTTP_BASE_URL
         (str "default: "))
    :default (-> :LEIHS_BORROW_HTTP_BASE_URL
                 env-or-default
                 http-url/parse-base-url)
    :parse-fn http-url/parse-base-url]
   ["-d" "--database-url LEIHS_DATABASE_URL"
    (str "default: " (:LEIHS_DATABASE_URL defaults))
    :default
    (->> :LEIHS_DATABASE_URL
         env-or-default
         jdbc-url/dissect
         extend-pg-params)
    :parse-fn
    #(-> %
         jdbc-url/dissect
         extend-pg-params)],
   shutdown/pid-file-option])

(defn parse
  [args]
  (cli/parse-opts args cli-options :in-order true))
