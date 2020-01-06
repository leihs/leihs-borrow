(ns leihs.borrow.legacy
  (:require [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [camel-snake-kebab.core :as csk]
            [wharf.core :refer [transform-keys]]
            [clojure.walk :as walk]))

(def base-url (atom nil))

(defn init [options]
  (reset! base-url (-> options :legacy-http-base-url :url))
  (-> "Legacy base URL set to: "
      (str @base-url)
      log/info))

(defn session-cookie-value [context]
  (or (get-in context [:request :cookies "leihs-user-session" :value])
      (throw (ex-info "Not authenticated!" {}))))

(defn fetch [url context query-params]
  (-> (client/get
        (str @base-url url)
        {:accept :json
         :content-type :json
         :cookies {"leihs-user-session" {:value (session-cookie-value context)}}
         :multi-param-style :array
         :query-params query-params})
      :body
      json/parse-string
      walk/keywordize-keys))

(defn post [url context form-params]
  (client/post (str @base-url url)
               {:cookies {"leihs-user-session"
                          {:value (session-cookie-value context)}}
                :form-params form-params
                :content-type :json}))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)

