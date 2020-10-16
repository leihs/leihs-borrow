(ns leihs.borrow.lib.re-graph
  (:require [re-graph.core :as re-graph]
            [leihs.core.constants :as constants]
            [leihs.borrow.csrf :as csrf]
            [leihs.borrow.lib.re-frame :refer [dispatch]]))

(def headers
  {constants/ANTI_CSRF_TOKEN_HEADER_NAME csrf/token})

(def config
  {:ws nil 
   :http {:url "/app/borrow/graphql"
          :impl {:headers headers}}})

(defn init []
  (dispatch [::re-graph/init config]))
