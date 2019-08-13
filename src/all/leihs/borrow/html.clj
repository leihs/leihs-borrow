(ns leihs.borrow.html
  (:require [ring.util.response :refer [resource-response content-type status]]))

(defn index-html-response
  []
  (-> "public/procure/client/index.html"
      resource-response
      (content-type "text/html")))

#_(defn not-found-handler
    [_]
    (-> (index-html-response)
        (status 404)))

(defn not-found-handler [_]
  (-> {:body "TODO: index/html"}
      (status 404)
      (content-type "text/html")))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
