(ns leihs.borrow.testing
  (:require
   [leihs.core.core :refer [raise]]))

(def max-sleep-secs 5)

(defn resolver [_ {:keys [sleep-secs]} _]
  (if sleep-secs
    (if (> sleep-secs max-sleep-secs)
      (raise "Maximum sleep time exceeded")
      (do (Thread/sleep (* sleep-secs 1000))
          (str "Slept for " sleep-secs " seconds.")))
    "Don't sleep without sleep arg."))

(def query resolver)
(def mutate resolver)
