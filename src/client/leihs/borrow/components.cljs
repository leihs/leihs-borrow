(ns leihs.borrow.components
  (:refer-clojure :exclude [time])
  (:require
    [clojure.string :refer [upper-case]]
    [reagent.core :as reagent]
    [leihs.borrow.features.sign-in.core :as sign-in]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.lib.translate :refer [t]]
    [leihs.borrow.client.routes :as routes]
    [leihs.borrow.csrf :as csrf]))

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

(defn error-screen [errors]
  (when-let [[e] (not-empty errors)]
    (if (-> e :extensions :code (= 401))
      [sign-in/login-error-screen]
      [fatal-error-screen errors])))

(defn tmp-nav []
  [:nav.border.border-black.mb-3.p-2
   [:b (upper-case (t :borrow.about-page/navigation-menu))]

   [:p [:a {:href (routing/path-for ::routes/about-page)} (t :borrow.about-page/title)]]
   [:p [:a {:href (routing/path-for ::routes/shopping-cart)} (t :borrow.shopping-cart/title)]]
   [:p [:a {:href (routing/path-for ::routes/draft-order)} (t :borrow.shopping-cart.draft/title)]]
   [:p [:a {:href (routing/path-for ::routes/categories-index)} (t :borrow.categories/title)]]
   [:p [:a {:href (routing/path-for ::routes/current-user-show)} (t :borrow.current-user/title)]]
   [:p [:a {:href (routing/path-for ::routes/delegations-index)} (t :borrow.delegations/title)]]
   [:p [:a {:href (routing/path-for ::routes/home)} (t :borrow.home-page/title)]]
   [:p [:a {:href (routing/path-for ::routes/models-favorites)} (t :borrow.favorite-models/title)]]
   [:p [:a {:href (routing/path-for ::routes/orders-index)} (t :borrow.customer-orders/title)]]
   [:p [:a {:href (routing/path-for ::routes/pickups-index)} (t :borrow.visits.pickups/title)]]
   [:p [:a {:href (routing/path-for ::routes/returns-index)} (t :borrow.visits.returns/title)]]
   [:p [:a {:href (routing/path-for ::routes/templates-index)} (t :borrow.templates/title)]]
   [:p [:a {:href (routing/path-for ::routes/inventory-pools-index)} (t :borrow.pools/title)]]
   [:form {:action "/sign-out" :method "POST"}
    [csrf/token-field]
    [:button {:type "submit"}
     (t :borrow/logout)]]])

(defn dev-nav []
  [:nav.border.border-black.mb-3.p-2
   [:b "dev nav"]

   [:p [:a {:href (routing/path-for ::routes/categories-show
                                    :categories-path "09ac0343-0d83-5c7f-b112-d5921e9479fd")}
        "a category show"]]
   [:p [:a {:href (routing/path-for ::routes/models)} "model index"]]
   [:p [:a {:href
            (routing/path-for
              ::routes/models-show
              :model-id "1c18b3d3-88e8-57ac-8c28-24d3f8f77604")}
        "a model show"]]
   [:p [:a {:href "/app/borrow/graphiql/index.html"} "Graph" [:i "i"] "QL API console"]]])


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
