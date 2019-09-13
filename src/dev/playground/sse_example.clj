(ns playground.sse-example
  (:require
    [cheshire.core :as json]
    [clojure.core.async :as async]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [compojure.core :refer [defroutes GET]]
    [hiccup.element :refer [javascript-tag]]
    [hiccup.page :refer [html5 include-js include-css]]
    [leihs.borrow.paths :refer [path]]
    [leihs.core.http-cache-buster2 :refer [cache-busted-path]]
    [manifold.deferred :as deferred]
    [ring.adapter.jetty :as jetty]
    ring.core.protocols
    ))

(defn head []
  [:head
   [:link {:rel "icon" :href "data:,"}]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]])

(defn handler
  ([request]
   {:headers {"Content-Type" "text/html"}
    :body (html5
            (head)
            [:body#app
             [:h1 "SSE example"]
             [:pre "check console"]
             ; (include-js "/borrow/js/playground.js")
             ; (javascript-tag "playground.shadow_example.run();")
             (javascript-tag "
                             var source = new EventSource(\"/sse\");

                             source.onmessage= (e) => {
                             const data = JSON.parse(e.data);
                             console.log(data);

                             if (data.msg == \"end\") {
                             console.log(\"Closing the stream.\");
                             source.close();
                             }
                             //update time series graph, tabular data etc
                             };

                             source.onopen = (e) => {
                             console.log(\"connection opened:\" + e);
                             };

                             source.onerror = (e) => {
                             console.log(\"error:\" + e);
                             if (e.readyState == EventSource.CLOSED) {
                             console.log(\"connection closed:\" + e);
                             }
                             source.close();
                             };")])})
  ([request respond _]
   (respond (handler request))))

(extend-type clojure.core.async.impl.channels.ManyToManyChannel
  ring.core.protocols/StreamableResponseBody
  (write-body-to-stream [channel response output-stream]
    (async/go (with-open [writer (io/writer output-stream)]
                (async/loop []
                  (when-let [msg (async/<! channel)]
                    (doto writer (.write msg) (.flush))
                    (recur)))))))

; (def stream-response
;   (partial assoc
;            {:status 200, :headers {"Content-Type" "text/event-stream"}}
;            :body))

(def EOL "\n")

(defn stream-msg [payload]
  (str "data:" (json/generate-string payload) EOL EOL))

(defn sse-handler [_ respond _]
  (let [ch (async/chan)]
    (respond {:status 200,
              :headers {"Content-Type" "text/event-stream"},
              :body ch})
    (async/go (async/>! ch (stream-msg {:val 42}))
              (async/<! (async/timeout 1000))
              (async/>! ch (stream-msg {:val 100}))
              (async/close! ch))))

(defroutes sse
  (GET (path :sse) [] (fn sse-handler [_ respond _]
                        (let [ch (async/chan)]
                          (respond {:status 200,
                                    :headers {"Content-Type" "text/event-stream"},
                                    :body ch})
                          (async/go (async/>! ch (stream-msg {:val 42}))
                                    (async/<! (async/timeout 1000))
                                    (async/>! ch (stream-msg {:val 100}))
                                    (async/close! ch)))))
  (GET (path :sse-example) [] #'handler))

(defonce _server (atom nil))

(defn stop []
  (when-let [server @_server]
    (log/info "Closing jetty server.")
    (.stop server)
    (reset! _server nil)))

(defn start []
  (log/info "starting jetty at port 3333")
  (reset! _server
          (jetty/run-jetty sse {:port 3333
                                :join? false
                                :async? true})))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
