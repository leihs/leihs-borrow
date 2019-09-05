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
            [clojure.walk :as walk]))

(defn get [context args value]
  (let [leihs-user-session-cookie-value
          (or (get-in context [:request :cookies "leihs-user-session" :value]) ; http
              (get-in context [:connection-params :cookies :leihs-user-session]) ; websocket
              )]
    (if-not leihs-user-session-cookie-value (throw (ex-info "Not authenticated!" {})))
    (-> "http://localhost:3000/borrow/booking_calendar_availability"
        (client/get {:accept :json
                     :content-type :json
                     :cookies {"leihs-user-session" {:value leihs-user-session-cookie-value}}
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
        get-count #(reservations/count tx model-id)
        time-out (clj-time/plus (clj-time-local/local-now) max-stream-duration)]
    (source-stream (get-calendar))
    (let [f (future
              (loop [updated-at (get-updated-at)
                     count (get-count)]
                (when (clj-time/before? (clj-time-local/local-now) time-out) 
                  (Thread/sleep 1000)
                  (let [new-updated-at (get-updated-at)
                        new-count (get-count)]
                    (if (or (clj-time/after? new-updated-at updated-at)
                            (not (= new-count count)))
                      (do (source-stream (get-calendar))
                          (recur new-updated-at new-count))
                      (recur updated-at count))))))]
      #(future-cancel f))))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
