(ns playground.shadow-example
  (:require [reagent.core :as r]))

(def value-a 1)

(defonce value-b 5)

(defn reload! []
  (println "Code updated.")
  (println "Trying values:" value-a value-b))

(defn ^:export run []
  (println "App loaded!"))
