(ns leihs.borrow.components
  (:refer-clojure :exclude [time])
  (:require
   [reagent.core :as reagent]
   [leihs.borrow.lib.translate :refer [t]]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.helpers :as h]
   [leihs.borrow.lib.re-frame :refer [dispatch]]
   [leihs.borrow.lib.errors :as errors]
   [leihs.borrow.lib.routing :as routing]
   ["/leihs-ui-client-side-external-react" :as UI]))

(defn merge-props [defaults givens]
  (merge
   defaults
   givens
   {:class (->> (get defaults :class)
                (concat (flatten [(get givens :class)]))
                (map keyword)
                set)}
   {:style (merge (get defaults :style)
                  (get givens :style))}))

; chars
(def thin-space \u2009)
(def non-breaking-space \u00A0)
(def en-space \u2003)
(def em-space \u2003)
(def nbsp non-breaking-space)

(defn image-square-thumb [image href]
  (let
   [img-src (:image-url image)
    inner
    (if img-src
      [:img.position-absolute.object-contain.object-center.h-full.w-full.p-1.bg-content {:src img-src}]
      [:span.d-block.position-absolute.h-full.w-full.bg-gray-400 " "])]

    [:div.square-container.position-relative.rounded.overflow-hidden.border.border-gray-200
     (if href [:a {:href href} inner] inner)]))

(defn error-view [errors]
  (let [has-401 (some #(= 401 (-> % :extensions :code)) errors)]
    [:> UI/Components.Design.ErrorView
     {:title (t :borrow.errors.loading-error)
      :message (when has-401 (t :borrow.errors.unauthorized))
      :actions (if has-401
                 [{:title (t :borrow.errors.go-to-login) :onClick #(js/document.location.reload)}] ; (server will send the correct redirect)
                 [{:title (t :borrow.errors.reload) :onClick #(js/document.location.reload)}
                  {:title (t :borrow.errors.go-to-start) :href (routing/path-for ::routes/home) :variant "link-button"}])
      :details (reagent/as-element
                (doall
                 (for
                  [[idx error] (map-indexed vector errors)]
                   [:div.preserve-linebreaks.mt-3 {:key idx} (js/JSON.stringify (clj->js error) 0 2)])))}]))

(defn error-notification [errors]
  (when (seq errors)
    (let [has-401 (some #(= 401 (-> % :extensions :code)) errors)]
      [:> UI/Components.Design.ErrorNotification
       {:shown true :onDismiss #(dispatch [::errors/clear]) :title (t :borrow.errors.error)}
       (if has-401
         [:<>
          [:div.mb-3 (t :borrow.errors.unauthorized)]
          [:div [:button.btn.btn-dark {:onClick #(js/document.location.reload)} (t :borrow.errors.go-to-login)]]]
         [:div (t :borrow.errors.processing-error)])
       [:details.mt-4.mb-4
        (doall
         (for
          [[idx error] (map-indexed vector errors)]
           [:div.preserve-linebreaks.mt-3.small {:key idx} (js/JSON.stringify (clj->js error) 0 2)]))]])))

; copied from <https://github.com/sindresorhus/cli-spinners/blob/af93e2f345a73a16c7686066c08dd970d66d8870/spinners.json#L720>
(def spinner-data-clock
  {:interval 100
   :frames ["🕛 " "🕐 " "🕑 " "🕒 " "🕓 " "🕔 " "🕕 " "🕖 " "🕗 " "🕘 " "🕙 " "🕚 "]})

(defn spinner-clock []
  (let [spinner spinner-data-clock
        speed (:interval spinner)
        frame (reagent/atom 0)]
    (fn []
      (js/setTimeout
       (fn [] (swap! frame #(mod (inc %) (count (:frames spinner)))))
       speed)

      [:span.ui-spinner.ui-spinner-clock
       [:span.ui-icon-colored (get-in spinner [:frames @frame])]])))

(defn loading [loading-text]
  [:div.text-center.fw-light {:style {:color "#cccccc"}}
   (cond loading-text [:div.mb-3 loading-text])
   [:div.text-5xl.show-after-1sec [spinner-clock]]])

; ; Intl stuff
(defn format-date [style date]
  (let [date (if (= (.-constructor date) js/Date) date (js/Date. date))]
    ; style oneof :full :long :medium :short
    (-> (js/Intl.DateTimeFormat. #_nil {:dateStyle style}) (.format date))))

(def decorate-file-size-formatter
  (js/Intl.NumberFormat.
   :default
   (clj->js {:maximumFractionDigits 2 :style :decimal})))

(defn decorate-file-size [bytes]
  (str
   (.format decorate-file-size-formatter (-> bytes (/ (* 1024 1024))))
   nbsp
   "MB"))
