(ns leihs.borrow.resources.calendar
  (:refer-clojure :exclude [get])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [leihs.core.sql :as sql]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.walk :as walk]))

(defn get
  [context args _]
  (let [leihs-user-session-cookie (some-> context
                                          :request
                                          :cookies
                                          (clojure.core/get "leihs-user-session"))]
    (-> "http://localhost:3000/borrow/booking_calendar_availability"
        (client/get {:accept :json
                     :content-type :json
                     :cookies {"leihs-user-session" leihs-user-session-cookie}
                     :query-params (select-keys args [:model_id
                                                      :inventory_pool_id
                                                      :start_date
                                                      :end_date])})
        :body
        json/parse-string
        walk/keywordize-keys)))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
