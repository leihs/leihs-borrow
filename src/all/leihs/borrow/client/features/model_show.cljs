(ns leihs.borrow.client.features.model-show
  (:require
   #_[reagent.core :as r]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.client.components :as ui]
   [leihs.borrow.client.routes :as routes]
   #_[leihs.borrow.client.components :as ui]))


; is kicked off from router when this view is loaded
(rf/reg-event-fx
 ::routes/models-show
 (fn [_ [_ args]]
   (let [model-id (get-in args [:route-params :model-id])]
     {:dispatch [::re-graph/query
                 (rc/inline "leihs/borrow/client/queries/getModelShow.gql")
                 {:modelId model-id}
                 [::on-fetched-data model-id]]})))

(rf/reg-event-db
 ::on-fetched-data
 (fn [db [_ model-id {:keys [data errors]}]]
   (-> db
       (update-in , [:models model-id] (fnil identity {}))
       (assoc-in , [:models model-id :errors] errors)
       (assoc-in , [:models model-id :data] data))))

(rf/reg-sub 
 ::model-data
 (fn [db [_ id]]
   (get-in db [:models id])))

(def decorate-file-size-formatter
  (js/Intl.NumberFormat.
   js/navigator.language
   (clj->js {:maximumFractionDigits 2
    :style :unit
    :unit :megabyte})))

(defn decorate-file-size [bytes]
  (.format decorate-file-size-formatter (-> bytes (/ (* 1024 1024)))))

(defn view []
  (let
   [routing @(rf/subscribe [:routing/routing])
    model-id (get-in routing [:bidi-match :route-params :model-id])
    fetched @(rf/subscribe [::model-data model-id])
    model (get-in fetched [:data :model])
    errors (:errors fetched)
    is-loading? (not (or model errors))]

    [:section.m-3
     (cond 
       is-loading? [:pre "loading model" [:samp model-id] "…"]
       errors [ui/error-view errors]
       :else
        [:<>
         [:header
          [:h1.text-3xl.font-extrabold.leading-none 
           (:name model)
           [:span " "]
           [:small.font-normal.text-gray-600.leading-none (:manufacturer model)]]]

         ; FIXME: show all images not just the first one
         (if-let [first-image (first (:images model))]
           [:div.flex.justify-center.py-4.mt-4.border-b-2.border-gray-300
            [:img {:src (:imageUrl first-image)}]])

         [:p.py-4.border-b-2.border-gray-300 (:description model)]

         (if-let [attachments  (:attachments model)]
           [:<>
            [:ul.list-inside.list-disc.text-blue-600
             (doall
              (for [a attachments]
                [:<> {:key (:id a)}
                 [:li.border-b-2.border-gray-300.py-2
                  [:a.text-blue-500 {:href (:url a)} (:filename a)]
                  [:small.text-gray-600 (str " (" (decorate-file-size (:size a)) ")")]]]))]])

         (if-let [fields (not-empty (map vector (:properties model)))]
           [:dl.pb-4.mb-4.mt-4.border-b-2.border-gray-300
            (doall
             (for [[field] fields]
               [:<> {:key (:id field)}
                [:dt.font-bold (:key field)]
                [:dd.pl-6 (:value field)]]))])

         (if-let [recommends (-> model :recommends :edges not-empty)]
           [:div.mt-4
            [:h2.text-xl.font-bold "Ergänzende Modelle"]
            [:div.flex.flex-wrap.-mx-2
             (doall
              (for [edge recommends]
                (let 
                 [rec (:node edge)
                  href (str "/borrow/models/" (:id rec))]
                  [:div {:key (:id rec) :class "w-1/2"}
                   [:div.p-2
                     ; FIXME: use path helper!
                    [:div.square-container.relative.rounded.overflow-hidden.border.border-gray-200
                     [:a {:href href}
                      (if-let [img (get-in rec [:images 0 :imageUrl])]
                        [:img.absolute.object-contain.object-center.h-full.w-full.p-1 {:src img}]
                        [:div.absolute.h-full.w-full.bg-gray-400 " "])]]
                    
                    [:a.text-gray-700.font-semibold {:href href}
                     (:name rec)]]])))]])

         #_[:p.debug (pr-str model)]] )]))
