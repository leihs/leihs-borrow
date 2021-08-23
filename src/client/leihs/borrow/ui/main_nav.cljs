(ns leihs.borrow.ui.main-nav
  (:require
   #_[re-frame.core :as rf]
   #_[shadow.resource :as rc]
   #_["date-fns" :as datefn]

   [leihs.borrow.lib.re-frame :refer [subscribe dispatch reg-sub]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]

   [leihs.borrow.features.shopping-cart.core :as cart]

   ["/leihs-ui-client-side-external-react" :as UI]))

(reg-sub ::cart-item-count
         :<- [::cart/data]
         (fn [cart _]
           (let [pending-count (:pending-count cart)]
             (cond-> (-> cart :reservations count)
               pending-count
               (+ pending-count)))))

(defn navbar []
  (let [routing @(subscribe [:routing/routing])
        is-fake-menu-open? (= (get-in routing [:bidi-match :handler]) ::routes/about-page)
        ;cart-timed-out @(subscribe [::cart/timed-out?])
        cart-item-count @(subscribe [::cart-item-count])]

    [:> UI/Components.Design.Navbar
     {:brandName "Leihs"
      :brandItem {:href (routing/path-for ::routes/home)}
      :menuIsOpen is-fake-menu-open?
      :menuItem {:href (when-not is-fake-menu-open? (routing/path-for ::routes/about-page))
                 :onClick (when is-fake-menu-open? #(dispatch [:routing/navigate-back]))}
      :cartItemCount cart-item-count
      ;:cartTimedOut cart-timed-out
      :cartItem {:href (routing/path-for ::routes/shopping-cart)}}]))

