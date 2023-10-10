(ns leihs.borrow.resources.visits
  (:require [clojure.tools.logging :as log]
            [leihs.borrow.resources.reservations :as reservations]
            [taoensso.timbre :as timbre :refer [debug info]]))

(def RELEVANT-STATES #{"submitted" "approved" "rejected" "signed" "closed"})

(defn fulfillment [{{tx :tx-next} :request :as context}
                   _
                   {:keys [reservation-ids]}
                   fulfilled-states]
  (let [rs (reservations/get-by-ids tx reservation-ids)
        fulfilled-quantity (->> rs
                                (filter #(->> % :status (contains? fulfilled-states)))
                                (map :quantity)
                                (apply +))
        to-fulfill-quantity (->> rs
                                 (filter #(->> % :status (contains? RELEVANT-STATES)))
                                 (map :quantity)
                                 (apply +))]
    (when (> to-fulfill-quantity 0)
      {:fulfilled-quantity fulfilled-quantity
       :to-fulfill-quantity to-fulfill-quantity})))

(def approve-fulfillment 
  #(fulfillment %1 %2 %3 #{"approved" "signed" "closed"}))

(def pickup-fulfillment 
  #(fulfillment %1 %2 %3 #{"signed" "closed"}))

(def return-fulfillment 
  #(fulfillment %1 %2 %3 #{"closed"}))
