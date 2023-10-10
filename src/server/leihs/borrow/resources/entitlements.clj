(ns leihs.borrow.resources.entitlements
  (:require [clojure.tools.logging :as log]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [hugsql.core :as hugsql]
            [leihs.core.db :as db]
            [logbug.debug :as logbug]
            [taoensso.timbre :as timbre :refer [debug info spy]]))

(hugsql/def-sqlvec-fns "sql/entitlements.sql")

#_ (->> {:and-pool-ids-1 (and-pool-ids-1-snip
                          {:pool-ids ["8bd16d45-056d-5590-bc7f-12849f034351"]})
         :and-pool-ids-2 (and-pool-ids-2-snip
                          {:pool-ids ["8bd16d45-056d-5590-bc7f-12849f034351"]})
         :and-model-ids-1 (and-model-ids-1-snip
                           {:model-ids ["4fab1679-2347-514f-a0ff-116f955a484f"]})
         :and-model-ids-2 (and-model-ids-2-snip
                           {:model-ids ["4fab1679-2347-514f-a0ff-116f955a484f"]})
         :where-user-id (where-user-id-snip {:user-id "c0777d74-668b-5e01-abb5-f8277baa0ea8"})}
        all-entitlements-sqlvec
        (jdbc-query (db/get-ds)))

(defn get-for-model-and-pool [tx model-id pool-id]
  (->> {:and-pool-ids-1 (and-pool-ids-1-snip {:pool-ids [pool-id]})
        :and-pool-ids-2 (and-pool-ids-2-snip {:pool-ids [pool-id]})
        :and-model-ids-1 (and-model-ids-1-snip {:model-ids [model-id]})
        :and-model-ids-2 (and-model-ids-2-snip {:model-ids [model-id]})}
       all-entitlements-sqlvec
       (jdbc-query tx)))

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
         (jdbc-query tx)
         (map :quantity)
         (reduce +))))

#_ (total-quantity (db/get-ds)
                   "c0777d74-668b-5e01-abb5-f8277baa0ea8"
                   "4fab1679-2347-514f-a0ff-116f955a484f"
                   ["8bd16d45-056d-5590-bc7f-12849f034351"])

(def all-sql
  (first (all-entitlements-sqlvec)))

#_ (->> [all-sql] (jdbc-query (leihs.core.db/get-ds)))
