(ns leihs.borrow.lib.pagination
  (:require-macros [leihs.borrow.lib.macros :refer [spy]])
  (:require [re-frame.core :as rf]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch]]
            [re-graph.core :as re-graph]))

(reg-event-fx
  ::get-more
  (fn [{:keys [db]} [_ query query-vars db-path data-path]]
    (let [page-info (get-in db (conj db-path :page-info))
          has-next? (get page-info :has-next-page)
          query-vars (merge {:after-cursor (get page-info :end-cursor)}
                            query-vars)]
      (when (spy has-next?)
        {:dispatch-n (list [::fetching db-path true]
                           [::re-graph/query
                            query
                            query-vars
                            [::on-fetched-more db-path data-path]])}))))

(reg-event-fx
  ::on-fetched-more
  (fn [{:keys [db]} [_ db-path data-path {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:dispatch [::fetching db-path false]
       :db (-> db
               (assoc-in (conj db-path :page-info)
                         (get-in data (conj data-path :page-info)))
               (update-in (conj db-path :edges)
                          concat
                          (get-in data (conj data-path :edges))))})))

(reg-event-db
  ::fetching
  (fn [db [_ db-path yes-or-no]]
    (assoc-in db (concat db-path [:fetching]) yes-or-no)))
