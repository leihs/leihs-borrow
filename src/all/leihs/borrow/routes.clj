(ns leihs.borrow.routes
  (:refer-clojure :exclude [str keyword replace])
  (:require
    [leihs.core.http-cache-buster2 :as cache-buster :refer [wrap-resource]]
    [bidi.bidi :as bidi]
    [cheshire.core :refer [parse-string]]
    [clojure.string :refer [starts-with? replace]]
    [clojure.tools.logging :as log]
    [leihs.borrow.authenticate :refer [wrap-ensure-authenticated-entity]]
    [leihs.borrow.graphql :as graphql]
    [leihs.borrow.html :as html]
    [leihs.borrow.paths :refer [path paths]]
    [leihs.core.anti-csrf.back :as anti-csrf]
    [leihs.core.auth.session :as session]
    [leihs.core.core :refer [presence]]
    [leihs.core.ds :as datasource]
    [leihs.core.locale :as locale]
    [leihs.core.ring-exception :as ring-exception]
    [leihs.core.routes :as core-routes]
    [leihs.core.routing.back :as core-routing]
    [leihs.core.settings :as settings]
    [leihs.core.sign-out.back :as sign-out]
    [leihs.core.status :as status]
    [ring-graphql-ui.core :refer [wrap-graphiql]]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.accept]
    [ring.middleware.cookies :refer [wrap-cookies]]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.util.response :refer [redirect status]]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug :refer [I>]]
    [logbug.ring :refer [wrap-handler-with-logging]]
    ))

(def handler-resolve-table
  (merge core-routes/resolve-table
         {:graphql graphql/handler,
          :home html/html-handler
          :not-found html/not-found-handler,
          :status (status/routes "/borrow/status")}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn redirect-to-root-handler
  [request]
  (redirect (path :root)))

(defn handler-resolver
  [handler-key]
  (get handler-resolve-table handler-key nil))

(defn dispatch-to-handler
  [request]
  (if-let [handler (:handler request)]
    (handler request)
    (throw
      (ex-info
        "There is no handler for this resource and the accepted content type."
        {:status 404, :uri (get request :uri)}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- match-pair-with-fallback
  [path]
  (let [matched-pair (bidi/match-pair paths {:remainder path, :route paths})]
    (if (-> matched-pair
            :handler
            (= :not-found))
      (bidi/match-pair paths {:remainder path, :route paths})
      matched-pair)))

(defn wrap-resolve-handler
  ([handler] (fn [request] (wrap-resolve-handler handler request)))
  ([handler request]
   (let [path (or (-> request
                      :path-info
                      presence)
                  (-> request
                      :uri
                      presence))
         {route-params :route-params, handler-key :handler}
           (match-pair-with-fallback path)
         handler-fn (handler-resolver handler-key)]
     (handler (assoc request
                     :route-params route-params
                     :handler-key handler-key
                     :handler handler-fn)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wrap-empty
  [handler]
  (fn [request] (or (handler request) {:status 404})))

(defn wrap-accept
  [handler]
  (ring.middleware.accept/wrap-accept
    handler
    {:mime ["application/json" :qs 1 :as :json
            "application/javascript" :qs 1 :as :javascript
            "image/apng" :qs 1 :as :apng
            "image/*" :qs 1 :as :image
            "text/css" :qs 1 :as :css
            "text/html" :qs 1 :as :html]}))

(defn init
  []
  (-> ;wrap-handler-with-logging
      dispatch-to-handler
      ; anti-csrf/wrap
      locale/wrap
      ; wrap-authorize
      ; wrap-ensure-authenticated-entity
      session/wrap-authenticate
      wrap-cookies
      settings/wrap
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-empty
      datasource/wrap-tx
      (wrap-graphiql {:path "/borrow/graphiql",
                      :endpoint "/borrow/graphql"})
      core-routing/wrap-canonicalize-params-maps
      wrap-params
      wrap-multipart-params
      wrap-content-type
      (wrap-resource "public"
                     {:allow-symlinks? true
                      :cache-bust-paths ["/borrow/css/site.css"
                                         "/borrow/css/site.min.css"
                                         "/borrow/js/app.js"]
                      :never-expire-paths [#".*fontawesome-[^\/]*\d+\.\d+\.\d+\/.*"
                                           #".+_[0-9a-f]{40}\..+"]
                      :enabled? true})
      wrap-resolve-handler
      wrap-accept
      ring-exception/wrap))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
