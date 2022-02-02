(ns leihs.borrow.legacy
  (:refer-clojure :exclude [str keyword])
  (:require
    [cheshire.core :as json]
    [clj-http.client :as client]
    [clj-yaml.core :as yaml]
    [clojure.tools.logging :as log]
    [clojure.walk :as walk]
    [cuerdas.core :as string :refer [snake kebab upper human]]
    [environ.core :refer [env]]
    [leihs.core.core :refer [keyword str presence]]
    [taoensso.timbre :refer [debug info warn error spy]]
    [wharf.core :refer [transform-keys]]
    ))


(def base-url* (atom nil))

(defn base-url  []
  (or @base-url*
      (throw (ex-info "legacy not properly initialized!" {}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn long-opt-for-key [k]
  (str "--" (kebab k) " " (-> k snake upper)))

(def legacy-http-host-key :legacy-http-host)
(def legacy-http-port-key :legacy-http-port)
(def legacy-http-protocol-key :legacy-http-protocol)

(def legacy-http-keys [legacy-http-host-key legacy-http-port-key legacy-http-protocol-key])

(def cli-opts
  [[nil (long-opt-for-key legacy-http-protocol-key)
    :default (or (some-> legacy-http-protocol-key env presence)
                 "http")]
   [nil (long-opt-for-key legacy-http-host-key)
    :default (or (some-> legacy-http-host-key env presence)
                 "localhost")]
   [nil (long-opt-for-key legacy-http-port-key)
    :default (or (some-> legacy-http-port-key env presence yaml/parse-string)
                 3210)
    :parse-fn yaml/parse-string]])

(defn init [options]
  (let [opts (select-keys options legacy-http-keys)]
    (info "initialize legacy " opts)
    (reset! base-url*
            (str (get opts legacy-http-protocol-key) "://"
                 (get opts legacy-http-host-key)
                 ":" (get opts legacy-http-port-key)))
    (info " initialized legacy " @base-url*)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn session-cookie-value [context]
  (or (get-in context [:request :cookies "leihs-user-session" :value])
      (throw (ex-info "Not authenticated!" {}))))

(defn fetch [path context query-params]
  (let [url (str (base-url) path)]
    (-> {:method :get
         :url url
         :accept :json
         :content-type :json
         :cookies {"leihs-user-session" {:value (session-cookie-value context)}}
         :multi-param-style :array
         :query-params query-params}
        spy
        client/request
        :body
        json/parse-string
        walk/keywordize-keys)))

(defn post [path context form-params]
  (let [url (str (base-url) path)][url (str (base-url) path)]
    (-> {:method :post
         :url url
         :cookies {"leihs-user-session"
                   {:value (session-cookie-value context)}}
         :form-params form-params
         :content-type :json}
        spy
        client/request)))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)

