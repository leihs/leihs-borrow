(ns leihs.borrow.main
  (:gen-class)
  (:refer-clojure :exclude [str keyword])
  (:require [clj-pid.core :as pid]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [leihs.core.core :refer [keyword str]]
            [leihs.core.ds :as ds]
            [leihs.core.http-server :as http-server]
            [leihs.core.shutdown :as shutdown]
            [leihs.core.status :as status]
            [leihs.borrow.cli :as cli]
            [leihs.borrow.routes :as routes]
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
                  ; (shutdown/init options)
                  (let [status (status/init)]
                    (ds/init (:database-url options)
                             (:health-check-registry status)))
                  (let [app-handler (routes/init)]
                    (http-server/start (:http-base-url options) app-handler))
                  nil))

(defn -main
  [& args]
  (require 'pg-types.all)
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
