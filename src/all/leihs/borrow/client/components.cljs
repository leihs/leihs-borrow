(ns leihs.borrow.client.components
  (:require
   [reagent.core :as reagent]
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

(defn button [label active? props]
  (let 
   [base-cls
    #{:text-white  :font-bold
      :py-0 :px-2
      :rounded-full
      :border-solid :border-4 :border-black}]
    [:button
     (merge-props
      {:type :button
       :disabled (not active?)
       :class (cond-> base-cls
                active? (conj :bg-black :border-black)
                (not active?) (conj :bg-grey :border-grey))}
      props)
     label]))

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
(def shopping-cart-icon [:span.ui-icon.ui-icon-colored.ui-shopping-cart-icon "🛒"])
(defn main-nav []
  [:nav.ui-main-nav.px-2.py-1.border-b-2.flex.flex-wrap.items-center.justify-between.sticky.bg-content
   {:style {:top 0}}
   [:span [:a {:href (routing/path-for ::routes/about-page)} menu-icon]]
   [:h1.inline.font-black [:a {:href (routing/path-for ::routes/home)} "LEIHS"]]
   " "
   [:h2.inline.text-sm [:a {:href (routing/path-for ::routes/shopping-cart)} shopping-cart-icon]]])


(defn tmp-nav []
  [:nav.border.border-black.m-3.p-2
   [:b "tmp nav"]
   [:p [:a {:href (routing/path-for ::routes/home)} "home"]]

   [:p [:a {:href (routing/path-for ::routes/about-page)} "about"]]
   [:p [:a {:href (routing/path-for ::routes/models-index)} "test model index"]]
   [:p [:a {:href
            (routing/path-for
             ::routes/models-show
             :model-id "1c18b3d3-88e8-57ac-8c28-24d3f8f77604")}
        "test model show"]]])


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