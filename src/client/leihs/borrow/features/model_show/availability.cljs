(ns leihs.borrow.features.model-show.availability
  (:require
   ["date-fns" :as datefn]
   [leihs.borrow.lib.helpers :as h]))

(def MONTHS-BUFFER 6)
(defn with-future-buffer [date]
  (datefn/lastDayOfMonth (datefn/addMonths date (- MONTHS-BUFFER 1))))

(def max-date (datefn/addYears (js/Date.) 10))

(defn merge-availability [old-one new-one]
  (map (fn [{{pool-id :id} :inventory-pool :as old-for-pool}]
         (if-let [new-dates-for-pool (->> new-one
                                          (filter #(-> %
                                                       :inventory-pool
                                                       :id
                                                       (= pool-id)))
                                          first
                                          :dates)]
           (update-in old-for-pool
                      [:dates]
                      concat
                      new-dates-for-pool)
           old-for-pool))
       old-one))

(defn update-availability [container new-availability]
  (if (empty? (:availability container))
    (assoc container :availability new-availability)
    (update container
            :availability
            merge-availability
            new-availability)))

(defn set-loading-as-ended [container end-date success]
  (let [end-date-js (js/Date. end-date)
        fetching-until-date (-> container :fetching-until-date js/Date.)
        fetched-until-date (-> container :fetched-until-date js/Date.)]
    (merge container
           (when-not (datefn/isAfter fetching-until-date end-date-js)
             {:fetching-until-date nil})
           (when success
             (when-not (datefn/isAfter fetched-until-date end-date-js)
               {:fetched-until-date end-date})))))