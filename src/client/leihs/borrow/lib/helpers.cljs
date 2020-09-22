(ns leihs.borrow.lib.helpers
  (:require-macros leihs.borrow.lib.helpers)
  (:require [clojure.walk :refer [postwalk walk] :as walk]
            [camel-snake-kebab.core :as csk]
            ["date-fns" :as datefn]))

(def keywordize-keys walk/keywordize-keys)

(defn- tranform-map-keys [transformer map]
  (postwalk #(cond-> %
               (and (keyword? %)
                    (not (qualified-keyword? %)))
               transformer)
            map))

(defn camel-case-keys [m]
  (tranform-map-keys csk/->camelCase m))

(defn kebab-case-keys [m]
  (tranform-map-keys csk/->kebab-case m))

(defn date-format-day [date]
  (datefn/format date "yyyy-MM-dd"))
