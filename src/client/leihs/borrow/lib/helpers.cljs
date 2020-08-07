(ns leihs.borrow.lib.helpers
  (:require [clojure.walk :refer [postwalk walk] :as walk]
            [camel-snake-kebab.core :as csk]))

(def keywordize-keys walk/keywordize-keys)

(defn kebab-case-keys [m]
  (postwalk #(cond-> %
               (and (keyword? %)
                    (not (qualified-keyword? %)))
               csk/->kebab-case)
            m))
