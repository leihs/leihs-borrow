(ns leihs.borrow.ui.main-nav
  (:require
   [clojure.string :refer [join split replace]]
   [leihs.borrow.lib.re-frame :refer [subscribe
                                      dispatch
                                      reg-event-db
                                      reg-sub]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [leihs.borrow.lib.routing :as routing]
   [leihs.borrow.client.routes :as routes]
   [leihs.borrow.lib.translate :refer [t set-default-translate-path] :as translate]
   [leihs.borrow.csrf :as csrf]
   [leihs.borrow.lib.helpers :as h :refer [log]]
   [leihs.borrow.features.current-user.core :as current-user]
   [leihs.borrow.features.current-user.profile-switch :as profile-switch]
   [leihs.borrow.features.languages.core :as languages]
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

(reg-event-db ::set-menu-open
              (fn-traced [db [_ flag]]
                (update-in db [:ls ::data] merge {:is-menu-open? flag :is-profile-menu-open? false})))

(reg-event-db ::set-profile-menu-open
              (fn-traced [db [_ flag]]
                (update-in db [:ls ::data] merge {:is-menu-open? false :is-profile-menu-open? flag})))

(defn get-initials [name]
  (let [words (split name #"[ \-]+")]
    (->> words
         (map #(replace % #"[^a-zA-Z0-9]" ""))
         (filter #(-> % count (> 0)))
         (map #(-> % first))
         (take 3)
         (join ""))))

(defn add-initials [profile]
  (assoc profile :short-name (get-initials (:name profile))))

(reg-sub ::user
         :<- [::current-user/user-data]
         (fn [{:keys [id name delegations]} _]
           {:id id
            :name (str name (when (seq delegations) (t :!borrow.phrases.user-or-delegation-personal-postfix)))
            :short-name (get-initials name)}))

(reg-sub ::delegations
         :<- [::current-user/delegations]
         (fn [delegations _]
           (map add-initials delegations)))

(reg-sub ::current-profile
         :<- [::current-user/current-profile]
         (fn [current-profile _] current-profile))


(defn menu-link [href label]
  [:> UI/Components.Design.Menu.Link {:on-click #(dispatch [::set-menu-open false]) :href href} label])

(defn navbar-menu []
  (let [cart-item-count @(subscribe [::cart-item-count])
        invalid-cart-item-count @(subscribe [::invalid-cart-item-count])
        menu-data @(subscribe [::menu-data])
        is-menu-open? (:is-menu-open? menu-data)
        is-profile-menu-open? (:is-profile-menu-open? menu-data)
        user @(subscribe [::user])
        delegations @(subscribe [::delegations])
        current-profile @(subscribe [::current-profile])
        changing-to-profile-id @(subscribe [::profile-switch/changing-to-id])
        profile-errors @(subscribe [::profile-switch/errors])
        user-nav @(subscribe [::user-nav])
        legacy-url (:legacy-url user-nav)
        documentation-url (:documentation-url user-nav)
        support-url nil
        languages @(subscribe [::languages/data])
        locale-to-use @(subscribe [::translate/locale-to-use])]

    ; TODO: provide support-url in user-nav
    ; TODO: provide languages, implement language switch

    [:> UI/Components.Design.Navbar.MenuWrapper
     {:menuIsOpen (or is-menu-open? is-profile-menu-open?)}


     [:> UI/Components.Design.Navbar
      {:brandName "Leihs"
       :brandItem {:href (routing/path-for ::routes/home)}
       :menuIsOpen is-menu-open?
       :menuItem {:on-click #(dispatch [::set-menu-open (not is-menu-open?)])
                  :aria-controls "menu"
                  :aria-expanded is-menu-open?
                  :role :button}
       :cartItemCount cart-item-count
       :invalidCartItemCount invalid-cart-item-count
       :cartItem {:href (routing/path-for ::routes/shopping-cart)}
       :profileButtonProps (when (seq delegations)
                             {:profile-short (get-initials (:name current-profile))
                              :on-click #(dispatch [::set-profile-menu-open (not is-profile-menu-open?)])
                              :is-open is-profile-menu-open?
                              :aria-controls "profile-menu"
                              :aria-expanded is-profile-menu-open?})}]


     [:> UI/Components.Design.Menu
      {:show is-menu-open? :id "menu"}

      [:> UI/Components.Design.Menu.Group {:title (t :borrow/section-title)}
       [menu-link (routing/path-for ::routes/home) (t :borrow/catalog)]
       [menu-link (routing/path-for ::routes/shopping-cart) (t :borrow/shopping-cart)]
       [menu-link (routing/path-for ::routes/rentals-index) (t :user/rentals)]
       [menu-link (routing/path-for ::routes/models-favorites) (t :borrow/favorite-models)]
       [menu-link (routing/path-for ::routes/inventory-pools-index) (t :borrow/pools)]]

      [:> UI/Components.Design.Menu.Group {:title (t :user/section-title)}
       [menu-link (routing/path-for ::routes/current-user-show) (t :user/current-user)]
       [:> UI/Components.Design.Menu.Button {:type "submit" :form "sign-out-form"} (t :user/logout)]
       [:form.visually-hidden {:id "sign-out-form" :action "/sign-out" :method "POST"} [csrf/token-field]]]

      ; (documentation and support are in the "Leihs" group, keeping number of groups low)
      (comment [:> UI/Components.Design.Menu.Group {:title (t :help/section-title)}])

      (when (> (count languages) 1)
        [:> UI/Components.Design.Menu.Group {:title (t :language/section-title)}
         (doall
          (for [language languages]
            (let [locale (:locale language)]
              [:<> {:key locale}
               [:> UI/Components.Design.Menu.Button
                {:isSelected (= (keyword locale) locale-to-use)
                 :type "button"
                 :value locale
                 :on-click #(dispatch [::languages/switch (-> % .-target .-value)])}
                (:name language)]])))])

      [:> UI/Components.Design.Menu.Group {:title "Leihs"}
       [menu-link legacy-url (t :desktop-version)]
       (when documentation-url [menu-link documentation-url (t :help/documentation)])
       (when support-url [menu-link support-url (t :help/support)])]]
     (when is-profile-menu-open?
       [:<>
        (when profile-errors
          [ui/error-view profile-errors])
        [:> UI/Components.Design.ProfileMenu
         {:id "profile-menu"
          :user user
          :delegations (h/camel-case-keys delegations)
          :currentProfile current-profile
          :onSelectProfile #(dispatch [::profile-switch/change-profile (-> % .-id)])
          :changingToProfileId changing-to-profile-id
          :onDismiss #(dispatch [::set-profile-menu-open false])
          :txt {:title (t :!borrow.profile-menu/title)}}]])]))
