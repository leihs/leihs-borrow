(ns leihs.borrow.lib.re-graph
  (:require [re-graph.core :as re-graph]
            [leihs.borrow.lib.re-frame :refer [dispatch]]))

(def config {:ws-url nil 
             :http-url "/app/borrow/graphql" 
             :http-parameters {:with-credentials? true}})

(defn init []
  (dispatch [::re-graph/init config]))
