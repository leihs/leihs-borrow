(ns leihs.borrow.ui.main-nav
  (:require
   #_[re-frame.core :as rf]
   #_[shadow.resource :as rc]
   #_["date-fns" :as datefn]

   [leihs.borrow.lib.re-frame :refer [subscribe dispatch]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]

   [leihs.borrow.features.shopping-cart.core :as cart]

   ["/leihs-ui-client-side-external-react" :as UI]))

(defn navbar []
  (let [cart-data @(subscribe [:leihs.borrow.features.shopping-cart.core/data])
        routing @(subscribe [:routing/routing])
        is-fake-menu-open? (= (get-in routing [:bidi-match :handler]) ::routes/about-page)
        ;cart-timed-out @(subscribe [::cart/timed-out?])
        cart-item-count (count (:reservations cart-data))]

    [:> UI/Components.Design.Navbar
     {:brandName "Leihs"
      :brandItem {:href (routing/path-for ::routes/home)}
      :menuIsOpen is-fake-menu-open?
      :menuItem {:href (when-not is-fake-menu-open? (routing/path-for ::routes/about-page))
                 :onClick (when is-fake-menu-open? #(dispatch [:routing/navigate-back]))}
      :cartItemCount cart-item-count
      ;:cartTimedOut cart-timed-out
      :cartItem {:href (routing/path-for ::routes/shopping-cart)}}]))

