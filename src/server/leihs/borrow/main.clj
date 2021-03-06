(ns leihs.borrow.main
  (:gen-class)
  (:refer-clojure :exclude [str keyword])
  (:require [clj-pid.core :as pid]
            [clojure.pprint :refer [pprint]]
            [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [leihs.core.core :refer [keyword str]]
            [leihs.core.ds :as ds]
            [leihs.core.http-server :as http-server]
            [leihs.core.shutdown :as shutdown]
            [leihs.core.status :as status]
            [leihs.borrow.cli :as cli]
            [leihs.borrow.graphql :as graphql]
            [leihs.borrow.graphql.connections :as graphql-connections]
            [leihs.borrow.legacy :as legacy]
            [leihs.borrow.routes :as routes]
            [leihs.borrow.resources.translations.core :as translations]
            [logbug.catcher :as catcher]
            [signal.handler]
            ))

(defn- main-usage
  [options-summary & more]
  (->>
    ["Leihs Borrow"
     ""
     "usage: leihs-borrow [<opts>] SCOPE [<scope-opts>] [<args>]"
     ""
     "Options:"
     options-summary
     ""
     ""
     (when more
       ["-------------------------------------------------------------------"
        (with-out-str (pprint more))
        "-------------------------------------------------------------------"])]
    flatten
    (clojure.string/join \newline)))

(defn- run
  [options]
  (catcher/snatch {:return-fn (fn [e] (System/exit -1))}
    (log/info "Invoking run with options: " options)
    ; (settings/init options)
    (shutdown/init options)
    (legacy/init options)
    (graphql/init options)
    (graphql-connections/init options)
    (let [status (status/init)]
      (ds/init (:database-url options)
               (:health-check-registry status)))
    (when (log/spy :info (:load-translations options))
      (translations/reload))
    (let [app-handler (routes/init)]
      (http-server/start (:http-base-url options) app-handler))
    nil))

(defn -main
  [& args]
  (require 'pg-types.all)
  ; ---------------------------------------------------
  ; provide implementation fo render-page-base function
  (require 'leihs.borrow.ssr)
  ; ---------------------------------------------------
  (spec/check-asserts true)

  (let [{:keys [options summary]} (cli/parse (rest args))]
    (letfn [(print-main-usage-summary
              []
              (println (main-usage summary {:args args, :options options})))]
      (if (:help options)
        (print-main-usage-summary)
        (case (-> args
                  first
                  keyword)
          :run (run options)
          (println (print-main-usage-summary)))))))
