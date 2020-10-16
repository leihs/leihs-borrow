(ns leihs.borrow.routes
  (:refer-clojure :exclude [str keyword replace])
  (:require
    [leihs.core.http-cache-buster2 :as cache-buster :refer [wrap-resource]]
    [bidi.bidi :as bidi]
    [cheshire.core :refer [parse-string]]
    [clojure.string :refer [starts-with? replace]]
    [clojure.tools.logging :as log]
    [leihs.borrow.authenticate :as authenticate]
    [leihs.borrow.client.routes :as client-routes]
    [leihs.borrow.graphql :as graphql]
    [leihs.borrow.html :as html]
    [leihs.borrow.resources.images :as images]
    [leihs.borrow.resources.attachments :as attachments]
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
    [clj-logging-config.log4j :as logging-config]
    [logbug.debug :as debug :refer [I>]]
    [logbug.ring :refer [wrap-handler-with-logging]]))

(def resolve-table
  (merge core-routes/resolve-table
         {:graphql graphql/handler,
          :home html/html-handler,
          ::client-routes/home html/html-handler,
          :image images/handler-one,
          :attachment attachments/handler-one,
          :attachment-with-filename attachments/handler-one,
          :not-found html/not-found-handler,
          :status (status/routes "/app/borrow/status")}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn dispatch-to-handler
  [request]
  (if-let [handler (:handler request)]
    (handler request)
    (throw
      (ex-info
        "There is no handler for this resource and the accepted content type."
        {:status 404, :uri (get request :uri)}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn wrap-empty [handler]
  (fn [request]
    (or (handler request)
        {:status 404})))

(defn init []
  (core-routing/init paths resolve-table)
  (-> ;wrap-handler-with-logging
      dispatch-to-handler
      anti-csrf/wrap
      ; locale/wrap
      authenticate/wrap
      session/wrap-authenticate
      wrap-cookies
      settings/wrap
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-empty
      datasource/wrap-tx
      datasource/wrap-after-tx
      (wrap-graphiql {:path "/app/borrow/graphiql",
                      :endpoint "/app/borrow/graphql"})
      core-routing/wrap-canonicalize-params-maps
      wrap-params
      wrap-multipart-params
      wrap-content-type
      (wrap-resource "public"
                     {:allow-symlinks? true
                      :cache-bust-paths ["/app/borrow/css/site.css"
                                         "/app/borrow/css/site.min.css"
                                         "/app/borrow/js/app.js"]
                      :never-expire-paths [#".*fontawesome-[^\/]*\d+\.\d+\.\d+\/.*"
                                           #".+_[0-9a-f]{40}\..+"]
                      :enabled? true})
      (core-routing/wrap-resolve-handler html/html-handler)
      wrap-accept
      ring-exception/wrap))

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
