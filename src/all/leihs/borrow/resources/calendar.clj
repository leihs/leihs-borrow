(ns leihs.borrow.resources.calendar
  (:refer-clojure :exclude [get])
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
            [clojure.walk :as walk]))

(defn get [context args _]
  (let [leihs-user-session-cookie (some-> context
                                          :request
                                          :cookies
                                          (clojure.core/get "leihs-user-session"))]
    (-> "http://localhost:3000/borrow/booking_calendar_availability"
        (client/get {:accept :json
                     :content-type :json
                     :cookies {"leihs-user-session" (or leihs-user-session-cookie
                                                        {:value "87a6e85e-61d2-4808-8e88-b9da6ea3d21c"})}
                     :query-params (select-keys args [:model_id
                                                      :inventory_pool_id
                                                      :start_date
                                                      :end_date])})
        :body
        json/parse-string
        walk/keywordize-keys)))

(def max-stream-duration (clj-time/minutes 5))

(defn stream [context args source-stream]
  (let [model-id (:model_id args)
        tx (-> context :request :tx)
        running (atom true)
        get-calendar #(get context args nil)
        get-updated-at #(reservations/updated-at tx model-id)
        time-out (clj-time/plus (clj-time-local/local-now) max-stream-duration)]
    (source-stream (get-calendar))
    (let [f (future
              (loop [updated-at (get-updated-at)]
                (when (clj-time/before? (clj-time-local/local-now) time-out) 
                  (Thread/sleep 1000)
                  (let [new-updated-at (get-updated-at)]
                    (if (clj-time/after? new-updated-at updated-at)
                      (do (source-stream (get-calendar))
                          (recur new-updated-at))
                      (recur updated-at))))))]
      #(future-cancel f))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
