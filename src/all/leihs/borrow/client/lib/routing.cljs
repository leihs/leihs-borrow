;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; based on <https://github.com/MattiNieminen/re-fill/blob/960b4eaf/src/re_fill/routing.cljs> ;
; * replace :re-fill/ :routing/                                                              ;
; * add support for query params                                                             ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns leihs.borrow.client.lib.routing
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [cemerick.url]
            [re-frame.core :as rf]
            [re-frame.db :as db]))

; from <https://github.com/juxt/bidi/issues/51#issuecomment-344101759>
(defn bidi-match-route-with-query-params
  [route path & {:as options}]
  (let [query-params (->> (:query (cemerick.url/url path))
                          (map (fn [[k v]] [(keyword k) v]))
                          (into {}))]
    (-> (bidi/match-route* route path options)
        (assoc :query-params query-params))))

(rf/reg-fx
 :routing/init-routing
 (fn [_]
   (let [pushy-instance (get-in @db/app-db [:routing/routing :pushy-instance])]
     (if-not pushy-instance
       (->> (pushy/pushy #(rf/dispatch [:routing/change-view %]) identity)
            (swap! db/app-db assoc-in [:routing/routing :pushy-instance])
            :routing/routing
            :pushy-instance
            pushy/start!)))))

(rf/reg-event-fx
 :routing/init-routing
 (fn [{:keys [db]} [_ routes]]
   {:db (assoc-in db [:routing/routing :routes] routes)
    :routing/init-routing nil}))

(rf/reg-event-fx
 :routing/change-view
 (fn [{:keys [db]} [_ token]]
   (let [{:keys [routes]} (:routing/routing db)
         bidi-match (bidi-match-route-with-query-params routes token)]
     {:db (assoc-in db [:routing/routing :bidi-match] bidi-match)
      :dispatch [(:handler bidi-match) bidi-match]})))

(rf/reg-fx
 :routing/navigate
 (fn [[route-key bidi-args query-params]]
   (let [{:keys [pushy-instance routes]} (:routing/routing @db/app-db)
         bidi-path (bidi/path-for routes route-key bidi-args)
         query-string (when-not (empty? query-params) 
                        (str "?" (cemerick.url/map->query query-params)))
         path (str bidi-path query-string)]
     (if path
       (pushy/set-token! pushy-instance path)
       (js/console.error "No matching route for" bidi-args)))))

(rf/reg-event-fx
 :routing/navigate
 (fn [_ [_ [route-key route-args]]]
   (let
    [bidi-args (dissoc route-args :query-params)
     query-params (get route-args :query-params)]
     {:routing/navigate [route-key bidi-args query-params]})))

(rf/reg-fx
 :routing/navigate-raw
 (fn [url]
   (aset (.-location js/window) "href" url)))

(rf/reg-event-fx
 :routing/navigate-raw
 (fn [_ [_ url]]
   {:routing/navigate-raw url}))

(rf/reg-event-fx
 :routing/refresh-page
 (fn [_] {:routing/refresh-page nil}))

(rf/reg-fx
 :routing/refresh-page
 (fn [_]
   (.reload (.-location js/window))))

(rf/reg-sub
 :routing/routing
 (fn [db _]
   (:routing/routing db)))

(defn routed-view
  [views]
  (let [r @(rf/subscribe [:routing/routing])
        component (or (get views (get-in r [:bidi-match :handler]))
                      (:else views))]
    [component]))