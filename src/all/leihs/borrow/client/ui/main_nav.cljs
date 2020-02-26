(ns leihs.borrow.client.ui.main-nav
  (:require
   [re-frame.core :as rf]
   #_[shadow.resource :as rc]
   ["date-fns" :as datefn]

   [leihs.borrow.client.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]

   [leihs.borrow.client.ui.icons :as icons]))

(defn navbar []
  (let [current-order @(rf/subscribe [:leihs.borrow.client.features.shopping-cart.core/current-order])
        valid-until (some->> (:valid-until current-order) js/Date.)
        timeout? (some->> valid-until (datefn/isAfter (js/Date.)))

        style {:top 0 :z-index 1000
               :border-bottom-color "rgba(0,0,0, 0.08)"
               :backdrop-filter "blur(12px)" :-webkit-backdrop-filter "blur(12px)"
               :background "rgba(255,255,255, 0.83)"}

        ; partials
        toggler [:a.nav-item.nav-link.px-0 {:href (routing/path-for ::routes/about-page)}
                 icons/menu-icon]
        brand [:a.navbar-brand.m-0.font-black.text-xl {:href (routing/path-for ::routes/home)}
               (str " " "LEIHS" " ")]
        timer (when valid-until
                (if timeout?
                  [:span {:class "text-color-danger"} "!!!"]
                  [:span {:class "text-color-info"} (datefn/formatDistanceToNow (js/Date. valid-until))]))
        cart [:a.nav-item.nav-link.px-0 {:href (routing/path-for ::routes/shopping-cart)}
              icons/shopping-cart-icon]]

    [:nav.ui-main-nav.navbar.navbar-light.text-xl.shadow-md.py-0.px-2.sticky-top.flex-nowrap.justify-content-between
     {:style style}

     [:div.navbar-nav.w-100
      [:div.mr-auto toggler]]

     [:div.mx-auto brand]

     [:div.navbar-nav.w-100
      [:div.ml-auto.d-flex.align-items-center
       [:div.mx-auto.px-2.text-xs timer]
       " " cart]]]))
