(ns leihs.borrow.client.features.model-show
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   #_[leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]
   #_[leihs.borrow.client.components :as ui]))


; is kicked of from router
(rf/reg-event-fx
 ::routes/models-show
 (fn [{:keys [db]} [_ args]]
   (let [model-id (get-in args [:route-params :model-id])]
     {:db (update-in db [:models model-id] (fnil identity {}))
      :dispatch [::fetch-model model-id]})))

(rf/reg-event-fx
 ::fetch-model
 (fn [{:keys [db]} [_ model-id]]
   {:db (assoc-in db [:models model-id :is-loading] true)
    :dispatch [::re-graph/query
               (rc/inline "leihs/borrow/client/queries/getModelShow.gql")
               {:modelId model-id}
               [::on-fetched-model model-id]]}))

(rf/reg-event-db
 ::on-fetched-model
 (fn [db [_ model-id {:keys [data errors]}]]
   (-> db
       (assoc-in , [:models model-id :errors] errors)
       (assoc-in , [:models model-id :data] data)
       (assoc-in , [:models model-id :is-loading] false))))

(rf/reg-sub 
 ::model-data
 (fn [db [_ id]]
   (get-in db [:models id])))

(defn view [] 
  (let
   [routing @(rf/subscribe [:routing/routing])
    model-id (get-in routing [:bidi-match :route-params :model-id])
    model-data @(rf/subscribe [::model-data model-id])]

    [:section
     [:h1.font-xl "MODELS SHOW " [:samp model-id]]
     [:p (pr-str model-data)]]))
