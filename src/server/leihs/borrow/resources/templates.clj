(ns leihs.borrow.resources.templates
  (:refer-clojure :exclude [apply])
  (:require [camel-snake-kebab.core :as csk]

            [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [next.jdbc.sql :refer [query] :rename {query jdbc-query}]

            [leihs.borrow.db :refer [query]]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.inventory-pools :as pools]
            [leihs.borrow.resources.models :as models]
            [leihs.borrow.resources.reservations :as reservations]
            [wharf.core :refer [transform-keys]]))

(def base-sqlmap
  (-> (sql/select :model_groups.id
                  :model_groups.name
                  :inventory_pools_model_groups.inventory_pool_id)
      (sql/from :model_groups)
      (sql/join :inventory_pools_model_groups
                [:=
                 :inventory_pools_model_groups.model_group_id
                 :model_groups.id])
      (sql/where [:= :model_groups.type "Template"])
      (sql/order-by [:model_groups.name :asc])))

(defn get-one-by-id [tx id user-id]
  (if-let
   [template
    (-> base-sqlmap
        (sql/join :inventory_pools
                  [:=
                   :inventory_pools.id
                   :inventory_pools_model_groups.inventory_pool_id])
        (sql/where [:= :model_groups.id id])
        (pools/accessible-to-user-condition user-id)
        sql-format
        (query tx)
        first)]
    template
    (throw (ex-info "Resource not found or not accessible for profile user id" {:status 403}))))

(defn get-one [{{tx :tx} :request user-id ::target-user/id} {:keys [id]} _]
  (get-one-by-id tx id user-id))

(defn get-multiple [{{tx :tx} :request user-id ::target-user/id} _ _]
  (-> base-sqlmap
      (sql/join :inventory_pools
                [:=
                 :inventory_pools.id
                 :inventory_pools_model_groups.inventory_pool_id])
      (pools/accessible-to-user-condition user-id)
      sql-format
      (->> (jdbc-query tx))))

(defn lines [tx tmpl-id]
  (-> (sql/select :model_links.id
                  :model_links.model_id
                  :model_links.quantity)
      (sql/from :model_links)
      (sql/where [:= :model_links.model_group_id tmpl-id])
      sql-format
      (query tx)))

(defn get-lines [{{tx :tx} :request} _ {template-id :id}]
  (lines tx template-id))

(defn apply
  [{{tx :tx} :request user-id ::target-user/id :as context}
   {template-id :id start-date :start-date end-date :end-date
    :as args}
   _]
  (let [tmpl (get-one-by-id tx template-id user-id)
        tmpl-lines (lines tx template-id)]
    (->> tmpl-lines
         (map (fn [line]
                (when (and
                       (> (:quantity line) 0)
                       (models/reservable? context args {:id (:model-id line)}))
                  (as-> line <>
                    (transform-keys csk/->kebab-case <>)
                    (select-keys <> [:model-id :quantity])
                    (merge <> {:start-date start-date
                               :end-date end-date
                               :inventory-pool-id (:inventory-pool-id tmpl)})
                    (reservations/create-optimistic context <> nil)))))
         (remove nil?)
         flatten)))
