; FIXME: cancel action
; TODO: is-cancelable? from API


(ns leihs.borrow.features.customer-orders.show
  (:require
   ["date-fns" :as datefn]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   #_[reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-graph.core :as re-graph]
   [shadow.resource :as rc]
   [leihs.borrow.components :as ui]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                      reg-event-db
                                      reg-sub
                                      reg-fx
                                      subscribe
                                      dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.lib.filters :as filters]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.features.customer-orders.core :as rentals]
   [leihs.borrow.features.customer-orders.index :refer [rental-progress-bars]]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.rental-show)

; is kicked off from router when this view is loaded
(reg-event-fx
 ::routes/rentals-show
 (fn-traced [{:keys [db]} [_ args]]
            (let [order-id (get-in args [:route-params :rental-id])]
              {:dispatch [::re-graph/query
                          (rc/inline "leihs/borrow/features/customer_orders/customerOrderShow.gql")
                          {:id order-id, :userId (filters/user-id db)}
                          [::on-fetched-data order-id]]})))

(reg-event-db
 ::on-fetched-data
 (fn-traced [db [_ order-id {:keys [data errors]}]]
            (-> db
                (update-in , [::data order-id] (fnil identity {}))
                (cond-> errors (assoc-in , [::errors order-id] errors))
                (assoc-in , [::data order-id] (into (sorted-map) (:rental data))))))

(reg-sub ::data
         (fn [db [_ id]] (get-in db [::data id])))

(reg-sub ::errors
         (fn [db [_ id]] (get-in db [::errors id])))

(reg-sub ::reservations
         (fn [[_ id]] (subscribe [::data id]))
         (fn [co _] #_(h/log co) (:reservations co)))

(reg-sub ::reservations-grouped
         (fn [[_ id]] (subscribe [::reservations id]))
         (fn [lines _]
           (->> lines
                (group-by
                 (fn [line]
                   [(get-in line [:model :id])
                    (get-in line [:inventory-pool :id])
                    (get-in line [:start-date])
                    (get-in line [:end-date])])))))

(defn ui-item-line [reservation]
  (let [model (:model reservation)
        option (:option reservation)
        name (:name (or model option))
        quantity (:quantity reservation)
        start-date (js/Date. (:start-date reservation))
        end-date (js/Date. (:end-date reservation))
        ;; NOTE: should be in API
        total-days (+ 1 (datefn/differenceInCalendarDays end-date start-date))
        title (t :reservation-line.title {:itemCount quantity, :itemName name})
        duration (t :reservation-line.duration {:totalDays total-days, :fromDate start-date})
        sub-title (get-in reservation [:inventory-pool :name])
        href (when model (routing/path-for ::routes/models-show :model-id (:id (:model reservation))))]
    [:<>
     [:> UI/Components.Design.ListCard {:href href}
      [:> UI/Components.Design.ListCard.Title
       [:a.stretched-link {:href href}
        title]]

      [:> UI/Components.Design.ListCard.Body
       sub-title]

      [:> UI/Components.Design.ListCard.Foot
       [:> UI/Components.Design.Badge duration]
       ;
       ]]]))

(defn ui-items-list [grouped-reservation]
  [:> UI/Components.Design.ListCard.Stack #_{:divided true :space 3}
  ;;  (h/log items)
   (doall
    (for [[_ items] grouped-reservation]
      (for [item items]
        (when item
          [:<> {:key (:id item)}
           [ui-item-line item]]))))])

(defn view []
  (let [routing @(subscribe [:routing/routing])
        rental-id (get-in routing [:bidi-match :route-params :rental-id])
        rental @(subscribe [::data rental-id])
        grouped-reservations @(subscribe [::reservations-grouped rental-id])
        errors @(subscribe [::errors rental-id])
        is-loading? (not (or rental errors))

        rental-title  (or (:title rental) (:purpose rental))

        is-cancelable? (= ["IN_APPROVAL"] (:fulfillment-states rental))
        contracts (map :node (get-in rental [:contracts :edges]))]

    [:<>
     (cond

       is-loading?
       [:<>
        [:> UI/Components.Design.PageLayout.Header
         {:title (t :page-title)}]
        [:div [:div.text-center.text-5xl.show-after-1sec [ui/spinner-clock]]]]

       errors [ui/error-view errors]

       :else
       [:<>
        [:> UI/Components.Design.PageLayout.Header
         {:title rental-title}

         [:h2.fw-light (rentals/rental-summary-text rental)]]

        [:> UI/Components.Design.Stack {:space 5}

         [:> UI/Components.Design.Section
          {:title (t :state) :collapsible true}

          [:> UI/Components.Design.Stack {:space 3}
           (rental-progress-bars rental false)

           (when is-cancelable?
             [:> UI/Components.Design.ActionButtonGroup
              [:button.btn.btn-secondary {:onClick #(js/alert "TODO: Filters")} (t :cancel-action-label)]])]]

         [:> UI/Components.Design.Section
          {:title (t :purpose) :collapsible true}
          (:purpose rental)]

         [:> UI/Components.Design.Section
          {:title (t :pools-section-title) :collapsible true}
          [:span.text-danger "TODO: list of inventory pools + progress bars"]]

         [:> UI/Components.Design.Section
          {:title (t :items-section-title) :collapsible true}
          (ui-items-list grouped-reservations)]

         [:> UI/Components.Design.Section
          {:title (t :user-or-delegation-section-title) :collapsible true}
          [:p.text-danger "TODO: Delegation"]
          [:p.text-danger "TODO: if user: " (t :user-or-delegation-personal-postfix)]]

         (when (seq contracts)
           [:> UI/Components.Design.Section
            {:title (t :documents-section-title) :collapsible true}

            [:> UI/Components.Design.Stack {:space 3}
             (doall
              (for [contract contracts]
                ^{:key (:id contract)}
                [:<>
                 [:> UI/Components.Design.DownloadLink
                  {:href (:print-url contract)}
                  (t :!borrow.terms/contract) " " (:compact-id contract) " "
                  [:span.fw-light (str "(" (ui/format-date :short (:created-at contract)) ")")]]]))]])]

        [:> UI/Components.Design.PageLayout.MetadataWithDetails
         {:summary (t :metadata-summary {:rentalId (:id rental)})
          :details (clj->js (h/camel-case-keys rental))}]

        ;
        ])]))
