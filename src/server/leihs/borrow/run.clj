(ns leihs.borrow.run
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    ;[leihs.borrow.paths]
    [clojure.pprint :refer [pprint]]
    [clojure.tools.cli :as cli :refer [parse-opts]]
    [leihs.borrow.graphql :as graphql]
    [leihs.borrow.legacy :as legacy]
    [leihs.borrow.routes :as routes]
    [leihs.borrow.ssr]
    [leihs.core.db :as db]
    [leihs.core.http-server :as http-server]
    [leihs.core.shutdown :as shutdown]
    [leihs.core.ssr-engine :as ssr-engine]
    [leihs.core.ssr]
    [leihs.core.status :as status]
    [leihs.core.url.jdbc]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [taoensso.timbre :refer [debug info warn error]]
    ))


(defn run [options]
  (catcher/snatch
    {:return-fn (fn [e] (System/exit -1))}
    (info "Invoking run with options: " options)
    (shutdown/init options)
    (legacy/init options)
    (ssr-engine/init options)
    (leihs.core.ssr/init leihs.borrow.ssr/render-page-base)
    (graphql/init)
    (let [status (status/init)]
      (db/init options (:health-check-registry status)))
    (let [http-handler (routes/init)]
      (http-server/start options http-handler))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def cli-options
  (concat
    [["-h" "--help"]
     shutdown/pid-file-option]
    (http-server/cli-options :default-http-port 3250)
    legacy/cli-opts
    db/cli-options))

(defn main-usage [options-summary & more]
  (->> ["leihs-borrow"
        ""
        "usage: leihs-perm [<gopts>] run [<opts>] [<args>]"
        ""
        "Options:"
        options-summary
        ""
        ""
        (when more
          ["-------------------------------------------------------------------"
           (with-out-str (pprint more))
           "-------------------------------------------------------------------"])]
       flatten (clojure.string/join \newline)))

(defn main [gopts args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options :in-order true)
        pass-on-args (->> [options (rest arguments)]
                          flatten (into []))
        options (merge gopts options)]
    (cond
      (:help options) (println (main-usage summary {:args args :options options}))
      :else (run options))))
