(ns leihs.borrow.resources.legacy-availability.core
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [clojure.java.jdbc :as jdbc]
            [java-time :as t]
            [com.rpl.specter :as s]
            [leihs.borrow.resources.legacy-availability.changes :as ch]
            [leihs.borrow.resources.entitlement-groups :as eg]
            [leihs.core.db :as db]
            [leihs.core.sql :as sql]))

(comment (s/select [s/ALL (fn [[k v]] (= k :a))] {:a 1 :b 2}))

(defn maximum-available-in-pool-and-period-summed-for-groups
  "In a single inventory pool."
  ([tx model-id user-id start-date end-date pool-id]
   (maximum-available-in-pool-and-period-summed-for-groups tx
                                                           model-id
                                                           user-id
                                                           start-date
                                                           end-date
                                                           pool-id
                                                           nil))

  ([tx model-id user-id start-date end-date pool-id exclude-res-ids]
   (let [changes (ch/main tx model-id pool-id exclude-res-ids)
         user-group-ids (eg/get-user-group-ids tx user-id)
         group-ids (concat [:general] user-group-ids)
         inner-changes (ch/between changes
                                   (ch/local-date start-date)
                                   (ch/local-date end-date))]
     (->> inner-changes
          vals
          (map (fn [allocs]
                 (-> allocs
                     (select-keys group-ids)
                     vals
                     (->> (map :in-quantity)
                          (map #(if (< % 0) 0 %))
                          (apply +)))))
          (apply min)))))

(defn maximum-available-in-period-summed-for-groups
  "In a collection of inventory pools."
  ([tx model-id user-id start-date end-date pool-ids]
   (maximum-available-in-period-summed-for-groups tx
                                                  model-id
                                                  user-id
                                                  start-date
                                                  end-date
                                                  pool-ids
                                                  nil))
  ([tx model-id user-id start-date end-date pool-ids exclude-res-ids]
   (->> pool-ids
        (map #(maximum-available-in-pool-and-period-summed-for-groups
               tx
               model-id
               user-id
               start-date
               end-date
               %
               exclude-res-ids))
        (apply +))))

(comment (let [model-id "804a50c1-2329-5d5b-9884-340f43833514"
               pool-id "8bd16d45-056d-5590-bc7f-12849f034351"
               tx (db/get-ds)
               user-id "c0777d74-668b-5e01-abb5-f8277baa0ea8"
               start-date (ch/local-date)
               end-date (t/plus (ch/local-date) #_start-date (t/days 30))
               changes (ch/main tx model-id pool-id nil)
               ]
           (maximum-available-in-pool-and-period-summed-for-groups tx
                                                                   model-id
                                                                   user-id
                                                                   start-date
                                                                   end-date
                                                                   pool-id)))
