(ns leihs.borrow.paths
  (:require [bidi.bidi :refer [path-for]]
            [bidi.verbose :refer [branch leaf param]]
            [leihs.borrow.client.routes :as client-routes]
            leihs.core.paths))

(def paths
  (branch
   ""
   leihs.core.paths/core-paths
   (branch
    "/borrow"
    (leaf "/shutdown" :shutdown)
    (leaf "/graphql" :graphql)
    (leaf "/status" :status)
      ; NOTE: don't rename the handler-key for image as it may break the
      ; workaround for the problem with hanging requests
    (branch "/attachments/" (param :attachment-id)
            (leaf "" :attachment))
    (branch "/images/" (param :image-id)
            (leaf "" :image))
    ["/" client-routes/client-routes])
   (leaf true :not-found)))

(reset! leihs.core.paths/paths* paths)

(def path leihs.core.paths/path)
