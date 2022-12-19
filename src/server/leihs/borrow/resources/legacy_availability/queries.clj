(ns leihs.borrow.resources.legacy-availability.queries
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.db :as db]
            [leihs.core.settings :refer [settings!]]
            [leihs.core.sql :as sql]))

(defn running-reservations
  "Used by the availability calculation."
  [tx model-id pool-id exclude-res-ids]
  (let [timeout-minutes (-> (settings! tx [:timeout_minutes])
                            :timeout_minutes)]
    (-> (sql/select :reservations.id,
                    :reservations.inventory_pool_id,
                    :reservations.model_id,
                    :reservations.item_id,
                    :reservations.quantity,
                    :reservations.start_date,
                    :reservations.end_date,
                    :reservations.returned_date,
                    :reservations.status,
                    [(sql/call :array (-> (sql/select :egu.entitlement_group_id)
                                          (sql/from [:entitlement_groups_users :egu])
                                          (sql/merge-join [:entitlement_groups :eg]
                                                          [:= :eg.id :egu.entitlement_group_id])
                                          (sql/merge-where [:= :egu.user_id :reservations.user_id])
                                          (sql/order-by [:eg.name :asc])))
                     :user_group_ids])
        (sql/from :reservations)
        (sql/merge-left-join :items [:= :reservations.item_id :items.id])
        (sql/merge-where [:or [:is-null :reservations.item_id] [:= :items.is_borrowable true]])
        (sql/merge-where [:not-in :reservations.status ["draft" "rejected" "canceled" "closed"]])
        (sql/merge-where [:not [:and
                                [:= :reservations.status "unsubmitted"]
                                [:< :reservations.updated_at
                                 (sql/raw (format "now() at time zone 'UTC' - interval '%d minutes'"
                                                  timeout-minutes))]]])
        (sql/merge-where [:not [:and
                                [:< :reservations.end_date (sql/raw "(now() at time zone 'UTC')::date")]
                                [:is-null :reservations.item_id]]])
        (cond-> pool-id (sql/merge-where [:= :reservations.inventory_pool_id pool-id]))
        (sql/merge-where [:= :reservations.type "ItemLine"])
        (sql/merge-where [:= :reservations.model_id model-id])
        (cond-> (not (empty? exclude-res-ids))
          (sql/merge-where [:not-in :reservations.id exclude-res-ids]))
        sql/format
        (->> (jdbc/query tx)))))

(comment
 (settings! (db/get-ds))
 (let [pool-id "8bd16d45-056d-5590-bc7f-12849f034351"
       model-id "804a50c1-2329-5d5b-9884-340f43833514"]
   (-> (running-reservations (db/get-ds) model-id pool-id)
       ; first
       ; :user_group_ids
       )))
