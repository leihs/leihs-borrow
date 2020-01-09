(ns leihs.borrow.client.components
  (:refer-clojure :exclude [time])
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   ["date-fns" :as datefn]
   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]))

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

(defn button [label active? props]
  (let 
   [base-cls
    #{:text-color-content-inverse  :font-bold
      :py-0 :px-2
      :rounded-full
      :border-solid :border-4 :border-black}]
    [:button
     (merge-props
      {:type :button
       :disabled (not active?)
       :class (cond-> base-cls
                active? (conj :bg-content-inverse :border-black)
                (not active?) (conj :bg-grey :border-grey))}
      props)
     label]))

(defn image-square-thumb [image href]
  (let 
   [img-src (:imageUrl image)
    inner
    (if img-src
      [:img.absolute.object-contain.object-center.h-full.w-full.p-1.bg-content {:src img-src}]
      [:span.block.absolute.h-full.w-full.bg-gray-400 " "])]
    
    [:div.square-container.relative.rounded.overflow-hidden.border.border-gray-200
     (if href [:a {:href href} inner] inner)]))

(defn error-view [errors]
  [:section.p-4
   {:style {:white-space "pre-wrap" :background "salmon" :padding "1rem"}}
   [:h1 "ERROR :("]
   [:p [:button.border-black.border-2.rounded-full.py-1.px-3 {:type :button, :on-click #(-> js/window (.-location) (.reload))} "RELOAD"]]
   (doall
    (for
     [[idx error] (map-indexed vector errors)]
      [:small.code {:key idx} (js/JSON.stringify (clj->js error) 0 2)]))])

(defn fatal-error-screen [errors]
  [:section.p-4
   {:style {:white-space "pre-wrap" :background "salmon" :padding "1rem"}}
   [:h1 "FATAL ERROR :("]
   [:p [:button.border-black.border-2.rounded-full.py-1.px-3 {:type :button, :on-click #(-> js/window (.-location) (.reload))} "RELOAD"]]
   (doall
    (for
     [[idx error] (map-indexed vector errors)]
      [:small.code {:key idx} (js/JSON.stringify (clj->js error) 0 2)]))])

(def menu-icon [:span.ui-icon.ui-menu-icon "☰"])
(def trash-icon [:span.ui-icon.ui-icon-colored.ui-trash-icon "🗑️"])
(def shopping-cart-icon [:span.ui-icon.ui-icon-colored.ui-shopping-cart-icon "🛒"])

(def -heart-icon-styles {:display "inline-block" :text-align "center" :width "1em" :height "1em" :vertical-align "middle" :line-height 1})
(def favorite-yes-icon [:span.ui-icon.ui-favorite-yes-icon  
                        [:span {:style -heart-icon-styles}"♥"]])
(def favorite-no-icon [:span.ui-icon.favorite-no-icon 
                       [:span {:style (merge -heart-icon-styles {:line-height "0.85em"})} 
                       [:span {:style {:font-size "75%" }} "♡"]]])

(defn main-nav []
  (let [current-order @(rf/subscribe [:leihs.borrow.client.features.shopping-cart.core/current-order])
        valid-until (some->> (:valid-until current-order) js/Date.)
        timeout? (some->> valid-until (datefn/isAfter (js/Date.) ,))]
    [:nav.ui-main-nav.px-2.border-b.shadow-md.flex.flex-wrap.items-center.justify-between.sticky.bg-content.text-xl.py-2
     {:style 
      {:top 0 :z-index 1000
       :border-bottom-color "rgba(0,0,0, 0.08)"
       :backdrop-filter "blur(12px)" :-webkit-backdrop-filter "blur(12px)"
       :background "rgba(255,255,255, 0.83)"}}
     [:span [:a {:href (routing/path-for ::routes/about-page)} menu-icon]]
     [:span.font-black [:a {:href (routing/path-for ::routes/home)} "LEIHS"]]
     " "
     [:span.text-sm
      (if timeout?
        [:span {:class "text-color-danger" } "!!!"]
        [:span {:class "text-color-info"} (datefn/formatDistanceToNow (js/Date. valid-until))])
      " "
      [:a {:href (routing/path-for ::routes/shopping-cart)} shopping-cart-icon]]]))

(defn tmp-nav []
  [:nav.border.border-black.m-3.p-2
   [:b "NAVIGATION MENU"]

   [:p [:a {:href (routing/path-for ::routes/home)} "Home"]]
   [:p [:a {:href (routing/path-for ::routes/models-favorites)} "Favorites"]]
   [:p [:a {:href (routing/path-for ::routes/orders-index)} "Orders"]]
   [:p [:a {:href (routing/path-for ::routes/about-page)} "About"]]])

(defn dev-nav []
  [:nav.border.border-black.m-3.p-2
   [:b "dev nav"]

   [:p [:a {:href (routing/path-for ::routes/categories-show
                                    :categories-path "09ac0343-0d83-5c7f-b112-d5921e9479fd")}
        "a category show"]]
   [:p [:a {:href (routing/path-for ::routes/models-index)} "model index"]]
   [:p [:a {:href
            (routing/path-for
             ::routes/models-show
             :model-id "1c18b3d3-88e8-57ac-8c28-24d3f8f77604")}
        "a model show"]]])


; copied from <https://github.com/sindresorhus/cli-spinners/blob/af93e2f345a73a16c7686066c08dd970d66d8870/spinners.json#L720>
(def spinner-data-clock
  {
		:interval 100,
		:frames ["🕛 " "🕐 " "🕑 " "🕒 " "🕓 " "🕔 " "🕕 " "🕖 " "🕗 " "🕘 " "🕙 " "🕚 "]
	})

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


