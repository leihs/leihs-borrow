(ns leihs.borrow.lib.translate
  (:require-macros [leihs.borrow.lib.translate])
  (:require [tongue.core :as tongue]
            [re-frame.db :as db]
            [leihs.borrow.features.current-user.core :as current-user]
            [clojure.string :as string]))

(def ^:dynamic *default-path* "Default path to use for locating a key.")

(def dicts
  "Read in from an external source"
  {:en-GB {:borrow {:all "All"
                    :about-page {:title "About"
                                 :navigation-menu "Navigation Menu"}
                    :categories {:title "Categories"}
                    :current-user {:title "Current User"}
                    :customer-orders {:title "Orders"
                                      :active-orders "Active Orders"
                                      :rejected-orders "Rejected Orders"
                                      :approved-orders "Approved Orders"}
                    :delegations {:title "Delegations"
                                  :responsible "Responsible"
                                  :members "Members"
                                  :loading "Loading delegation"}
                    :favorite-models {:title "Favorites"}
                    :filter {:search "Search"
                             :for "For"
                             :time-span "Time Span"
                             :show-only-available "Show available only"
                             :from "From"
                             :until "Until"
                             :quantity "Quantity"
                             :get-results "Get Results"
                             :clear "Clear"}
                    :home-page {:title "Home"}
                    :logout "Logout"
                    :model-show {:loading "Loading model"
                                 :compatibles "Compatible Models"}
                    :pagination {:load-more "Load more"
                                 :nothing-found "Nothing found"}
                    :pools {:title "Pools"
                            :access-suspended "Your access is suspended"
                            :no-reservable-models "No reservable models"
                            :maximum-reservation-pre "Maximum reservation of "
                            :maximum-reservation-post " days"}
                    :shopping-cart {:title "Cart"
                                    :edit "Edit"
                                    :line {:total "Total"
                                           :total-models "Model(s)"
                                           :total-items "Item(s)"
                                           :from "from"
                                           :first-pickup "First pickup"
                                           :last-return "last return"}
                                    :confirm-order "Confirm order"
                                    :delete-order "Delete order"
                                    :order-overview "Order Overview"
                                    :empty-order "Your order is empty"
                                    :borrow-items "Borrow Items"
                                    :order-name "Name Your Order"}
                    }}
   :de-CH {:borrow {:all "Alle"
                    :about-page {:title "Übersicht"
                                 :navigation-menu "Navigationsmenü"}
                    :categories {:title "Kategorien"}
                    :current-user {:title "Aktueller Benutzer"}
                    :customer-orders {:title "Bestellungen"
                                      :active-orders "Aktive Bestellungen"
                                      :rejected-orders "Abgelehnte Bestellungen"
                                      :approved-orders "Genehmigte Bestellungen"}
                    :delegations {:title "Delegationen"
                                  :responsible "Verantwortliche Person"
                                  :members "Mitglieder"
                                  :loading "Delegation wird geladen"}
                    :favorite-models {:title "Favoriten"}
                    :filter {:search "Suche"
                             :for "Für"
                             :time-span "Zeitraum"
                             :show-only-available "Nur Verfügbare anzeigen"
                             :from "Von"
                             :until "Bis"
                             :quantity "Anzahl"
                             :get-results "Resultate anzeigen"
                             :clear "Löschen"}
                    :home-page {:title "Home"}
                    :logout "Abmelden"
                    :model-show {:loading "Modell wird geladen"
                                 :compatibles "Ergänzende Modelle"}
                    :pagination {:load-more "Mehr laden"
                                 :nothing-found "Nichts gefunden"}
                    :pools {:title "Geräteparks"
                            :access-suspended "Zugang gesperrt"
                            :no-reservable-models "Keine reservierbaren Modelle"
                            :maximum-reservation-pre "Maximale Reservierung von "
                            :maximum-reservation-post " Tagen"}
                    :shopping-cart {:title "Warenkorb"
                                    :edit "Editieren"
                                    :line {:total "Total"
                                           :total-models "Modell(e)"
                                           :total-items "Gegenstand/Gegenstände"
                                           :from "aus"
                                           :first-pickup "Erste Abholung"
                                           :last-return "letzte Rückgabe"}
                                    :confirm-order "Bestellung bestätigen"
                                    :delete-order "Bestellung löschen"
                                    :order-overview "Bestellübersicht"
                                    :empty-order "Deine Bestellung ist leer"
                                    :borrow-items "Gegenstände ausleihen"
                                    :order-name "Benenne deine Bestellung"}
                    }}})

(def dicts-extensions
  {:tongue/fallback :en-GB})

(def translate
  (-> dicts 
      (merge dicts-extensions)
      tongue/build-translate))

(defn drop-first-char [s]
  (->> s (drop 1) string/join))

(defn fully-qualify-key [pre k]
  (let [k* (-> k str drop-first-char)
        pre* (-> pre str drop-first-char)]
    (keyword
      (condp re-matches k*
        #"!.*" (drop-first-char k*)
        #".*/.*" (str pre* "." k*)
        (str pre* "/" k*)))))

(defn locale-to-use []
  (or (current-user/locale-name-to-use @db/app-db)
      :en-GB))

(defn apply-default-path [[a1 & as]]
  (cons (if *default-path*
          (fully-qualify-key *default-path* a1)
          a1)
        as))

(defn t-base [& args]
  (apply translate
         (locale-to-use)
         (apply-default-path args)))

(comment
  ((resolve 'drop-first-char) "Foo")
  (fully-qualify-key :borrow.about-page :title)
  (t :borrow/all))
