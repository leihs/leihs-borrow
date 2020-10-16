(ns leihs.borrow.csrf
  (:require [reagent.cookies :as cookies]
            [leihs.core.constants :as constants]))

(def token
  (cookies/get-raw constants/ANTI_CSRF_TOKEN_COOKIE_NAME))

(defn token-field []
  [:input {:type "hidden"
           :name constants/ANTI_CSRF_TOKEN_FORM_PARAM_NAME
           :value token}])
