(ns playground.pedestal.interceptors
  (:require [io.pedestal.interceptor.helpers :as interceptor]
            [io.pedestal.http :as http]
            [io.pedestal.log :as log]
            [leihs.borrow.html :as html]))

(def not-found
  "An interceptor that returns a 404 when routing failed to resolve a route."
  (interceptor/after
    ::not-found
    (fn [context]
      (if-not (http/response? (:response context))
        (do (log/meter ::not-found)
            (->> context
                 :request
                 html/not-found-handler
                 (assoc context :response)))
        context))))
