(ns leihs.borrow.graphql.streamers
  (:require [leihs.borrow.resources.calendar :as calendar]))

(def streamers
  {:stream-calendar calendar/stream})
