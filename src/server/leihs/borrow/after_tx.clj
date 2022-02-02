(ns leihs.borrow.after-tx
  (:require
    [taoensso.timbre :refer [debug info warn error]]
    ))


;TODO replace this

(declare ^:dynamic after-tx)

(defn wrap [handler]
  (fn [request]
    (let [response (handler request)]
      (doseq [hook (:after-tx response)] (hook))
      response)))


; TODO with something like

(defn a-better-wrap [handler]
  (fn [request]
    (let [after-tx-hooks* (atom [])
          response (handler (assoc request :after-tx-hooks* after-tx-hooks*))]
      (doseq [hook @after-tx-hooks*]
        (try (hook request response)
             (catch Exception e
               (warn "ignored exception during after-tx-hooks* evaluation " e))
             (catch Throwable e
               (error "Hell broke loose during after-tx-hooks* evaluation" e)
               (throw e))))
      response)))

