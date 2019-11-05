(ns leihs.borrow.client.components
  (:require
   #_[reagent.core :as r]))


(defn merge-props [defaults givens]
  (merge
   defaults
   givens
   {:class (->> (get defaults :class)
                (concat (flatten [(get givens :class)]))
                (map keyword)
                set)}
   {:style (merge (get defaults :style)
                  (get givens :style))}))

(defn button [label active? props]
  (let 
   [base-cls
    #{:text-white  :font-bold
      :py-0 :px-2
      :rounded-full
      :border-solid :border-4 :border-black}]
    [:button
     (merge-props
      {:type :button
       :disabled (not active?)
       :class (cond-> base-cls
                active? (conj :bg-black :border-black)
                (not active?) (conj :bg-grey :border-grey))}
      props)
     label]))