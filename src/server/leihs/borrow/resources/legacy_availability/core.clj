(ns leihs.borrow.resources.legacy-availability.core
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [java-time :as t]
            [com.rpl.specter :as s]
            [leihs.borrow.resources.legacy-availability.changes :as ch]
            [leihs.borrow.resources.entitlement-groups :as eg]
            [leihs.core.db :as db]
            [logbug.debug :as logbug]))

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
                                   (ch/local-date end-date))
         non-negative (fn [n] (if (neg? n) 0 n))]
     (->> inner-changes
          vals
          (map (fn [allocs]
                 (-> allocs
                     (select-keys group-ids)
                     vals
                     (->> (map :in-quantity)
                          (apply +)))))
          (apply min)
          non-negative))))

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

(comment (let [model-id #uuid "041d82c9-c02f-4f6e-bf15-805c49796911"
               pool-id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"
               tx (db/get-ds)
               user-id #uuid "c0777d74-668b-5e01-abb5-f8277baa0ea8"
               start-date #_(ch/local-date) (t/plus (ch/local-date) #_start-date (t/days 1))
               end-date #_(ch/local-date) (t/plus (ch/local-date) #_start-date (t/days 1))
               changes (ch/main tx model-id pool-id nil)]
           (maximum-available-in-pool-and-period-summed-for-groups tx
                                                                   model-id
                                                                   user-id
                                                                   start-date
                                                                   end-date
                                                                   pool-id)))
