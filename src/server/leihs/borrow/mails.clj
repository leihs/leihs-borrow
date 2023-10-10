(ns leihs.borrow.mails
  (:require
   [next.jdbc :as jdbc :refer [execute!] :rename {execute! jdbc-execute!}]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]

   [leihs.core.db :as db]
   [leihs.core.core :refer [presence flip]]
   [leihs.core.settings :refer [settings!]]
   [wet.core :as wet]
   [leihs.borrow.resources.delegations :refer [delegation?]]
   [leihs.borrow.resources.languages :as lang]
   [leihs.borrow.resources.inventory-pools :as pools]
   [leihs.borrow.resources.orders.shared :as orders]
   [leihs.borrow.resources.users.shared :as users]
   [leihs.borrow.translate :refer [t]]
   [taoensso.timbre :refer [debug info warn error spy]]))

(defn get-tmpl [tx name pool-id lang-locale]
  (-> (sql/select :body)
      (sql/from :mail_templates)
      (sql/where [:= :name name])
      (sql/where [:= :inventory_pool_id pool-id])
      (sql/where [:= :language_locale lang-locale])
      sql-format
      (->> (jdbc-query tx))
      first
      :body))

(defn send-received [{{:keys [settings] tx :tx-next} :request} order]
  (when (:deliver_received_order_notifications settings)
    (let [inventory-pool (pools/get-by-id tx (:inventory_pool_id order))
          lang-locale (:locale (lang/default tx))
          tmpl (get-tmpl tx "received" (:id inventory-pool) lang-locale)]
      (cond
       (not inventory-pool) (warn "Pool for sending email not found or it is inactive.")
       (not tmpl) (warn (format "No 'received' mail template found for pool '%s'." (:id inventory-pool)))
       :else (let [email-signature (:email_signature settings)
                   user (users/get-by-id tx (:user_id order))
                   reservations (-> (sql/select :r.quantity, :r.start_date, :r.end_date
                                                [[:concat_ws " " :m.product :m.version] :model_name])
                                    (sql/from [:reservations :r])
                                    (sql/join [:models :m] [:= :r.model_id :m.id])
                                    (sql/where [:= :r.order_id (:id order)])
                                    sql-format
                                    (->> (jdbc-query tx)))
                   purpose (:purpose order)
                   order-url (str (:external_base_url settings) "/manage/" (:id inventory-pool) "/orders/" (:id order) "/edit")
                   email-body (wet/render (wet/parse tmpl)
                                          {:params {:user user
                                                    :inventory_pool inventory-pool
                                                    :email_signature email-signature
                                                    :reservations reservations
                                                    :comment nil
                                                    :purpose purpose
                                                    :order_url order-url}
                                           :filters {}})
                   address (or (:email inventory-pool) (:smtp_default_from_address settings))]
               (debug email-body)
               (-> (sql/insert-into :emails)
                   (sql/values [{:inventory_pool_id (:id inventory-pool)
                                 :from_address address
                                 :to_address address
                                 :subject (t :borrow.mail-templates.received.subject lang-locale)
                                 :body email-body}])
                   sql-format
                   (->> (jdbc-execute! tx))))))))

(defn send-submitted [{{:keys [settings] tx :tx-next} :request} order]
  (let [inventory-pool (pools/get-by-id tx (:inventory_pool_id order))
        user (users/get-by-id tx (:user_id order))
        target-user-id (if (delegation? user)
                         (orders/delegated-user-id tx (:id order))
                         (:id user))
        target-user (users/get-by-id tx target-user-id)
        lang-locale (:locale (lang/get-the-one-to-use tx target-user-id))
        tmpl (get-tmpl tx "submitted" (:id inventory-pool) lang-locale)]
    (cond
     (not inventory-pool) (warn "Pool for sending email not found or it is inactive.")
     (not tmpl) (warn (format "No 'submitted' mail template found for pool '%s'." (:id inventory-pool)))
     :else (let [email-signature (:email_signature settings)
                 reservations (-> (sql/select :r.quantity, :r.start_date, :r.end_date
                                              [[:concat_ws " " :m.product :m.version] :model_name])
                                  (sql/from [:reservations :r])
                                  (sql/join [:models :m] [:= :r.model_id :m.id])
                                  (sql/where [:= :r.order_id (:id order)])
                                  sql-format
                                  (->> (jdbc-query tx)))
                 purpose (:purpose order)
                 order-url (str (:external_base_url settings) "/manage/" (:id inventory-pool) "/orders/" (:id order) "/edit")
                 email-body (wet/render (wet/parse tmpl)
                                        {:params {:user target-user
                                                  :inventory_pool inventory-pool
                                                  :email_signature email-signature
                                                  :reservations reservations
                                                  :comment nil
                                                  :purpose purpose
                                                  :order_url order-url}
                                         :filters {}})
                 to-address (or (:email target-user) (:secondary_email target-user))
                 from-address (or (:email inventory-pool) (:smtp_default_from_address settings))]
             (debug email-body)
             (-> (sql/insert-into :emails)
                 (sql/values [{:user_id target-user-id
                               :from_address from-address
                               :to_address to-address
                               :subject (t :borrow.mail-templates.submitted.subject lang-locale)
                               :body email-body}])
                 sql-format
                 (->> (jdbc-execute! tx)))))))

(comment (let [tx (db/get-ds-next)
               ctx {:request {:settings (settings! tx), :tx-next tx}}
               order (-> (sql/select :*)
                         (sql/from :orders)
                         (sql/where [:= :id #uuid "63b4e83e-e2c3-4924-ae89-3596824b1c95"])
                         sql-format
                         (->> (jdbc-query tx))
                         first)]
           ; order
           (send-received ctx order)))
