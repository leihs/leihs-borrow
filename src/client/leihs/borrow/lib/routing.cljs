;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; based on <https://github.com/MattiNieminen/re-fill/blob/960b4eaf/src/re_fill/routing.cljs> ;
; * replace :re-fill/ :routing/                                                              ;
; * add support for query params
;   * when matching routes
;   * when navigating using dispatch
;   * when using `path-for`  
; * pushy: only capture&handle nav events if we defined a route for it!
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns leihs.borrow.lib.routing
  (:require [bidi.bidi :as bidi]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [pushy.core :as pushy]
            [cemerick.url]
            [re-frame.core :as rf]
            [re-frame.db :as db]
            [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                               reg-event-db
                                               reg-sub
                                               reg-fx
                                               subscribe
                                               dispatch]]
            [leihs.borrow.lib.localstorage :as ls]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.client.routes :as routes]))

; from <https://github.com/juxt/bidi/issues/51#issuecomment-344101759>
(defn bidi-match-route-with-query-params
  [routes path & {:as options}]
  (let [query-params (->> (:query (cemerick.url/url path))
                          (map (fn [[k v]] [(keyword k) v]))
                          (into {}))]
    (-> (bidi/match-route* routes path options)
        (assoc :query-params query-params))))

(reg-fx
  :routing/init-routing
  (fn [routes]
    (let [pushy-instance (get-in @db/app-db [:routing/routing :pushy-instance])]
      (if-not pushy-instance
        (let [dispatch-fn (fn [arg]
                            (current-user/fetch-and-save
                              #(dispatch [:routing/change-view arg])))
              match-fn (fn [path] 
                         (let [is-routed? (:handler (bidi/match-route routes path))]
                           (if is-routed? path false)))]
          (->> (pushy/pushy dispatch-fn match-fn)
               (swap! db/app-db assoc-in [:routing/routing :pushy-instance])
               :routing/routing
               :pushy-instance
               pushy/start!))))))

(reg-event-fx
  :routing/init-routing
  (fn-traced [{:keys [db]} [_ routes]]
    {:db (assoc-in db [:routing/routing :routes] routes)
     :routing/init-routing routes}))

(reg-event-fx
  :routing/change-view
  (fn-traced [{:keys [db]} [_ token]]
    (let [{:keys [routes]} (:routing/routing db)
          bidi-match (bidi-match-route-with-query-params routes token)]
      {:db (assoc-in db [:routing/routing :bidi-match] bidi-match)
       :dispatch-n (list [::scroll-to-top true]
                         [(:handler bidi-match) bidi-match]
                         [::current-user/fetch]
                         [:leihs.borrow.features.shopping-cart.timeout/refresh])})))

(reg-event-fx
  :routing/navigate
  (fn-traced [_ [_ [route-key route-args]]]
    (let
      [bidi-args (dissoc route-args :query-params)
       query-params (get route-args :query-params)]
      {:routing/navigate [route-key bidi-args query-params]})))

(reg-fx
  :routing/navigate
  (fn [[route-key bidi-args query-params]]
    (let [{:keys [pushy-instance routes]} (:routing/routing @db/app-db)
          bidi-path (->> bidi-args
                         seq
                         flatten
                         (apply bidi/path-for routes route-key))
          query-string (when-not (empty? query-params) 
                         (str "?" (cemerick.url/map->query query-params)))
          path (str bidi-path query-string)]
      (if path
        (pushy/set-token! pushy-instance path)
        (js/console.error "No matching route for" bidi-args)))))

(reg-fx
  :routing/navigate-raw
  (fn [url]
    (aset (.-location js/window) "href" url)))

(reg-event-fx
  :routing/navigate-raw
  (fn-traced [_ [_ url]]
    {:routing/navigate-raw url}))

(reg-event-fx
  :routing/refresh-page
  (fn-traced [_] {:routing/refresh-page nil}))

(reg-event-fx
  :routing/navigate-back
  (fn [_]
    (js/window.history.back)))

(reg-fx
  :routing/refresh-page
  (fn [_]
    (.reload (.-location js/window))))

(reg-event-fx ::scroll-to-top (fn-traced [_] {::scroll-to-top nil}))
(reg-fx ::scroll-to-top (fn [_] (js/window.scrollTo 0 0)))

(reg-sub
  :routing/routing
  (fn [db _]
    (:routing/routing db)))

(defn routed-view
  [views]
  (let [r @(subscribe [:routing/routing])
        component (or (get views (get-in r [:bidi-match :handler]))
                      (:else views))]
    [component]))

(defn bidi-path-for-with-query-params [routes-map name & args]
  (if-not (= (namespace name) (namespace ::routes/ns))
    (throw (js/Error. (str "route name is from wrong namespace! " (pr-str name)))))
  (let [route-args args ; FIXME: remove :query params from seq!
        query-params (get (apply hash-map args) :query-params)
        bidi-path (apply bidi/path-for routes-map name route-args)
        query-string (when-not (empty? query-params)
                       (str "?" (cemerick.url/map->query query-params)))]
    ; (js/console.log 'bp [name bidi-path])
    (str bidi-path query-string)))

; TODO: define this next to routes (so this lib does not need to import routes!)
(defn path-for [name & args]
  (apply
    bidi-path-for-with-query-params routes/routes-map name
    args))
