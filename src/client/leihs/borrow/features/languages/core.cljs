(ns leihs.borrow.features.languages.core
  (:require 
   [ajax.core :refer [GET POST json-response-format]]
   [clojure.string :as string]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.core.constants :as constants]
   [leihs.borrow.csrf :as csrf]
   [leihs.borrow.lib.helpers :refer [log spy body-encode]]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch
                                      dispatch-sync]]
   [leihs.borrow.lib.translate :as translate]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]))

(reg-sub ::data (fn [db _] (::data db)))

(reg-event-db ::on-failure
              (fn-traced [db _] (log "failure") db))

(reg-event-fx ::switch
              (fn-traced [_ [_ locale-id]]
                (let [data {:locale locale-id,
                            constants/ANTI_CSRF_TOKEN_FORM_PARAM_NAME csrf/token}]
                  {:http-xhrio {:method :post
                                :uri (str js/window.location.origin "/my/user/me")
                                :body (body-encode data)
                                :response-format (json-response-format {:keywords? true})
                                :on-success [::translate/set-locale-to-use]
                                :on-failure [::on-failure]}})))
