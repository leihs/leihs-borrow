(ns leihs.borrow.ui.main-nav
  (:require
    [re-frame.core :as rf]
    #_[shadow.resource :as rc]
    ["date-fns" :as datefn]

    [leihs.borrow.lib.re-frame :refer [subscribe]]
    [leihs.borrow.lib.routing :as routing]
    [leihs.borrow.client.routes :as routes]

    [leihs.borrow.features.shopping-cart.core :as cart]

    [leihs.borrow.ui.icons :as icons]))

(defn navbar []
  (let [cart-data @(subscribe [:leihs.borrow.features.shopping-cart.core/data])

        style {:top 0 :z-index 1000
               :border-bottom-color "rgba(0,0,0, 0.08)"
               :backdrop-filter "blur(12px)" :-webkit-backdrop-filter "blur(12px)"
               :background "rgba(255,255,255, 0.83)"}

        ; partials
        toggler [:a.nav-item.nav-link.px-0 {:href (routing/path-for ::routes/about-page)}
                 icons/menu-icon]
        brand [:a.navbar-brand.m-0.font-black.text-xl {:href (routing/path-for ::routes/home)}
               (str " " "LEIHS" " ")]
        valid-until [:span (if @(subscribe [::cart/timed-out?])
                             {:style {:color "red"}}
                             {:class "text-color-info"})
                     (:valid-until cart-data)]
        cart [:a.nav-item.nav-link.px-0 {:href (routing/path-for ::routes/shopping-cart)}
              icons/shopping-cart-icon]]

    [:nav.ui-main-nav.navbar.navbar-light.text-xl.shadow-md.py-0.px-2.sticky-top.flex-nowrap.justify-content-between
     {:style style}

     [:div.navbar-nav.w-100
      [:div.mr-auto toggler]]

     [:div.mx-auto brand]

     [:div.navbar-nav.w-100
      [:div.ml-auto.d-flex.align-items-center
       [:div.mx-auto.px-2.text-xs valid-until]
       " " cart]]]))
