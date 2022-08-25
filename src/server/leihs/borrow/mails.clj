(ns leihs.borrow.mails
  (:require
    [clojure.tools.logging :as log]
    [leihs.borrow.legacy :as legacy ]
    [leihs.core.ring-exception :as ring-ex :refer [logstr]]
    [taoensso.timbre :refer [debug info warn error]]
    ))

(defn send-received [context order]
  (when (-> context :request :settings :deliver_received_order_notifications)
    (try
      (let [response (legacy/post "/mail/received" context {:order_id (:id order)})
            status (:status response)]
        (when-not (= status 202)
          (log/warn "Legacy responded with status:"
                    (str status ",")
                    "and body:"
                    (:body response))))
      (catch Throwable e
        (error (ex-message e) (ex-data e) (logstr e))))))

(defn send-submitted [context order]
  (try
    (let [response (legacy/post "/mail/submitted" context {:order_id (:id order)})
          status (:status response)]
      (when-not (= status 202)
        (log/warn "Legacy responded with status:"
                  (str status ",")
                  "and body:"
                  (:body response))))
    (catch Throwable e
      (error (ex-message e) (ex-data e) (logstr e)))))
