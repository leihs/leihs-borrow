(ns leihs.borrow.resources.availability
  (:refer-clojure :exclude [get])
  (:require [clojure.tools.logging :as log]
            [clojure.spec.alpha :as spec]
            [leihs.borrow.legacy :as legacy]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]))

(defn get-available-quantities [context args _value]
  (legacy/fetch
    "/borrow/models/availability"
    context
    (transform-keys csk/->snake_case args)))

(spec/def ::date string?)
(spec/def ::quantity (comp #(>= % 0) integer?))
(spec/def ::visits_count (comp #(>= % 0) integer?))

(spec/def ::calendar-date
  (spec/keys :req-un #{::date ::quantity ::visits_count}))

(defn get [context args _value]
  (->> (legacy/fetch
         "/borrow/booking_calendar_availability"
         context
         (->> [:model-id :inventory-pool-id :start-date :end-date :user-id]
              (select-keys args)
              (transform-keys csk/->snake_case)))
       :list
       (map (fn [d]
              (->> (clojure.set/rename-keys d {:d :date})
                   (spec/assert ::calendar-date))))
       (hash-map :dates)))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
