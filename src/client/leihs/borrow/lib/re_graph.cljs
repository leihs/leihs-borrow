(ns leihs.borrow.lib.re-graph
  (:require [re-graph.core :as re-graph]
            [leihs.borrow.lib.re-frame :refer [dispatch]]))

(def config {:ws nil 
             :http {:url "/app/borrow/graphql"}})

(defn init []
  (dispatch [::re-graph/init config]))
