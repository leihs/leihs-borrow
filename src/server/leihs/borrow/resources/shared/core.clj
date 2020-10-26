(ns leihs.borrow.resources.shared.core
  (:require [camel-snake-kebab.core :as csk]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]
            [inflections.core :refer [plural]]
            [com.walmartlabs.lacinia :as lacinia]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.borrow.client.routes :as client-routes]
            [leihs.borrow.paths :refer [path]]))

(defn url
  "This function assumes the following convention in the paths map:
  \"orders\" {[\"/\" :order-id] ::orders-show}"
  [context _ {:keys [id]}]
  (let [type-name (-> context
                      ::lacinia/container-type-name
                      name)
        path-name (-> type-name
                      plural
                      csk/->kebab-case
                      (str "-show")
                      (->> (keyword "leihs.borrow.client.routes")))
        path-param (-> type-name
                       string/lower-case
                       (str "-id")
                       keyword)]
    (str (-> context :request :headers :origin)
         (path path-name {path-param id}))))
