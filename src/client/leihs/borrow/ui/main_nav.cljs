(ns leihs.borrow.ui.main-nav
  (:require ["/borrow-ui" :as UI]
            ["date-fns" :as datefn]
            [clojure.string :refer [join replace split]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [leihs.borrow.client.routes :as routes]
            [leihs.borrow.components :as ui]
            [leihs.borrow.csrf :as csrf]
            [leihs.borrow.features.current-user.core :as current-user]
            [leihs.borrow.features.current-user.profile-switch :as profile-switch]
            [leihs.borrow.features.customer-orders.index :as customer-orders]
            [leihs.borrow.features.languages.core :as languages]
            [leihs.borrow.features.languages.language-switch :as language-switch]
            [leihs.borrow.features.shopping-cart.core :as cart]
            [leihs.borrow.lib.re-frame :refer [dispatch reg-event-db
                                               reg-event-fx reg-sub subscribe]]
            [leihs.borrow.lib.routing :as routing]
            [leihs.borrow.lib.translate :refer [set-default-translate-path t] :as translate]
            [reagent.core :as reagent]))

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

(reg-sub ::cart-valid-until
         :<- [::cart/data]
         (fn [cart _]
           (-> cart :valid-until datefn/parseISO)))

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

(defn- menu-link [href label is-selected]
  [:> UI/Components.Design.Menu.Link {:on-click #(dispatch [::set-current-menu nil]) :href href :isSelected is-selected} label])

(defn- borrow-menu-items [lg-screen?]
  (let [handler @(subscribe [:routing/current-handler])
        user-nav @(subscribe [::user-nav])
        documentation-url (:documentation-url user-nav)]
    (filter some?
            [{:href (routing/path-for ::routes/home)
              :label (t :borrow/catalog)
              :selected (some #{handler} [::routes/home ::routes/categories-show ::routes/models ::routes/models-show ::routes/templates-index ::routes/templates-show])}
             (when-not lg-screen?
               {:href (routing/path-for ::routes/shopping-cart)
                :label (t :borrow/shopping-cart)
                :selected (some #{handler} [::routes/shopping-cart])})
             {:href (routing/path-for ::routes/rentals-index)
              :label (reagent/as-element [:<> (t :user/rentals) " " [customer-orders/current-lendings-status-badge]])
              :selected (some #{handler} [::routes/rentals-index ::routes/rentals-show])}
             {:href (routing/path-for ::routes/models-favorites)
              :label (t :borrow/favorite-models)
              :selected (some #{handler} [::routes/models-favorites])}
             {:href (routing/path-for ::routes/inventory-pools-index)
              :label (t :borrow/pools)
              :selected (some #{handler} [::routes/inventory-pools-index ::routes/inventory-pools-show])}
             (when documentation-url
               {:href documentation-url
                :label (t :documentation)})])))

(defn- show-app-menu? [user-nav]
  (or (:admin-url user-nav)
      (:procure-url user-nav)
      (seq (:manage-nav-items user-nav))))

(defn- app-menu-data []
  (let [user-nav @(subscribe [::user-nav])]
    {:items
     (remove nil?
             (concat
              [#_{:href (routing/path-for ::routes/home) :label (t :borrow/section-title)} ; (don't show link to borrow itself)
               (when-let [admin-url (:admin-url user-nav)] {:href admin-url :label (t :app-switch/admin)})
               (when-let [procure-url (:procure-url user-nav)] {:href procure-url :label (t :app-switch/procure)})
               (when (seq (:manage-nav-items user-nav)) {:isSeparator true})]
              (for [{:keys [name url]} (:manage-nav-items user-nav)]
                {:href url :label name})))}))

(defn- app-switch-menu-items []
  (let [user-nav @(subscribe [::user-nav])]
    [:> UI/Components.Design.Menu {:id "app-menu"}
     [:> UI/Components.Design.Menu.Group
      #_[menu-link (routing/path-for ::routes/home) (t :borrow/section-title) true] ; (don't show link to borrow itself)
      (when-let [admin-url (:admin-url user-nav)] [menu-link admin-url (t :app-switch/admin)])
      (when-let [procure-url (:procure-url user-nav)] [menu-link procure-url (t :app-switch/procure)])
      (when (seq (:manage-nav-items user-nav))
        [:div.pt-3 (t :app-switch/manage)])
      (doall
       (for [{:keys [name url]} (:manage-nav-items user-nav)]
         [:<> {:key name}
          [menu-link url name]]))]]))

(defn- user-menu-data []
  (let [profile-errors @(subscribe [::profile-switch/errors])
        user @(subscribe [::user])
        delegations @(subscribe [::delegations])
        current-profile @(subscribe [::current-profile])
        changing-to-profile-id @(subscribe [::profile-switch/changing-to-id])
        languages @(subscribe [::languages/data])
        locale-to-use @(subscribe [::current-user/locale-to-use])]
    {:items [{:href (routing/path-for ::routes/current-user-show)
              :label (reagent/as-element [:<> [:> UI/Components.Design.Icons.UserIcon] (t :user/current-user)])}
             {:label (reagent/as-element [:<> [:> UI/Components.Design.Icons.PowerOffIcon] (t :user/logout)])
              :as "button" :type "submit" :form "sign-out-form" :onClick true}]
     :children (reagent/as-element
                [:<> {:key "other"}
                 ; logout form
                 [:form.visually-hidden {:id "sign-out-form" :action "/sign-out" :method "POST"} [csrf/token-field]]

                 ; profile select
                 (when (seq delegations)
                   [:div.mt-4
                    (when profile-errors
                      [ui/error-view profile-errors])
                    [:label.form-label {:for "profile-select"}
                     (t :!borrow.profile-menu/title)
                     (when changing-to-profile-id [:> UI/Components.Design.Spinner])]
                    [:select#profile-select.form-select
                     {:value (:id current-profile)
                      :onChange #(dispatch [::profile-switch/change-profile (-> % .-target .-value)])}
                     [:option {:value (:id user) :key (:id user)} (str #_(:short-name user) (:profile-name user))]
                     (doall
                      (for [delegation delegations]
                        [:option {:value (:id delegation) :key (:id delegation)} (str #_(:short-name delegation) (:profile-name delegation))]))]])

                 ; language select
                 (when (> (count languages) 1)
                   [:div.mt-4.mb-3
                    [:label.form-label {:for "language-select"} (t :language/section-title)]
                    [:select#language-select.form-select
                     {:value (name locale-to-use)
                      :onChange #(dispatch [::switch-language (-> % .-target .-value)])}
                     (doall
                      (for [language languages]
                        (let [locale (:locale language)]
                          [:<> {:key locale}
                           [:option {:value locale} (:name language)]])))]])])}))

(defn calc-remaining-minutes [cart-valid-until now]
  (let [cart-remaining-seconds (max 0 (datefn/differenceInSeconds cart-valid-until now))
        remaining-minutes (-> cart-remaining-seconds (/ 60) (js/Math.ceil))]
    (when (<= remaining-minutes 5)
      remaining-minutes)))

(defn top []
  (reagent/with-let [now (reagent/atom (js/Date.))
                     timer-fn  (js/setInterval #(reset! now (js/Date.)) 1000)]
    (let [cart-item-count @(subscribe [::cart-item-count])
          invalid-cart-item-count @(subscribe [::invalid-cart-item-count])
          cart-valid-until @(subscribe [::cart-valid-until])
          cart-remaining-minutes (reagent/reaction (calc-remaining-minutes cart-valid-until @now))
          menu-data @(subscribe [::menu-data])
          current-menu (:current-menu menu-data)
          current-profile @(subscribe [::current-profile])
          user-nav @(subscribe [::user-nav])]
      [:> UI/Components.Design.Topnav
       {:brandName "Leihs"
        :brandLinkProps {:href (routing/path-for ::routes/home)}
        :mainMenuIsOpen (= current-menu "main")
        :mainMenuLinkProps {:on-click #(dispatch [::set-current-menu (when-not (= current-menu "main") "main")])
                            :aria-controls "menu"}
        :mainMenuItems (borrow-menu-items true)
        :cartItemCount cart-item-count
        :invalidCartItemCount invalid-cart-item-count
        :cartItemLinkProps {:href (routing/path-for ::routes/shopping-cart)
                            :title (t :cart-item/menu-title)}
        :cartRemainingMinutes (or @cart-remaining-minutes js/undefined)
        :mobileUserMenuIsOpen (= current-menu "user")
        :userProfileShort (get-initials (:name current-profile))
        :mobileUserMenuLinkProps {:on-click #(dispatch [::set-current-menu (when-not (= current-menu "user") "user")])
                                  :aria-controls "user-menu"
                                  :title (t :user/menu-title)}
        :desktopUserMenuData (user-menu-data)
        :desktopUserMenuTriggerProps {:title (t :user/menu-title)}

        :appMenuData (when (show-app-menu? user-nav) (app-menu-data))
        :appMenuTriggerProps {:title (t :app-switch/menu-title)}}])
    (finally (js/clearInterval timer-fn))))

(defn main-nav []
  (let [user-loaded @(subscribe [::user-loaded])
        user-nav @(subscribe [::user-nav])]
    (when user-loaded
      [:> UI/Components.Design.Menu
       {:id "menu"}

       [:> UI/Components.Design.Menu.Group
        {:title (t :borrow/section-title)}
        (doall
         (for [[n menu-item] (map-indexed #(vector %1 %2) (borrow-menu-items false))]
           [:<> {:key n}
            [menu-link
             (:href menu-item)
             (:label menu-item)
             (:selected menu-item)]]))]

       (when (show-app-menu? user-nav)
         [:> UI/Components.Design.Menu.Group
          {:title (t :app-switch/section-title)}
          (app-switch-menu-items)])])))

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

     ; profile select
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

     ; language select
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
