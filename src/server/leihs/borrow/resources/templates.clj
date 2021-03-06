(ns leihs.borrow.resources.templates
  (:refer-clojure :exclude [apply])
  (:require [camel-snake-kebab.core :as csk]
            [clojure.spec.alpha :as spec]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]
            [leihs.core.core :refer [spy-with]]
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

(defn get-one-by-id [tx id]
  (-> base-sqlmap
      (sql/merge-where [:= :model_groups.id id])
      sql/format
      (query tx)
      first))

(defn get-one [{{:keys [tx]} :request} {:keys [id]} _]
  (get-one-by-id tx id))

(defn get-multiple [{{tx :tx} :request user-id ::target-user/id} _ _]
  (-> base-sqlmap
      (sql/merge-join :inventory_pools_model_groups
                      [:=
                       :inventory_pools_model_groups.model_group_id
                       :model_groups.id])
      (sql/merge-join :inventory_pools
                      [:=
                       :inventory_pools.id
                       :inventory_pools_model_groups.inventory_pool_id])
      (pools/accessible-to-user-condition user-id)
      sql/format
      (->> (jdbc/query tx))
      (->> (spy-with count))))

(defn lines [tx tmpl-id]
  (-> (sql/select :model_links.id
                  :model_links.model_id
                  :model_links.quantity)
      (sql/from :model_links)
      (sql/merge-where [:= :model_links.model_group_id tmpl-id])
      sql/format
      (query tx)))

(defn get-lines [{{:keys [tx]} :request} _ {template-id :id}]
  (lines tx template-id))

(defn apply
  [{{:keys [tx]} :request :as context}
   {template-id :id start-date :start-date end-date :end-date
    :as args}
   _]
  (let [tmpl (get-one-by-id tx template-id)
        tmpl-lines (lines tx template-id)]
    (->> tmpl-lines
         (map (fn [line]
                (when (models/reservable? context args {:id (:model-id line)})
                  (as-> line <>
                    (transform-keys csk/->kebab-case <>)
                    (select-keys <> [:model-id :quantity])
                    (merge <> {:start-date start-date
                               :end-date end-date
                               :inventory-pool-id (:inventory-pool-id tmpl)})
                    (reservations/create-draft context <> nil)))))
         (remove nil?)
         flatten)))

(comment
  (-> base-sqlmap
      sql/format
      (->> (jdbc/query scratch/tx))
      first))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
