(ns leihs.borrow.mails
  (:require [clojure.tools.logging :as log]
            [leihs.core.ring-exception :as ring-ex]
            [leihs.borrow.legacy :as legacy]))

(defn send-received [context order]
  (if (-> context :request :settings :deliver_received_order_notifications)
    (try
      (let [response (legacy/post "/mail/received" context {:order_id (:id order)})
            status (:status response)]
        (when-not (= status 202)
          (log/warn "Legacy responded with status:"
                    (str status ",")
                    "and body:"
                    (:body response))))
      (catch Throwable e
        (ring-ex/log e)))))

(defn send-submitted [context order]
  (if (-> context :request :settings :deliver_received_order_notifications)
    (try
     (let [response (legacy/post "/mail/submitted" context {:order_id (:id order)})
           status (:status response)]
       (when-not (= status 202)
         (log/warn "Legacy responded with status:"
                   (str status ",")
                   "and body:"
                   (:body response))))
     (catch Throwable e
       (ring-ex/log e)))))
