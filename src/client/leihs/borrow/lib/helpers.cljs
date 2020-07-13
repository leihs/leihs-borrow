(ns leihs.borrow.lib.helpers
  (:require [clojure.walk :refer [walk]]
            [camel-snake-kebab.core :as csk]))

(defn kebabize-keys [m]
  (walk (fn [[k v]] [(csk/->kebab-case k) v]) identity m))

(comment
  (kebabize-keys {:foo 1 :barBaz 2}))
