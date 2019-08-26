(ns leihs.borrow.resources.calendar
  (:refer-clojure :exclude [get])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [leihs.core.sql :as sql]
            [clj-time.core :as clj-time]
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
                                                        {:value "7f109a0b-ebb3-44be-addc-f42595d12077"})}
                     :query-params (select-keys args [:model_id
                                                      :inventory_pool_id
                                                      :start_date
                                                      :end_date])})
        :body
        json/parse-string
        walk/keywordize-keys)))

(defn stream [context args source-stream]
  (log/debug "STREAM")
  (let [model-id (:model_id args)
        tx (-> context :request :tx)
        ; channel (async/chan)
        t (Thread. #(while true
                      (log/debug "thread")
                      (source-stream (log/spy (get context args nil)))
                      (Thread/sleep 1000)))]
    (.start t)
    #(.interrupt t)
    ; (async/go
    ;   (loop [updated-at (reservations/updated-at tx model-id)]
    ;     (log/debug updated-at)
    ;     (Thread/sleep 1000)
    ;     (let [new-updated-at (reservations/updated-at tx model-id)]
    ;       (if (clj-time/after? new-updated-at updated-at)
    ;         (do (async/>! channel (get context args nil))
    ;             (source-stream (async/<! channel))
    ;             (recur new-updated-at))
    ;         (do (source-stream (async/<! channel))
    ;             (recur updated-at))))))
    ; #(async/close! channel)
    ))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
