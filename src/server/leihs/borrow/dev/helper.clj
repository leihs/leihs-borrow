(ns leihs.borrow.dev.helper
  (:require [clojure.tools.namespace.repl :as ns-tools]))

(ns-tools/disable-reload!)

(def main-args* (atom nil))
