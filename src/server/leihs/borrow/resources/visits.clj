(ns leihs.borrow.resources.visits
  (:require [clojure.tools.logging :as log]
            [leihs.borrow.resources.reservations :as reservations]
            [taoensso.timbre :as timbre :refer [debug info]]))

(def RELEVANT-STATES #{"submitted" "approved" "rejected" "signed" "closed"})

(defn fulfillment [{{tx :tx} :request :as context}
                   _
                   {:keys [reservation-ids]}
                   fulfilled-pred]
  (let [rs (reservations/get-by-ids tx reservation-ids)
        fulfilled-quantity (->> rs
                                (filter fulfilled-pred)
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
  #(fulfillment %1 %2 %3 (fn [r] (contains? #{"approved" "signed" "closed"} (:status r)))))

(def pickup-fulfillment
  #(fulfillment %1 %2 %3 (fn [r] (contains? #{"signed" "closed"} (:status r)))))

(def return-fulfillment
  #(fulfillment %1 %2 %3 (fn [r] (or (= "closed" (:status r))
                                     (some? (:sent_back_to_main_location_at r))))))
