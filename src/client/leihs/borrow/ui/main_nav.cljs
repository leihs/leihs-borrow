(ns leihs.borrow.ui.main-nav
  (:require
   [leihs.borrow.lib.re-frame :refer [subscribe
                                      dispatch
                                      reg-event-db
                                      reg-sub]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path]]
   [leihs.borrow.csrf :as csrf]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.shopping-cart.core :as cart]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.menu)

(reg-sub ::user-name
         :<- [::current-user/user-data]
         (fn [user-data]
           (-> user-data :name)))

(reg-sub ::cart-item-count
         :<- [::cart/data]
         (fn [cart _]
           (let [pending-count (:pending-count cart)]
             (cond-> (-> cart :reservations count)
               pending-count
               (+ pending-count)))))

(reg-sub ::menu-data
         (fn [db] (get-in db [:ls ::data])))

(reg-event-db ::set-menu-open
              (fn-traced [db [_ flag]]
                (update-in db [:ls ::data] assoc :is-menu-open? flag)))

(defn navbar-menu []
  (let [cart-item-count @(subscribe [::cart-item-count])
        menu-data @(subscribe [::menu-data])
        is-menu-open? (:is-menu-open? menu-data)
        user-name @(subscribe [::user-name])
        documentation-url nil
        support-url nil
        languages [{:id "de" :title "Deutsch"}]]
    ; TODO: provide documentation-url, support-url
    ; TODO: provide languages, implement language switch
    [:<>

     [:> UI/Components.Design.Navbar
      {:brandName "Leihs"
       :brandItem {:href (routing/path-for ::routes/home)}
       :menuIsOpen is-menu-open?
       :menuItem {:on-click #(dispatch [::set-menu-open (not is-menu-open?)])
                  :aria-controls "menu"
                  :aria-expanded is-menu-open?
                  :role :button}
       :cartItemCount cart-item-count
       :cartItem {:href (routing/path-for ::routes/shopping-cart)}}]

     [:> UI/Components.Design.Collapse {:in is-menu-open?}
      [:div {:id "menu"}
       [:> UI/Components.Design.Menu

        [:> UI/Components.Design.Menu.Group {:title (t :borrow/section-title)}
         [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/home)} (t :borrow/catalog)]
         [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/shopping-cart)} (t :borrow/shopping-cart)]]

        [:> UI/Components.Design.Menu.Group {:title user-name}
         [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/rentals-index)} (t :user/rentals)]
         [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/models-favorites)} (t :user/favorite-models)]
         [:> UI/Components.Design.Menu.Link {:href (routing/path-for ::routes/current-user-show)} (t :user/current-user)]
         [:> UI/Components.Design.Menu.Button {:type "submit" :form "sign-out-form"} (t :user/logout)]
         [:form.visually-hidden {:id "sign-out-form" :action "/sign-out" :method "POST"} [csrf/token-field]]]

        (when (or documentation-url support-url)
          [:> UI/Components.Design.Menu.Group {:title (t :help/section-title)}
           (when documentation-url [:> UI/Components.Design.Menu.Link {:href documentation-url} (t :help/documentation)])
           (when support-url [:> UI/Components.Design.Menu.Link {:href support-url} (t :help/support)])])

        (when (> (count languages) 1)
          [:> UI/Components.Design.Menu.Group {:title (t :language/section-title)}
           (doall
            (for [language languages]
              [:> UI/Components.Design.Menu.Link {:on-click #(dispatch [::TODO-switch-language (:id language)])} (:title language)]))])]]]]))
