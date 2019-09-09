(ns playground.ws
  (:require [org.httpkit.server :refer [send! on-close on-receive accept]]
            [clojure.tools.logging :as log])
  (:import [org.httpkit.server AsyncChannel]))

(defonce channels (atom #{}))

(defn connect! [channel]
 (log/info "channel open")
 (swap! channels conj channel))

(defn disconnect! [channel status]
 (log/info "channel closed:" status)
 (swap! channels #(remove #{channel} %)))

(defn notify-clients [msg]
 (doseq [channel @channels]
   (send! channel msg)))

; ==========================================================================
(defmacro with-channel
  [request ch-name & body]
  `(let [~ch-name (:async-channel ~request)]
     (if (:websocket? ~request)
       (if-let [key# (get-in ~request [:headers "sec-websocket-key"])]
         (do
           (.sendHandshake ~(with-meta ch-name {:tag `AsyncChannel})
                           {"Upgrade"    "websocket"
                            "Connection" "Upgrade"
                            "Sec-WebSocket-Accept"   (accept key#)
                            "Sec-WebSocket-Protocol" "graphql-ws"})
           ~@body
           {:body ~ch-name})
         {:status 400 :body "missing or bad WebSocket-Key"})
       {:status 400 :body "not websocket protocol"})))
; ==========================================================================

(defn handler [request]
  (log/info (:headers request))
  (with-channel request channel
    (connect! channel)
    (on-close channel (partial disconnect! channel))
    (on-receive channel #(notify-clients %))))
