(ns leihs.borrow.resources.calendar
  (:refer-clojure :exclude [get count])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
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

(defn get [context args value]
  (let [leihs-user-session-cookie-value
          (get-in context [:request :cookies "leihs-user-session" :value])]
    (if-not leihs-user-session-cookie-value (throw (ex-info "Not authenticated!" {})))
    (let [http-availability
            (client/get
              "http://localhost:3000/borrow/booking_calendar_availability"
              {:accept :json
               :content-type :json
               :cookies {"leihs-user-session" {:value leihs-user-session-cookie-value}}
               :query-params (->> [:modelId
                                   :inventoryPoolId
                                   :startDate
                                   :endDate]
                                  (select-keys args)
                                  (transform-keys csk/->snake_case))})]
      (->> http-availability
           :body
           json/parse-string
           walk/keywordize-keys))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
