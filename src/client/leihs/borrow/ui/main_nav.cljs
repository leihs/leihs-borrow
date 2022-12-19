(ns leihs.borrow.ui.main-nav
  (:require
   [clojure.string :refer [join split replace]]
   [leihs.borrow.lib.re-frame :refer [subscribe
                                      dispatch
                                      reg-event-db
                                      reg-sub
                                      reg-event-fx]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path] :as translate]
   [leihs.borrow.csrf :as csrf]
   [leihs.borrow.lib.helpers :as h :refer [log]]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.current-user.profile-switch :as profile-switch]
   [leihs.borrow.features.languages.core :as languages]
   [leihs.borrow.features.languages.language-switch :as language-switch]
   [leihs.borrow.features.shopping-cart.core :as cart]
   [leihs.borrow.components :as ui]
   ["/leihs-ui-client-side-external-react" :as UI]))

(set-default-translate-path :borrow.menu)

(reg-sub ::user-nav
         :<- [::current-user/user-nav]
         identity)

(reg-sub ::cart-item-count
         :<- [::cart/data]
         (fn [cart _]
           (let [pending-count (:pending-count cart)]
             (cond-> (-> cart :reservations count)
               pending-count
               (+ pending-count)))))

(reg-sub ::invalid-cart-item-count
         :<- [::cart/data]
         (fn [cart _]
           (-> cart :invalid-reservation-ids count)))

(reg-sub ::menu-data
         (fn [db] (get-in db [:ls ::data])))

(reg-event-db ::set-current-menu
              (fn-traced [db [_ menu-id]]
                (update-in db [:ls ::data] merge {:current-menu menu-id})))

(reg-event-fx ::switch-language
              (fn-traced [_ [_ lang]]
                {:dispatch-n (list [::language-switch/switch lang]
                                   [::set-current-menu nil])}))

(defn get-initials [name]
  (let [words (split name #"[ \-]+")]
    (->> words
         (map #(replace % #"[^a-zA-Z0-9]" ""))
         (filter #(-> % count (> 0)))
         (map #(-> % first))
         (take 3)
         (join ""))))

(reg-sub ::user-loaded
         :<- [::current-user/user-data]
         (fn [user-data _]
           (boolean user-data)))

(reg-sub ::user
         :<- [::current-user/user-data]
         (fn [{:keys [id name delegations]} _]
           {:id id
            :name name
            :profile-name (str name (when (seq delegations) (t :!borrow.phrases.user-or-delegation-personal-postfix)))
            :short-name (get-initials name)}))

(reg-sub ::delegations
         :<- [::current-user/delegations]
         (fn [delegations _]
           (map (fn [{:keys [id name]}]
                  {:id id
                   :name name
                   :profile-name name
                   :short-name (get-initials name)})
                delegations)))

(reg-sub ::current-profile
         :<- [::current-user/current-profile]
         (fn [current-profile _] current-profile))


(defn menu-link [href label is-selected]
  [:> UI/Components.Design.Menu.Link {:on-click #(dispatch [::set-current-menu nil]) :href href :isSelected is-selected} label])

(defn top []
  (let [cart-item-count @(subscribe [::cart-item-count])
        invalid-cart-item-count @(subscribe [::invalid-cart-item-count])
        menu-data @(subscribe [::menu-data])
        current-menu (:current-menu menu-data)
        current-profile @(subscribe [::current-profile])]

    [:> UI/Components.Design.Navbar
     {:brandName "Leihs"
      :brandLinkProps {:href (routing/path-for ::routes/home)}
      :mainMenuIsOpen (= current-menu "main")
      :mainMenuLinkProps {:on-click #(dispatch [::set-current-menu (when-not (= current-menu "main") "main")])
                          :aria-controls "menu"}
      :cartItemCount cart-item-count
      :invalidCartItemCount invalid-cart-item-count
      :cartItemLinkProps {:href (routing/path-for ::routes/shopping-cart)}
      :userMenuIsOpen (= current-menu "user")
      :userProfileShort (get-initials (:name current-profile))
      :userMenuLinkProps {:on-click #(dispatch [::set-current-menu (when-not (= current-menu "user") "user")])
                          :aria-controls "user-menu"}
      :appMenuIsOpen (= current-menu "app")
      :appMenuLinkLabel (t :app-switch/button-label)
      :appMenuLinkProps {:on-click #(dispatch [::set-current-menu (when-not (= current-menu "app") "app")])
                         :aria-controls "app-menu"}}]))

(defn- borrow-menu-items []
  (let [handler @(subscribe [:routing/current-handler])]
    [:<>
     [menu-link (routing/path-for ::routes/home) (t :borrow/catalog)
      (some #{handler} [::routes/home ::routes/categories-show ::routes/models ::routes/models-show ::routes/templates-index ::routes/templates-show])]
     [menu-link (routing/path-for ::routes/shopping-cart) (t :borrow/shopping-cart)
      (some #{handler} [::routes/shopping-cart])]
     [menu-link (routing/path-for ::routes/rentals-index) (t :user/rentals)
      (some #{handler} [::routes/rentals-index ::routes/rentals-show])]
     [menu-link (routing/path-for ::routes/models-favorites) (t :borrow/favorite-models)
      (some #{handler} [::routes/models-favorites])]
     [menu-link (routing/path-for ::routes/inventory-pools-index) (t :borrow/pools)
      (some #{handler} [::routes/inventory-pools-index ::routes/inventory-pools-show])]]))

(defn- app-switch-menu-items []
  (let [user-nav @(subscribe [::user-nav])
        legacy-url (:legacy-url user-nav)]
    [:> UI/Components.Design.Menu {:id "app-menu"}
     [:> UI/Components.Design.Menu.Group
      [menu-link (routing/path-for ::routes/home) (t :borrow/section-title) true]
      [menu-link legacy-url (t :desktop-version)]
      (when-let [admin-url (:admin-url user-nav)] [menu-link admin-url (t :app-switch/admin)])
      (when-let [procure-url (:procure-url user-nav)] [menu-link procure-url (t :app-switch/procure)])
      (when (seq (:manage-nav-items user-nav))
        [:div.pt-2 (t :app-switch/manage)])
      (doall
       (for [{:keys [name url]} (:manage-nav-items user-nav)]
         [:<> {:key name}
          [menu-link url name]]))]]))

(defn side []
  (let [user-loaded @(subscribe [::user-loaded])
        user-nav @(subscribe [::user-nav])
        documentation-url (:documentation-url user-nav)]
    (when user-loaded
      [:> UI/Components.Design.Menu
       {:id "menu"}

       [:> UI/Components.Design.Menu.Group
        {:class "d-lg-none" :title (t :borrow/section-title)}
        (borrow-menu-items)]
       [:> UI/Components.Design.Menu.Group
        {:class "d-none d-lg-block" :title (t :borrow/section-title)}
        (borrow-menu-items)]

       [:> UI/Components.Design.Menu.Group
        {:title (t :app-switch/section-title) :class "d-md-none"}
        (app-switch-menu-items)]

       (when documentation-url
         [:> UI/Components.Design.Menu.Group
          [menu-link documentation-url (t :documentation)]])])))

(defn user-profile-nav []
  (let [handler @(subscribe [:routing/current-handler])
        profile-errors @(subscribe [::profile-switch/errors])
        user @(subscribe [::user])
        delegations @(subscribe [::delegations])
        current-profile @(subscribe [::current-profile])
        changing-to-profile-id @(subscribe [::profile-switch/changing-to-id])
        languages @(subscribe [::languages/data])
        locale-to-use @(subscribe [::current-user/locale-to-use])]
    [:> UI/Components.Design.Menu {:id "user-menu"}

     [:> UI/Components.Design.Menu.Group {:title (:name user)}
      [menu-link (routing/path-for ::routes/current-user-show) (t :user/current-user)
       (some #{handler} [::routes/current-user-show])]
      [:> UI/Components.Design.Menu.Button {:type "submit" :form "sign-out-form"} (t :user/logout)]
      [:form.visually-hidden {:id "sign-out-form" :action "/sign-out" :method "POST"} [csrf/token-field]]]

     (when (seq delegations)
       [:> UI/Components.Design.Menu.Group {:title (t :!borrow.profile-menu/title)}
        (when profile-errors
          [ui/error-view profile-errors])

        [:> UI/Components.Design.ProfileMenuButton
         {:profile user
          :isSelected (= (:id user) (:id current-profile))
          :isLoading (= (:id user) changing-to-profile-id)
          :onClick #(dispatch [::profile-switch/change-profile (:id user)])}]

        (doall
         (for [delegation delegations]
           [:> UI/Components.Design.ProfileMenuButton
            {:profile delegation
             :isSelected (= (:id delegation) (:id current-profile))
             :isLoading (= (:id delegation) changing-to-profile-id)
             :onClick #(dispatch [::profile-switch/change-profile (:id delegation)])
             :key (:id delegation)}]))])

     (when (> (count languages) 1)
       [:> UI/Components.Design.Menu.Group {:title (t :language/section-title)}
        (doall
         (for [language languages]
           (let [locale (:locale language)
                 selected? (= (keyword locale) locale-to-use)]
             [:<> {:key locale}
              [:> UI/Components.Design.Menu.Button
               {:isSelected selected?
                :type "button"
                :value locale
                :on-click (when-not selected? #(dispatch [::switch-language (-> % .-target .-value)]))}
               (:name language)]])))])]))

(defn app-nav []
  [:> UI/Components.Design.Menu {:id "app-menu"}
   [:> UI/Components.Design.Menu.Group {:title (t :app-switch/section-title)}
    (app-switch-menu-items)]])