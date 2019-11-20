(ns leihs.borrow.client.components
  (:require
   #_[reagent.core :as r]
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

(defn main-nav []
  [:nav.ui-main-nav.px-2.py-1.border-b-2
   [:h1.inline.font-black [:a {:href (routing/path-for ::routes/home)} "AUSLEIHE"]]
   " "
   [:h2.inline.text-sm [:a {:href (routing/path-for ::routes/about-page)} "about"]]
   " "
   #_[:h2.inline.text-sm [:a {:href (routing/path-for ::routes/about-page)} "cart"]]])


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