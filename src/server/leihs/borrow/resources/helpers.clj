(ns leihs.borrow.resources.helpers
  (:require [clojure.string :as string]))

(defn treat-order-arg
  ([order-specs] (treat-order-arg order-specs nil))
  ([order-specs table]
   (map  (fn [{:keys [attribute direction]}]
           (let [col* (-> attribute name string/lower-case keyword)
                 col** (if table [:. table col*] col*)
                 dir* (-> direction name string/lower-case keyword)]
             [col** dir*]))
         order-specs)))
