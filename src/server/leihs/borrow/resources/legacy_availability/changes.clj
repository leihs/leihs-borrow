(ns leihs.borrow.resources.legacy-availability.changes
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [clojure.data.generators :refer [uuid]]
            [clojure.set :as set]
            [java-time :as t]
            [leihs.borrow.resources.entitlements :as e]
            [leihs.borrow.resources.entitlement-groups :as eg]
            [leihs.borrow.resources.legacy-availability.allocations :as a]
            [leihs.borrow.resources.legacy-availability.queries :as q]
            [leihs.borrow.resources.models.core :as m]
            [leihs.core.db :as db]
            [logbug.debug :as logbug]
            [com.rpl.specter :as s]))

(def UTC-ZONE-ID (java.time.ZoneId/of "UTC"))

(defn local-date
  ([] (t/local-date (t/instant) UTC-ZONE-ID))
  ([date]
   (-> date
       t/local-date
       (.atStartOfDay UTC-ZONE-ID)
       .toLocalDate)))

(defn initial-group-quantity [tx entitlement pool-id]
  (let [max-possible-quantity (count (m/borrowable-items tx (:model_id entitlement) pool-id))]
    (min max-possible-quantity (:quantity entitlement))))

(defn init [tx entitlements pool-id]
  (let [entitlements-map
        (as-> entitlements <>
          (reduce #(assoc %1 (:entitlement_group_id %2) (initial-group-quantity tx %2 pool-id)) {} <>)
          (set/rename-keys <> {nil :general})
          (cond-> <> (nil? (:general <>))
                  (assoc :general 0)))
        initial-group-allocations
        (->> entitlements-map
             (map (fn [e-map]
                    [(first e-map) {:in-quantity (second e-map),
                                    :running-reservations []}]))
             (into {}))]
    {(local-date) initial-group-allocations}))

(def replacement-interval (t/months 1))

(defn late? [reservation]
  (and (-> reservation :returned_date nil?)
       (t/before? (-> reservation :end_date local-date)
                  (local-date))))

(defn being-maintained-until [_model date]
  ; TODO:
  ; At the moment the feature is not used in legacy. 
  ; SELECT distinct(maintenance_period) FROM models => 0
  date)

(defn get-unavailable-from [reservation]
  (if (:item_id reservation)
    (local-date)
    (t/max (local-date (:start_date reservation))
           (local-date))))

(defn get-unavailable-until [reservation model]
  (let [date (t/max (if (late? reservation)
                      (t/plus (local-date) replacement-interval)
                      (local-date (:end_date reservation)))
                    (local-date))]
    (cond->> date
      (> (:maintenance_period model) 0)
      (being-maintained-until model))))

(defn explode-date-range [start end]
  (->> (t/iterate t/plus start (t/days 1))
       (take-while #(not= % (t/plus end (t/days 1))))))

(defn most-recent-before-or-equal [changes date]
  (->> changes
       keys
       (filter #(or (= % date) (t/before? % date)))
       (apply t/max)))

(defn between [changes date1 date2]
  (let [most-recent-date (or (most-recent-before-or-equal changes date1)
                             date1)
        dates-between (set/intersection (set (keys changes))
                                        (set (explode-date-range most-recent-date date2)))]
    (select-keys changes dates-between)))

(defn insert-for-single-date [changes date]
  (if (get changes date)
    changes
    (let [allocs-source-date (most-recent-before-or-equal changes date)
          allocs (get changes allocs-source-date)]
      (assoc changes date allocs))))

(defn insert-for-time-span [changes date1 date2]
  (-> changes
      (insert-for-single-date date1)
      (insert-for-single-date (t/plus date2 (t/days 1)))))

(defn update-allocations [inner-changes allocated-group-id reservation]
  #_(let [g-id (uuid)
          inner-changes
          {(local-date) {g-id {:in-quantity 2 :running-reservations [:res1 :res2]}
                         :general {:in-quantity 0 :running-reservations []}}}]
      (->> inner-changes
           (s/transform [s/MAP-VALS (s/submap [g-id]) s/MAP-VALS :in-quantity] #(- % 1))))
  (let [group-alloc-path [s/MAP-VALS (s/submap [allocated-group-id]) s/MAP-VALS]]
    (->> inner-changes
         (s/transform (conj group-alloc-path :in-quantity) #(- % (:quantity reservation)))
         (s/transform (conj group-alloc-path :running-reservations) #(conj % (:id reservation)))
         (into {}))))

(defn update-inner-changes
  [changes date1 date2 reservation inventory-pool-and-model-group-ids]
  (let [inner-changes (between changes date1 date2)
        allocated-group-id (a/get-group-id reservation
                                           inner-changes
                                           inventory-pool-and-model-group-ids)]
    (merge changes
           (update-allocations inner-changes allocated-group-id reservation))))

(defn extend-with
  [changes reservation model inventory-pool-and-model-group-ids]
  (let [unavailable-from (get-unavailable-from reservation)
        unavailable-until (get-unavailable-until reservation model)]
    (-> changes
        (insert-for-time-span unavailable-from unavailable-until)
        (update-inner-changes unavailable-from
                              unavailable-until
                              reservation
                              inventory-pool-and-model-group-ids))))

(defn main
  ([tx model-id pool-id] (main tx model-id pool-id nil))
  ([tx model-id pool-id exclude-res-ids]
   (let [model (m/get-one-by-id tx model-id)
         running-reservations (q/running-reservations tx
                                                      model-id
                                                      pool-id
                                                      exclude-res-ids)
         entitlements (e/get-for-model-and-pool tx model-id pool-id)
         inventory-pool-and-model-group-ids
         (eg/get-inventory-pool-and-model-group-ids tx model-id pool-id)
         initial-changes (init tx entitlements pool-id)]
     (reduce (fn [changes reservation]
               (extend-with changes
                            reservation
                            model
                            inventory-pool-and-model-group-ids))
             initial-changes
             running-reservations))))

(comment (let [model-id #uuid "041d82c9-c02f-4f6e-bf15-805c49796911"
               pool-id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"
               tx (db/get-ds)]
           (-> (main tx model-id pool-id)
               sort
               #_(between (local-date) (t/plus (local-date) (t/days 15))))))
