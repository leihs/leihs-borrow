(ns leihs.borrow.resources.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as clj-str]
            [clojure.tools.logging :as log]
            [leihs.borrow.graphql.target-user :as target-user]
            [leihs.borrow.resources.helpers :as helpers]
            [leihs.borrow.resources.languages :as languages]
            [leihs.borrow.resources.users.shared :refer [get-by-id base-sqlmap]]
            [leihs.core.paths :refer [path]]
            [leihs.core.settings :refer [settings!]]
            [leihs.core.sql :as sql]
            [leihs.core.user.queries :refer [merge-search-term-where-clause]]
            [leihs.core.remote-navbar.shared :refer [sub-apps]]))

(defn get-one
  [{{:keys [tx]} :request target-user-id ::target-user/id} _ {:keys [user-id]}]
  (get-by-id tx (or user-id target-user-id)))

(defn get-current
  [{{tx :tx {session-id :user_session_id} :authenticated-entity} :request
    user-id ::target-user/id}
   _
   _]
  {:id user-id
   :user (get-by-id tx user-id)
   :session-id session-id})

(defn get-navigation [{{:keys [tx authenticated-entity]} :request} _ {user-id :id}]
  (let [settings (settings! tx [:external_base_url :documentation_link])
        base-url (:external_base_url settings)
        sub-apps (sub-apps tx authenticated-entity)]
    {:admin-url (when (:admin sub-apps) (str base-url "/admin/"))
     :procure-url (when (:procure sub-apps) (str base-url "/procure/"))
     :manage-nav-items (map #(assoc % :url (:href %)) (:manage sub-apps))
     :documentation-url (:documentation_link settings)}))

(defn get-settings [{{:keys [tx]} :request} _ {user-id :id}]
  (let [settings (settings! tx [:lending_terms_acceptance_required_for_order
                                :lending_terms_url
                                :show_contact_details_on_customer_order])]
    {:lending-terms-acceptance-required-for-order (:lending_terms_acceptance_required_for_order settings)
     :lending-terms-url (:lending_terms_url settings)
     :show-contact-details-on-customer-order (:show_contact_details_on_customer_order settings)}))

;#### debug ###################################################################
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
