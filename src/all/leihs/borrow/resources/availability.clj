(ns leihs.borrow.resources.availability
  (:refer-clojure :exclude [get])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [clojure.spec.alpha :as spec]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [clj-time.core :as clj-time]
            [clj-time.local :as clj-time-local]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]
            [clojure.walk :as walk]
            [ring.middleware.nested-params :refer [nested-params-request]]))

(spec/def ::start-date string?)
(spec/def ::end-date string?)
(spec/def ::inventory-pool-ids (spec/coll-of uuid? :min-count 1))

(def legacy-base-url (atom nil))

(defn init [options]
  (reset! legacy-base-url (-> options :legacy-http-base-url :url))
  (-> "Legacy base URL set to: "
      (str @legacy-base-url)
      log/info))

(defn fetch-from-legacy [url cookie-value query-params]
  (-> (client/get
        url
        {:accept :json
         :content-type :json
         :cookies {"leihs-user-session" {:value cookie-value}}
         :multi-param-style :array
         :query-params query-params})
      :body
      json/parse-string
      walk/keywordize-keys))

(defn session-cookie-value [context]
  (or (get-in context [:request :cookies "leihs-user-session" :value])
      (throw (ex-info "Not authenticated!" {}))))

(defn get-available-quantities [context args _value]
  (fetch-from-legacy
    (str @legacy-base-url "/borrow/models/availability")
    (session-cookie-value context)
    (transform-keys csk/->snake_case args)))

(comment
  (def result
    (let [context {:request {:cookies
                             {"leihs-user-session"
                              {:value "df306cab-9277-429e-8241-dfd04148429b"}}}}
          args {:model-ids ["976a9fc7-a192-584f-8075-e90794e864be"
                            "0fdfc760-ba04-52e3-9fee-4992f520abd7"]
                :inventory-pool-ids ["8bd16d45-056d-5590-bc7f-12849f034351"
                                     "5dd25b58-fa56-5095-bd97-2696d92c2fb1"]
                :start-date "2019-11-11"
                :end-date "2019-11-12"}]
      (get-available-quantities context args nil)))
  (->> result
       (group-by :model_id)
       (map (fn [[model-id pool-quantities]]
              [model-id (as-> pool-quantities <>
                          (map :quantity <>)
                          (apply + <>)
                          (cond-> <> (< <> 0) 0))]))
       (into {}))
  {"0fdfc760-ba04-52e3-9fee-4992f520abd7"
   [{:model_id "0fdfc760-ba04-52e3-9fee-4992f520abd7", :inventory_pool_id "5dd25b58-fa56-5095-bd97-2696d92c2fb1", :quantity 0}
    {:model_id "0fdfc760-ba04-52e3-9fee-4992f520abd7", :inventory_pool_id "8bd16d45-056d-5590-bc7f-12849f034351", :quantity 0}],
   "976a9fc7-a192-584f-8075-e90794e864be"
   [{:model_id "976a9fc7-a192-584f-8075-e90794e864be", :inventory_pool_id "5dd25b58-fa56-5095-bd97-2696d92c2fb1", :quantity 0}
    {:model_id "976a9fc7-a192-584f-8075-e90794e864be", :inventory_pool_id "8bd16d45-056d-5590-bc7f-12849f034351", :quantity 1}]}
  )

(defn get [context args _value]
  (->> (fetch-from-legacy
         (str @legacy-base-url "/borrow/booking_calendar_availability")
         (session-cookie-value context)
         (->> [:model-id :inventory-pool-id :start-date :end-date]
              (select-keys args)
              (transform-keys csk/->snake_case)))
       :list
       (map #(clojure.set/rename-keys % {:d :date}))
       (hash-map :dates)))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
