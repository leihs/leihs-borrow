(ns leihs.borrow.resources.availability
  (:refer-clojure :exclude [get])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [clojure.spec.alpha :as spec]
            [leihs.core.sql :as sql]
            [clj-time.core :as clj-time]
            [clj-time.local :as clj-time-local]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [leihs.borrow.resources.reservations :as reservations]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]
            [clojure.walk :as walk]))

(spec/def ::startDate string?)
(spec/def ::endDate string?)
(spec/def ::inventoryPoolIds (spec/coll-of uuid? :min-count 1))

(def FETCH-URL "http://localhost:3000/borrow/booking_calendar_availability")

(defn fetch-from-legacy [args leihs-user-session-cookie-value]
  (client/get
    FETCH-URL
    {:accept :json
     :content-type :json
     :cookies {"leihs-user-session" {:value leihs-user-session-cookie-value}}
     :query-params (->> [:modelId
                         :inventoryPoolId
                         :startDate
                         :endDate]
                        (select-keys args)
                        (transform-keys csk/->snake_case))}))

(defn session-cookie-value [context]
  (or (get-in context [:request :cookies "leihs-user-session" :value])
      (throw (ex-info "Not authenticated!" {}))))

(defn get [context args _]
  (->> context
       session-cookie-value
       (fetch-from-legacy args)
       :body
       json/parse-string
       walk/keywordize-keys
       :list
       (map #(clojure.set/rename-keys % {:d :date}))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
