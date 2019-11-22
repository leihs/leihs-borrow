(ns leihs.borrow.resources.visits
  (:refer-clojure :exclude [count])
  (:require [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.borrow.resources.helpers :as helpers]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(def base-sqlmap 
  (-> (sql/select :*)
      (sql/from :visits)))

(defn get-multiple [{{:keys [tx authenticated-entity]} :request}
                    {:keys [limit order-by]}
                    _]
  (-> base-sqlmap
      (sql/merge-where [:= :user_id (:id authenticated-entity)])
      (cond-> (seq order-by)
        (sql/order-by (helpers/treat-order-arg order-by)))
      (cond-> limit (-> (sql/limit limit)))
      sql/format
      (as-> <>
        (jdbc/query tx
                    <>
                    {:row-fn
                     #(assoc % :visit-type (case (:type %)
                                             "hand_over" "PICKUP"
                                             "take_back" "RETURN"))}))))

(comment
  (-> base-sqlmap
      sql/format
      (->> (jdbc/query (ds/get-ds)))
      first))
