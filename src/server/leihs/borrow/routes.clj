(ns leihs.borrow.routes
  (:refer-clojure :exclude [keyword replace])
  (:require
   [leihs.borrow.after-tx :as after-tx]
   [leihs.borrow.authenticate :as authenticate]
   [leihs.borrow.client.routes :as client-routes]
   [leihs.borrow.graphql :as graphql]
   [leihs.borrow.html :as html]
   [leihs.borrow.paths :refer [paths path]]
   [leihs.borrow.resources.attachments :as attachments]
   [leihs.borrow.resources.images :as images]
   [leihs.core.anti-csrf.back :as anti-csrf]
   [leihs.core.auth.session :as session]
   [leihs.core.db :as datasource]
   [leihs.core.graphql :as core-graphql]
   [leihs.core.http-cache-buster2 :as cache-buster :refer [wrap-resource]]
   [leihs.core.locale :as locale]
   [leihs.core.ring-audits :as ring-audits]
   [leihs.core.ring-exception :as ring-exception]
   [leihs.core.routes :as core-routes]
   [leihs.core.routing.back :as core-routing]
   [leihs.core.settings :as settings]
   [leihs.core.status :as status]
   [leihs.core.user.core :as user]
   [logbug.debug :as debug :refer [I>]]
   [logbug.ring :refer [wrap-handler-with-logging]]
   [ring-graphql-ui.core :refer [wrap-graphiql]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.params :refer [wrap-params]]
   ring.middleware.accept))

(def resolve-table
  (merge core-routes/resolve-table
         {:graphql graphql/handler,
          :home html/html-handler,
          ::client-routes/home html/html-handler,
          :image images/handler-one,
          :my-user user/routes,
          :attachment attachments/handler-one,
          :attachment-with-filename attachments/handler-one,
          :not-found html/not-found-handler}))

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
  (->
  ; (I> wrap-handler-with-logging
   dispatch-to-handler
   ring-audits/wrap
   anti-csrf/wrap
   locale/wrap
   authenticate/wrap
   session/wrap-authenticate
   wrap-cookies
   settings/wrap
   datasource/wrap-tx
   after-tx/wrap
   wrap-json-response
   (wrap-json-body {:keywords? true})
   wrap-empty
   core-graphql/wrap-with-schema
   (wrap-graphiql {:path "/borrow/graphiql",
                   :endpoint "/borrow/graphql"})
   core-routing/wrap-canonicalize-params-maps
   wrap-params
   wrap-multipart-params
   (status/wrap (path :status))
   wrap-content-type
   (wrap-resource "public"
                  {:allow-symlinks? true
                   :cache-bust-paths ["/borrow/ui/borrow-ui.css"
                                      "/borrow/js/main.js"]
                   :never-expire-paths [#".*fontawesome-[^\/]*\d+\.\d+\.\d+\/.*"
                                        #".+_[0-9a-f]{40}\..+"]
                   :enabled? true})
   (core-routing/wrap-resolve-handler html/html-handler)
   wrap-accept
   ring-exception/wrap))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
