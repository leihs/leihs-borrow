(ns leihs.borrow.client.lib.pagination
  (:require-macros [leihs.borrow.client.macros :refer [spy]])
  (:require [re-frame.core :as rf]
            [re-graph.core :as re-graph]))

(rf/reg-event-fx
  ::get-more
  (fn [{:keys [db]} [_ query query-vars db-path data-path]]
    (let [page-info (get-in db (conj db-path :pageInfo))
          has-next? (get page-info :hasNextPage)
          query-vars (merge {:afterCursor (get page-info :endCursor)}
                            query-vars)]
      (when (spy has-next?)
        {:dispatch [::re-graph/query
                    query
                    query-vars
                    [::on-fetched-more db-path data-path]]}))))

(rf/reg-event-fx
  ::on-fetched-more
  (fn [{:keys [db]} [_ db-path data-path {:keys [data errors]}]]
    (if errors
      {:db (update-in db [:meta :app :fatal-errors] (fnil conj []) errors)}
      {:db (-> db
               (assoc-in (conj db-path :pageInfo)
                         (get-in data (conj data-path :pageInfo)))
               (update-in (conj db-path :edges)
                          concat
                          (get-in data (conj data-path :edges))))})))

