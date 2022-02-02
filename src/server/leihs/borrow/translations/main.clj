(ns leihs.borrow.translations.main
  (:require
    ;[leihs.borrow.run :as run]
    [clj-yaml.core :as yaml]
    [clojure.pprint :refer [pprint]]
    [clojure.tools.cli :as cli :refer [parse-opts]]
    [environ.core :refer [env]]
    [leihs.borrow.translations.core]
    [leihs.core.repl :as repl]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [taoensso.timbre :refer [debug info warn error]])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn dump [options]
  (leihs.borrow.translations.core/dump (:dump-path options))
  (System/exit 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cli-options
  (concat
    [["-h" "--help"]
     [nil "--dump-path DUMP_PATH"
      :default "resources/sql/translations.sql"]]))

(defn main-usage [options-summary & more]
  (->> ["Leihs Borrow"
        ""
        "usage: leihs-borrow [<gopts>] dump-translations [<opts>]"
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
  (let [{:keys [options arguments
                errors summary]} (cli/parse-opts
                                   args cli-options :in-order true)
        cmd (some-> arguments first keyword)
        options (merge gopts options)
        print-summary #(println (main-usage summary {:args args :options options}))]
    (repl/init options)
    (cond (:help options) (print-summary)
          :else (dump options))))
