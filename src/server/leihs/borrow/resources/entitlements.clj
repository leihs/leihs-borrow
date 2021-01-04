(ns leihs.borrow.resources.entitlements
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [leihs.core.sql :as sql]))

(hugsql/def-sqlvec-fns "sql/entitlements.sql")

(comment
  (->> {:and-pool-ids-1 (and-pool-ids-1-snip
                          {:pool-ids ["8bd16d45-056d-5590-bc7f-12849f034351"]})
        :and-pool-ids-2 (and-pool-ids-2-snip
                          {:pool-ids ["8bd16d45-056d-5590-bc7f-12849f034351"]})
        :and-model-ids-1 (and-model-ids-1-snip
                           {:model-ids ["4fab1679-2347-514f-a0ff-116f955a484f"]})
        :and-model-ids-2 (and-model-ids-2-snip
                           {:model-ids ["4fab1679-2347-514f-a0ff-116f955a484f"]})
        :where-user-id (where-user-id-snip {:user-id scratch/user-id})}
       all-entitlements-sqlvec
       (jdbc/query scratch/tx)))

(defn total-quantity [tx user-id model-id pool-ids]
  (let [params (cond-> {}
                 (seq pool-ids)
                 (assoc :and-pool-ids-1
                        (and-pool-ids-1-snip {:pool-ids pool-ids})
                        :and-pool-ids-2
                        (and-pool-ids-2-snip {:pool-ids pool-ids}))
                 model-id
                 (assoc :and-model-ids-1
                        (and-model-ids-1-snip {:model-ids [model-id]})
                        :and-model-ids-2
                        (and-model-ids-2-snip {:model-ids [model-id]}))
                 user-id
                 (assoc :where-user-id
                        (where-user-id-snip {:user-id user-id})))]
    (->> params
         all-entitlements-sqlvec
         (jdbc/query tx)
         (map :quantity)
         (reduce +))))

(comment
  (total-quantity scratch/tx
                  scratch/user-id
                  "4fab1679-2347-514f-a0ff-116f955a484f"
                  ["8bd16d45-056d-5590-bc7f-12849f034351"]))

(def all-sql
  (first (all-entitlements-sqlvec)))

(comment 
  (->> [all-sql] (jdbc/query (leihs.core.ds/get-ds))))
