(ns leihs.borrow.resources.legacy-availability.queries
  (:require [taoensso.timbre :as timbre :refer [debug info spy]]
            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
            [leihs.core.db :as db]
            [leihs.core.settings :refer [settings!]]))

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
                    [[:'ARRAY (-> (sql/select :egu.entitlement_group_id)
                                  (sql/from [:entitlement_groups_users :egu])
                                  (sql/join [:entitlement_groups :eg]
                                            [:= :eg.id :egu.entitlement_group_id])
                                  (sql/where [:= :egu.user_id :reservations.user_id])
                                  (sql/order-by [:eg.name :asc]))]
                     :user_group_ids])
        (sql/from :reservations)
        (sql/left-join :items [:= :reservations.item_id :items.id])
        (sql/where [:or [:is-null :reservations.item_id] [:= :items.is_borrowable true]])
        (sql/where [:not-in :reservations.status ["draft" "rejected" "canceled" "closed"]])
        (sql/where [:not [:and
                          [:= :reservations.status "unsubmitted"]
                          [:< :reservations.updated_at
                           [:raw (format "now() at time zone 'UTC' - interval '%d minutes'"
                                         timeout-minutes)]]]])
        (sql/where [:not [:and
                          [:< :reservations.end_date [:raw "(now() at time zone 'UTC')::date"]]
                          [:is-null :reservations.item_id]]])
        (cond-> pool-id (sql/where [:= :reservations.inventory_pool_id pool-id]))
        (sql/where [:= :reservations.type "ItemLine"])
        (sql/where [:= :reservations.model_id model-id])
        (cond-> (not (empty? exclude-res-ids))
          (sql/where [:not-in :reservations.id exclude-res-ids]))
        sql-format
        (->> (jdbc-query tx)))))

(comment
 (settings! (db/get-ds))
 (let [pool-id "8bd16d45-056d-5590-bc7f-12849f034351"
       model-id "804a50c1-2329-5d5b-9884-340f43833514"]
   (-> (running-reservations (jdbc/with-options (db/get-ds-next) db/builder-fn-options)
                             model-id pool-id nil)
       ; first
       ; :user_group_ids
       )))
