(ns leihs.borrow.testing)

(defn resolver [_ {:keys [sleep-secs]} _]
  (if sleep-secs
    (do (Thread/sleep (* sleep-secs 1000))
        (str "Slept for " sleep-secs " seconds."))
    "Don't sleep without sleep arg."))

(def query resolver)
(def mutate resolver)
