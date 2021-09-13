(ns leihs.borrow.resources.visits
  (:require [leihs.borrow.resources.reservations :as reservations]))

(defn pickup-fulfillment [{{:keys [tx]} :request :as context}
                          args
                          {:keys [reservation-ids]}]
  (let [rs (reservations/get-by-ids tx reservation-ids)
        fulfilled-quantity (->> rs
                                (filter #(->> % :status (contains? #{"signed" "closed"})))
                                (map :quantity)
                                (apply +))
        to-fulfill-quantity (->> rs
                                 (filter #(->> % :status (contains? #{"approved" "signed" "closed"})))
                                 (map :quantity)
                                 (apply +))]
    (when (> to-fulfill-quantity 0)
      {:fulfilled-quantity fulfilled-quantity
       :to-fulfill-quantity to-fulfill-quantity})))

(defn return-fulfillment [{{:keys [tx]} :request :as context}
                          args
                          {:keys [reservation-ids]}]
  (let [rs (reservations/get-by-ids tx reservation-ids)
        fulfilled-quantity (->> rs
                                (filter #(-> % :status (= "closed")))
                                (map :quantity)
                                (apply +))
        to-fulfill-quantity (->> rs
                                 (filter #(->> % :status (contains? #{"signed" "closed"})))
                                 (map :quantity)
                                 (apply +))]
    (when (> to-fulfill-quantity 0)
      {:fulfilled-quantity fulfilled-quantity
       :to-fulfill-quantity to-fulfill-quantity})))
