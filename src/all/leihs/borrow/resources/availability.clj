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

(defn validate-start-date [start-date]
  (if-not (spec/valid? ::start-date start-date)
    (-> ::start-date
        (spec/explain-str start-date)
        (ex-info {})
        throw)))

(defn validate-end-date [end-date]
  (if-not (spec/valid? ::end-date end-date)
    (-> ::end-date
        (spec/explain-str end-date)
        (ex-info {})
        throw)))

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
