(ns leihs.borrow.run
  (:require
   clojure.string
   [clojure.pprint :refer [pprint]]
   [clojure.tools.cli :as cli]
   [leihs.borrow.graphql :as graphql]
   [leihs.borrow.routes :as routes]
   [leihs.core.db :as db]
   [leihs.core.http-server :as http-server]
   [leihs.core.shutdown :as shutdown]
   [leihs.core.status :as status]
   [leihs.core.url.jdbc]
   [logbug.catcher :as catcher]
   [taoensso.timbre :refer [info]]))

(defn run [options]
  (catcher/snatch
   {:return-fn (shutdown/run-return-fn options)}
   (info "Invoking run with options: " options)
   (shutdown/init options)
   (graphql/init options)
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
  (let [{:keys [options summary]}
        (cli/parse-opts args cli-options :in-order true)
        options (merge gopts options)]
    (cond
      (:help options) (println (main-usage summary {:args args :options options}))
      :else (run options))))
