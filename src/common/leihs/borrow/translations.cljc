(ns leihs.borrow.translations)

(def dict
  {:borrow

  ; --- GENERAL TERMS ---
  ; prefer using those over something like `example-page-section-title-contracts`
  ; but NOT for interpolation / string concating (that assumes to much about the languages)
  ; TODO: implement fallback keys so we can have both: `(t [:example-page-section-title-contracts :terms.contracts])
   {:terms {:contract {:de-CH "Vertrag" :en-GB "Contract"}
            :contracts {:de-CH "Verträge" :en-GB "Contracts"}

            :delegation {:de-CH "Delegation" :en-GB "Delegation"}
            :delegations {:de-CH "Delegationen" :en-GB "Delegations"}

            :weekdays {:monday {:de-CH "Montag" :en-GB "Monday"}
                       :tuesday {:de-CH "Dienstag" :en-GB "Tuesday"}
                       :wednesday {:de-CH "Mittwoch" :en-GB "Wednesday"}
                       :thursday {:de-CH "Donnerstag" :en-GB "Thursday"}
                       :friday {:de-CH "Freitag" :en-GB "Friday"}
                       :saturday {:de-CH "Samstag" :en-GB "Saturday"}
                       :sunday {:de-CH "Sonntag" :en-GB "Sunday"}}}

  ; --- GENERAL PHRASES ---
  ; use them for consistency between different parts of the app
  ; but NOT for interpolation / string concating (that assumes to much about the languages)
    :phrases
    {:items-count
     {:de-CH "{itemCount, plural,
                =0 {Keine Gegenstände}
                =1 {# Gegenstand}
                other {# Gegenstände}
                }"
      :en-GB "{itemCount, plural,
                =0 {No items}
                one {# item}
                other {# items}
                }"}

     :contract-display-name
     {:de-CH "Vertrag {ID} mit {poolName} vom {date, date, narrow}"
      :en-GB "Contract {ID} with {poolName} from {date, date, narrow}"}

     :user-or-delegation-personal-postfix
     {:de-CH " (persönlich)" :en-GB " (personal)"}}

    :all {:en-GB "All"
          :de-CH "Alle"}

  ; --- PER COMPONENT TRANSLATIONS (reused between some features) ---

    :order-panel
    {:label {:quantity {:en-GB "Quantity" :de-CH "Anzahl"}
             :minus {:en-GB "Minus" :de-CH "Minus"}
             :plus {:en-GB "Plus" :de-CH "Plus"}
             :pool {:en-GB "Inventory pool" :de-CH "Inventarpark"}
             :pool-max-amount {:en-GB "{pool} (max. {amount, number})" :de-CH "{pool} (max. {amount, number})"}
             :pool-max-amount-info {:en-GB  "Maximum available amount: {amount, number}" :de-CH "Maximal verfügbarer Bestand: {amount, number}"}
             :user-delegation {:en-GB "Order for" :de-CH "Bestellung für"}
             :timespan {:en-GB "Time span" :de-CH "Zeitraum"}
             :from {:en-GB "From" :de-CH "Von"}
             :until {:en-GB "Until" :de-CH "Bis"}
             :undefined {:en-GB "undefined" :de-CH "Unbestimmt"}
             :show-day-quants {:en-GB "Show availability in calendar" :de-CH "Verfügbarkeit im Kalender anzeigen"}}
     :validate {:missing-quantity
                {:de-CH "Verfügbarkeit kann nicht geprüft werden, da die Anzahl fehlt"
                 :en-GB "Availability can not be checked because the quantity is missing"}

                :invalid-start-date
                {:de-CH "Ungültiges Beginndatum"
                 :en-GB "Invalid start date"}
                :invalid-end-date
                {:de-CH "Ungültiges Enddatum"
                 :en-GB "Invalid end date"}
                :start-after-end
                {:de-CH "Enddatum muss nach Beginndatum sein"
                 :en-GB "End date must be after start date"}
                :end-date-too-late
                {:de-CH "Datum darf nicht nach {maxDate, date, small} sein"
                 :en-GB "Date must not be after {maxDate, date, small}"}

                :start-date-in-past
                {:de-CH "Abholdatum liegt in der Vergangenheit"
                 :en-GB "Pickup date is in the past"}
                :start-date-not-before
                {:de-CH "{days, plural, =1 {Abholung frühestens morgen} other {Abholung frühestens heute in # Arbeitstagen}}"
                 :en-GB "{days, plural, =1 {Earliest pickup date is tomorrow} other {Earliest pickup date in # working days from now}}"}

                :quantity-to-large-at-day
                {:de-CH
                 "Gegenstand ist am {startDate, date, small} nicht in der gewünschten Menge verfügbar"
                 :en-GB "Item is not available in the requested quantity on {startDate, date, small}."}
                :quantity-to-large-in-range
                {:de-CH "Gegenstand ist in diesem Zeitraum nicht in der gewünschten Menge verfügbar"
                 :en-GB "Item is not available in the desired quantity during this period"}

                :pool-closed-at-start-date
                {:de-CH "Abholung am {startDate, date, small} nicht möglich"
                 :en-GB "Pickup not possible on {startDate, date, small}"}
                :pool-closed-at-end-date
                {:de-CH "Rückgabe am {endDate, date, small} nicht möglich"
                 :en-GB "Return not possible on {endDate, date, small}"}
                :pool-closed-at-start-and-end-date
                {:de-CH "Abholung/Rückgabe am {startDate, date, small} nicht möglich"
                 :en-GB "Pickup/return not possible on {startDate, date, small}"}
                :pool-closed-max-visits
                {:de-CH " (maximale Besucherzahl erreicht)"
                 :en-GB " (maximum visitor capacity reached)"}
                :closed-on-day-of-week
                {:de-CH "{dayName} geschlossen"
                 :en-GB "closed on {dayName}"}

                :maximum-reservation-duration
                {:de-CH "Maximale Reservationsdauer ist beschränkt auf {days} Tage"
                 :en-GB "Maximum reservation duration is restricted to {days} days"}

                :no-pool-access
                {:de-CH "Keine Berechtigung für diesen Inventarpark"
                 :en-GB "No access to this inventory pool"}
                :pool-suspension
                {:de-CH "Benutzer für diesen Inventarpark gesperrt"
                 :en-GB "User suspended for this inventory pool"}
                :item-not-available-in-pool
                {:de-CH "Gegenstand in diesem Inventarpark nicht verfügbar"
                 :en-GB "Item not available in this inventory pool"}
                :unknown-pool
                {:de-CH "Unbekannter Inventarpark"
                 :en-GB "Unknown inventory pool"}}}

  ; --- PER FEATURE TRANSLATIONS ---

    :catalog {:title {:en-GB "Catalog"
                      :de-CH "Katalog"}
              :categories {:en-GB "Categories"
                           :de-CH "Kategorien"}
              :no-reservable-items {:en-GB "No reservable items found"
                                    :de-CH "Keine reservierbaren Gegenstände gefunden"}
              :check-available-pools {:en-GB "Check available inventory pools"
                                      :de-CH "Verfügbare Inventarparks prüfen"}
              :templates {:en-GB "Templates"
                          :de-CH "Vorlagen"}}
    :categories {:sub-categories {:en-GB "Sub-categories"
                                  :de-CH "Unterkategorien"}
                 :items {:en-GB "Items"
                         :de-CH "Gegenstände"}
                 :category-root {:en-GB "All Categories"
                                 :de-CH "Alle Kategorien"}}
    :current-user {:title {:en-GB "User Account"
                           :de-CH "Benutzerkonto"}
                   :user-data {:en-GB "User data"
                               :de-CH "Nutzerdaten"}
                   :no-contracts {:en-GB "None"
                                  :de-CH "Noch keine"}}

    :debug-page {:title {:en-GB "Debugging"
                         :de-CH "Debugging"}}

    :errors {:error {:en-GB "Error"
                     :de-CH "Fehler"}
             :processing-error {:en-GB "Error when processing this action"
                                :de-CH "Fehler beim Ausführen dieser Aktion"}
             :render-error {:en-GB "Error displaying this content"
                            :de-CH "Fehler bei der Anzeige"}
             :loading-error {:en-GB "Error loading this content"
                             :de-CH "Fehler beim Laden"}
             ; 401
             :unauthorized {:en-GB "User not logged in"
                            :de-CH "Benutzer ist nicht angemeldet"}
             ; 403
             :forbidden {:en-GB "This Resource is not available for the current profile"
                         :de-CH "Diese Ressource ist für das aktuelle Profil nicht verfügbar"}
             :reload {:en-GB "Reload current page"
                      :de-CH "Seite neu laden"}
             :go-to-start {:en-GB "Go to start page"
                           :de-CH "Zur Startseite"}
             :go-to-login {:en-GB "Go to login"
                           :de-CH "Zum Login"}}

    :favorite-models {:title {:en-GB "Favorites"
                              :de-CH "Favoriten"}
                      :items {:en-GB "Items"
                              :de-CH "Gegenstände"}
                      :no-favorites {:en-GB "No favorites added yet"
                                     :de-CH "Noch keine Favoriten hinzugefügt"}
                      :go-to-catalog {:en-GB "Go to catalog"
                                      :de-CH "Hier geht's zum Katalog"}}
    :filter {:search-input-placeholder {:de-CH "Suchbegriff",
                                        :en-GB "Search term"}
             :search-button-label {:de-CH "Suchen",
                                   :en-GB "Search"}

             :filter {:en-GB "Filter"
                      :de-CH "Filter"}

             :pool-select-label {:en-GB "Inventory pools" :de-CH "Inventarparks"}
             :all-pools-option-label {:en-GB "All inventory pools" :de-CH "Alle Inventarparks"}
             :invalid-pool-option-label {:en-GB "Invalid selection" :de-CH "Ungültige Auswahl"}
             :invalid-pool-message {:en-GB "The pre-selected inventory pool is not available for the current profile"
                                    :de-CH "Der vorher gewählte Inventarpark ist für das aktuelle Profil nicht verfügbar"}
             :pool-suspended-message {:de-CH "Benutzer für diesen Inventarpark gesperrt"
                                      :en-GB "User suspended for this inventory pool"}

             :availability-button-label {:en-GB "Availability"
                                         :de-CH "Verfügbarkeit"}
             :availability-unrestricted {:en-GB "Availability from/until"
                                         :de-CH "Verfügbarkeit von/bis"}
             :availability-label {:de-CH "{quantity} Stück verfügbar {startDate, date, narrow} – {endDate, date, narrow}",
                                  :en-GB "{quantity} {quantity, plural, =1 {item} other {items}} available {startDate, date, narrow} – {endDate, date, narrow}"}

             :availability-modal {:title {:en-GB "Filter Availability"
                                          :de-CH "Filter Verfügbarkeit"}
                                  :timespan {:title {:en-GB "Timespan"
                                                     :de-CH "Zeitraum"}
                                             :undefined {:en-GB "undefined"
                                                         :de-CH "Unbestimmt"}
                                             :errors {:start-date-and-end-date-set {:en-GB "Start and end date must be set."
                                                                                    :de-CH "Start- und Enddatum müssen gesetzt sein."}
                                                      :start-date-equal-or-before-end-date {:en-GB "Start date must be equal to or before end date."
                                                                                            :de-CH "Startdatum muss entweder gleich oder vor dem Enddatum sein."}}}
                                  :from {:en-GB "From"
                                         :de-CH "Von"}
                                  :until {:en-GB "Until"
                                          :de-CH "Bis"}
                                  :quantity {:en-GB "Quantity"
                                             :de-CH "Anzahl"}
                                  :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                  :apply {:en-GB "Apply" :de-CH "Anwenden"}}}

    :home-page {} ; see catalog
    :logout {:en-GB "Logout"
             :de-CH "Abmelden"}
    :menu {:borrow {:section-title {:en-GB "Borrow"
                                    :de-CH "Ausleihen"}
                    :catalog {:en-GB "Catalog"
                              :de-CH "Katalog"}
                    :shopping-cart {:en-GB "Cart"
                                    :de-CH "Warenkorb"}
                    :pools {:en-GB "Inventory Pools"
                            :de-CH "Inventarparks"}
                    :favorite-models {:en-GB "Favorites"
                                      :de-CH "Favoriten"}}
           :cart-item {:menu-title {:en-GB "Cart"
                                    :de-CH "Warenkob"}}
           :user {:section-title {:en-GB "User"
                                  :de-CH "Benutzer"}
                  :menu-title {:en-GB "User Menu"
                               :de-CH "Benutzermenu"}
                  :rentals {:en-GB "Orders"
                            :de-CH "Bestellungen"}
                  :current-user {:en-GB "User Account"
                                 :de-CH "Benutzerkonto"}
                  :logout {:en-GB "Logout"
                           :de-CH "Abmelden"}}
           :app-switch {:button-label {:en-GB "Borrow"
                                       :de-CH "Ausleihen"}
                        :menu-title {:en-GB "Section Menu"
                                     :de-CH "Bereichsmenu"}
                        :section-title {:en-GB "Switch Section"
                                        :de-CH "Bereich wechseln"}
                        :admin {:en-GB "Admin"
                                :de-CH "Admin"}
                        :procure {:en-GB "Procurement"
                                  :de-CH "Bedarfsermittlung"}
                        :manage {:en-GB "Lending / Inventory"
                                 :de-CH "Verleih / Inventar"}}
           :documentation {:en-GB "Help"
                           :de-CH "Hilfe"}
           :language {:section-title {:en-GB "Language"
                                      :de-CH "Sprache"}}}
    :model-show {:loading {:en-GB "Loading item"
                           :de-CH "Gegenstand wird geladen"}
                 :description {:en-GB "Description"
                               :de-CH "Beschreibung"}
                 :properties {:en-GB "Properties"
                              :de-CH "Eigenschaften"}
                 :documents {:en-GB "Documents"
                             :de-CH "Dokumente"}
                 :compatibles {:en-GB "Compatible Items"
                               :de-CH "Ergänzende Gegenstände"}
                 :add-item-to-cart {:en-GB "Add item"
                                    :de-CH "Gegenstand hinzufügen"}
                 :add-to-favorites {:en-GB "Add to favorites"
                                    :de-CH "Zu Favoriten hinzufügen"}
                 :remove-from-favorites {:en-GB "Remove from favorites"
                                         :de-CH "Von Favoriten entfernen"}
                 :previous-image {:en-GB "Previous image"
                                  :de-CH "Vorheriges Bild"}
                 :next-image {:en-GB "Next image"
                              :de-CH "Nächstes Bild"}
                 :not-available-for-current-profile {:en-GB "Item not available for current profile"
                                                     :de-CH "Gegenstand für das aktuelle Profil nicht verfügbar"}
                 :order-dialog {:title {:en-GB "Add item" :de-CH "Gegenstand hinzufügen"}
                                :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                :add {:en-GB "Add" :de-CH "Hinzufügen"}}
                 :order-success-notification {:title {:en-GB "Item added" :de-CH "Gegenstand hinzugefügt"}
                                              :item-was-added {:en-GB "The item was added to the cart" :de-CH "Der Gegenstand wurde zum Warenkorb hinzugefügt"}}}
    :models {:title {:en-GB "Search results" :de-CH "Suchresultate"}
             :no-items-found {:en-GB "No items found"
                              :de-CH "Keine Gegenstände gefunden"}}
    :pagination {:load-more {:en-GB "Load more"
                             :de-CH "Mehr laden"}}
    :pools {:title {:en-GB "Inventory Pools"
                    :de-CH "Inventarparks"}
            :available-pools {:en-GB "Available inventory pools"
                              :de-CH "Verfügbare Inventarparks"}
            :no-available-pools {:en-GB "No inventory pool available"
                                 :de-CH "Kein Inventarpark verfügbar"}
            :access-suspended {:en-GB "Access suspended"
                               :de-CH "Zugang gesperrt"}
            :no-reservable-models {:en-GB "No reservable items"
                                   :de-CH "Keine reservierbaren Gegenstände"}
            :maximum-reservation-duration {:en-GB "Maximum reservation duration {days} days"
                                           :de-CH "Maximale Reservationsdauer von {days} Tagen"}}
    :pool-show {:email {:en-GB "E-mail"
                        :de-CH "E-Mail"}
                :description {:en-GB "Description"
                              :de-CH "Beschreibung"}
                :contact {:title {:en-GB "Contact"
                                  :de-CH "Kontakt"}}
                :opening-times {:title {:en-GB "Opening times"
                                        :de-CH "Öffnungszeiten"}}
                :reservation-constraint {:title {:en-GB "Reservation constraint"
                                                 :de-CH "Reservationseinschränkung"}}
                :holidays {:title {:en-GB "Holidays"
                                   :de-CH "Feiertage"}}
                :show-remaining-holidays {:more {:en-GB "Show remaining"
                                                 :de-CH "Restliche Feiertage anzeigen"}
                                          :hide {:en-GB "Hide"
                                                 :de-CH "Verbergen"}}
                :closed {:en-GB "Closed"
                         :de-CH "Geschlossen"}}

    :profile-menu {:title {:en-GB "Switch Profile"
                           :de-CH "Profil wechseln"}}

    :rentals {:title {:en-GB "Orders"
                      :de-CH "Bestellungen"}
              :section-title-current-lendings {:en-GB "Current lendings"
                                               :de-CH "Aktuelle Ausleihen"}
              :section-title-open-rentals {:en-GB "Active orders"
                                           :de-CH "Aktive Bestellungen"}
              :section-title-closed-rentals {:en-GB "Closed orders"
                                             :de-CH "Abgeschlossene Bestellungen"}
              :no-orders-found {:en-GB "No results found for the current search filter"
                                :de-CH "Keine Treffer zum aktuellen Suchfilter gefunden"}
              :no-orders-yet {:en-GB "No orders yet"
                              :de-CH "Noch keine Bestellungen vorhanden"}
              :filter  {:pools {:title {:en-GB "Inventory pools" :de-CH "Inventarparks"}
                                :all {:en-GB "All inventory pools" :de-CH "Alle Inventarparks"}
                                :invalid-option {:en-GB "Invalid selection" :de-CH "Ungültige Auswahl"}
                                :invalid-option-info {:en-GB "The pre-selected inventory pool is not available for the current profile"
                                                      :de-CH "Der vorher gewählte Inventarpark ist für das aktuelle Profil nicht verfügbar"}}

                        :timespan-modal {:title {:en-GB "Timespan" :de-CH "Zeitraum"}
                                         :from {:en-GB "From" :de-CH "Von"}
                                         :until {:en-GB "Until" :de-CH "Bis"}
                                         :undefined {:en-GB "undefined" :de-CH "unbestimmt"}
                                         :errors {:start-date-equal-or-before-end-date
                                                  {:en-GB "Start date must be equal to or before end date."
                                                   :de-CH "Startdatum muss entweder gleich oder vor dem Enddatum sein."}}
                                         :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                         :apply {:en-GB "Apply" :de-CH "Anwenden"}}

                        ;; Note: the JS text resolver does not support nesting
                        :js-component {:search-button-label {:en-GB "Search" :de-CH "Suchen"}
                                       :search-input-placeholder {:en-GB "Search term" :de-CH "Suchbegriff"}
                                       :filter {:en-GB "Filter" :de-CH "Filter"}
                                       :status-select-label {:en-GB "Status" :de-CH "Status"}
                                       :pool-select-label {:en-GB "Inventory pools" :de-CH "Inventarparks"}
                                       :timespan-button-label {:en-GB "Timespan from/until" :de-CH "Zeitraum von/bis"}
                                       :timespan-label {:en-GB "{startDate, date, narrow} – {endDate, date, narrow}" :de-CH "{startDate, date, narrow} – {endDate, date, narrow}"}
                                       :timespan-label-from {:en-GB "{startDate, date, narrow} – " :de-CH "{startDate, date, narrow} – "}
                                       :timespan-label-until {:en-GB " – {endDate, date, narrow}" :de-CH " – {endDate, date, narrow}"}
                                       :timespan-unrestricted {:en-GB "Timespan from/until" :de-CH "Zeitraum von/bis"}}}

              :fulfillment-state-label {; states flowchart: <https://flowchart.fun/c#AoexBsHkCcBMFNoCgAEKCWBnFmCuAjAW3QBcT5YV8BPFAY10xJEMQC4UAKASQDkB9AILBgAJUgA1QQBkAlEiQARdNHh0So+JkQA3AIYl0IAHaoM2OqoMUqtQnuN6A5uy4AVSP2DcAwgGl+AFVgeQBtPiERcSlpAF0UCOExSRkzFxJsPQAHLOgQHQoOTg8vXwDg+TQsFFUAKzVyWCLRAFEAKRafNxbFSvN6Bzp4cEKuH0FeHxbpHrCS738g4Hj5sqWzaqz0OgBrG1wsooAhSFFxAHVZpFCTs8hLxXjbi56N7FUSXGhjUc5Wt0Col4V1C-0BwMeKDBQNeoPanW6kNaHS6sPGk2mPXi6KmM0UQA>
                                        :IN_APPROVAL {:de-CH "Genehmigung" :en-GB "Approval"}
                                        :TO_PICKUP {:de-CH "Abholung" :en-GB "Pickup"}
                                        :TO_RETURN {:de-CH "Rückgabe" :en-GB "Return"}
                                        :RETURNED {:de-CH "Alle Gegenstände zurückgebracht" :en-GB "All items returned"}
                                        :REJECTED {:de-CH "Bestellung wurde abgelehnt" :en-GB "Order was rejected"}
                                        :EXPIRED-UNAPPROVED {:de-CH "Abgelaufen (nicht genehmigt)" :en-GB "Expired (not approved)"}
                                        :CANCELED {:de-CH "Bestellung wurde storniert" :en-GB "Order was canceled"}
                                        :EXPIRED {:de-CH "Abgelaufen (nicht abgeholt)" :en-GB "Expired (not picked up)"}
                                        :OVERDUE {:de-CH "Rückgabe überfällig" :en-GB "Overdue"}}

              :x-items {:en-GB "{itemCount, plural, =1 {{itemCount} Gegenstand} other {{itemCount} Gegenstände}}"
                        :de-CH "{itemCount, plural, =1 {{itemCount} Gegenstand} other {{itemCount} Gegenstände}}"}

              :summary-line
              {:open {:de-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {Am {fromDate, date, short}, {itemCount} Gegenstand}
                                  other {Am {fromDate, date, short}, {itemCount} Gegenstände}
                                }}
                                other {{itemCount, plural,
                                  =1 {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstand}
                                  other {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstände}
                                }}
                              }"
                      :en-GB "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {On {fromDate, date, short}, {itemCount} item}
                                  other {On {fromDate, date, short}, {itemCount} items}
                                }}
                                other {{itemCount, plural,
                                  =1 {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} item}
                                  other {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} items}
                                }}
                              }"}
               :closed {:de-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {Am {fromDate, date, short}, {itemCount} Gegenstand}
                                  other {Am {fromDate, date, short}, {itemCount} Gegenstände}
                                }}
                                other {{itemCount, plural,
                                  =1 {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstand}
                                  other {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstände}
                                }}
                              }"
                        :en-GB "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {On {fromDate, date, short}, {itemCount} item}
                                  other {On {fromDate, date, short}, {itemCount} items}
                                }}
                                other {{itemCount, plural,
                                  =1 {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} item}
                                  other {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} items}
                                }}
                              }"}}
               ;

              :fulfillment-state
              {:summary-line
               {:IN_APPROVAL
                {:de-CH "{totalCount, plural,
                          =1 {{doneCount} von {totalCount} Gegenstand genehmigt}
                          other {{doneCount} von {totalCount} Gegenständen genehmigt}
                        }"
                 :en-GB "{totalCount, plural,
                          =1 {{doneCount} of {totalCount} item approved}
                          other {{doneCount} of {totalCount} items approved}
                        }"}
                :TO_PICKUP
                {:de-CH "{totalCount, plural,
                          =1 {{doneCount} von {totalCount} Gegenstand abgeholt}
                          other {{doneCount} von {totalCount} Gegenständen abgeholt}
                        }"
                 :en-GB "{totalCount, plural,
                          =1 {{doneCount} of {totalCount} item picked up}
                          other {{doneCount} of {totalCount} items picked up}
                        }"}
                :TO_RETURN
                {:de-CH "{totalCount, plural,
                          =1 {{doneCount} von {totalCount} Gegenstand zurückgebracht}
                          other {{doneCount} von {totalCount} Gegenständen zurückgebracht}
                        }"
                 :en-GB "{totalCount, plural,
                          =1 {{doneCount} of {totalCount} item returned}
                          other {{doneCount} of {totalCount} items returned}
                        }"}}

               :partial-status {:REJECTED {:de-CH " ({count} abgelehnt)" :en-GB " ({count} rejected)"}
                                :EXPIRED {:de-CH " ({count} abgelaufen)" :en-GB " ({count} expired)"}
                                :OVERDUE {:de-CH " ({count} überfällig)" :en-GB " ({count} overdue)"}}}}

    :rental-show {:page-title {:de-CH "Bestellung" :en-GB "Order"}
                  :message-403 {:de-CH "Diese Bestellung ist für das aktuelle Profil nicht sichtbar"
                                :en-GB "This order is not visible for the current profile"}
                  :state {:de-CH "Status" :en-GB "State"}
                  :cancel-action-label {:de-CH "Bestellung stornieren" :en-GB "Cancel order"}
                  :repeat-action-label {:de-CH "Bestellung wiederholen" :en-GB "Repeat order"}
                  :purpose {:de-CH "Zweck" :en-GB "Purpose"}
                  :contact-details {:de-CH "Kontaktdaten" :en-GB "Contact details"}
                  :pools-section-title {:de-CH "Inventarparks" :en-GB "Inventory pools"}
                  :items-section-title {:de-CH "Gegenstände" :en-GB "Items"}
                  :documents-section-title {:de-CH "Dokumente" :en-GB "Documents"}
                  :user-or-delegation-section-title {:de-CH "Bestellung für" :en-GB "Order for"}

                  :reservation-line
                  {:title {:de-CH "{itemCount}× {itemName}" :en-GB "{itemCount}× {itemName}"}
                   :duration-days {:de-CH "{totalDays, plural,
                                             =1 {# Tag}
                                             other {# Tage}
                                           }"
                                   :en-GB "{totalDays, plural,
                                             =1 {# day}
                                             other {# days}
                                           }"}
                   :overdue {:de-CH "überfällig" :en-GB "overdue"}
                   :option {:de-CH "Option" :en-GB "Option"}}

                  :reservation-status-label {:DRAFT {:de-CH "DRAFT" :en-GB "DRAFT"} ; (will never appear in this view)
                                             :UNSUBMITTED {:de-CH "UNSUBMITTED" :en-GB "UNSUBMITTED"} ; (will never appear in this view)
                                             :SUBMITTED {:de-CH "In Genehmigung" :en-GB "In approval"}
                                             :APPROVED {:de-CH "Abholung" :en-GB "Pick up"}
                                             :REJECTED {:de-CH "Abgelehnt" :en-GB "Rejected"}
                                             :SIGNED {:de-CH "Rückgabe" :en-GB "Return"}
                                             :CLOSED {:de-CH "Zurückgebracht" :en-GB "Returned"}
                                             :CANCELED {:de-CH "Storniert" :en-GB "Canceled"}
                                             ; Temporal statusses:
                                             :EXPIRED-UNAPPROVED {:de-CH "Abgelaufen (nicht genehmigt)" :en-GB "Expired (not approved)"}
                                             :EXPIRED {:de-CH "Abgelaufen (nicht abgeholt)" :en-GB "Expired (not picked up)"}}
                  :in-x-days {:de-CH "{days, plural,
                                      =0 {heute}
                                      =1 {morgen}
                                      other {in # Tagen}
                                      }"
                              :en-GB "{days, plural,
                                      =0 {today}
                                      =1 {tomorrow}
                                      other {in # days}
                                      }"}
                  :cancellation-dialog {:title {:de-CH "Bestellung stornieren" :en-GB "Cancel order"}
                                        :confirm {:de-CH "Stornieren" :en-GB "Cancel order"}
                                        :cancel {:de-CH "Abbrechen" :en-GB "Abort"}}
                  :repeat-order {:dialog {:title {:de-CH "Gegenstände hinzufügen" :en-GB "Add items"}
                                          :info {:de-CH "{count, plural,
                                                         =1 {Ein Gegenstand wird zum Warenkorb hinzugefügt.}
                                                         other {# Gegenstände werden zum Warenkorb hinzugefügt.}}"
                                                 :en-GB "{count, plural,
                                                         =1 {One item will be added to the cart.}
                                                         other {# items will be added to the cart.}}"}
                                          :error-only-options {:de-CH "Optionen können nur durch die Verleihstelle hinzugefügt werden."
                                                               :en-GB "Options can only be added by the lending desk."}
                                          :warning-some-options {:de-CH "{count, plural,
                                                         =1 {Hinweis: Eine Option kann nur durch die Verleihstelle hinzugefügt werden.}
                                                         other {Hinweis: # Optionen können nur durch die Verleihstelle hinzugefügt werden.}}"
                                                                 :en-GB "{count, plural,
                                                         =1 {Please note: One option can only be added by the lending desk.}
                                                         other {Please note: # options can only be added by the lending desk.}}"}
                                          :timespan {:de-CH "Zeitraum" :en-GB "Time span"}
                                          :undefined {:de-CH "Unbestimmt" :en-GB "undefined"}
                                          :from {:de-CH "Von" :en-GB "From"}
                                          :until {:de-CH "Bis" :en-GB "Until"}
                                          :order-for {:de-CH "Bestellung für" :en-GB "Order for"}
                                          :cancel {:de-CH "Abbrechen" :en-GB "Cancel"}
                                          :submit {:de-CH "Hinzufügen" :en-GB "Add"}
                                          :validation {:start-after-end {:de-CH "Enddatum muss nach Beginndatum sein"
                                                                         :en-GB "End date must be after start date"}
                                                       :start-date-in-past {:de-CH "Datum liegt in der Vergangenheit"
                                                                            :en-GB "Date is in the past"}
                                                       :end-date-too-late {:de-CH "Datum darf nicht nach {maxDate, date, small} sein"
                                                                           :en-GB "Date must not be after {maxDate, date, small}"}}}
                                 :success-notification {:title {:de-CH "Gegenstände hinzugefügt" :en-GB "Items added"}
                                                        :message {:de-CH "{count, plural,
                                                                          =1 {# Gegenstand wurde zum Warenkorb hinzugefügt und kann nun dort überprüft werden.}
                                                                          other {# Gegenstände wurden zum Warenkorb hinzugefügt und können nun dort überprüft werden.}}"
                                                                  :en-GB "{count, plural,
                                                                          =1 {# item was added to the cart and can be reviewed/edited there.}
                                                                          other {# items were added to the cart and can be reviewed/edited there.}}"}
                                                        :confirm {:de-CH "Zum Warenkorb" :en-GB "Go to cart"}}}}
                  ;

    :shopping-cart {:title {:en-GB "Cart"
                            :de-CH "Warenkorb"}
                    :edit {:en-GB "Edit"
                           :de-CH "Editieren"}
                    :draft {:title {:en-GB "Draft"
                                    :de-CH "Draft"}
                            :add-to-cart {:en-GB "Add to cart"
                                          :de-CH "Add to cart"}
                            :delete {:en-GB "Delete draft"
                                     :de-CH "Delete draft"}
                            :empty {:en-GB "Your draft is empty"
                                    :de-CH "Your draft is empty"}}
                    :countdown {:section-title {:en-GB "Status"
                                                :de-CH "Status"}
                                :time-limit {:en-GB "Time limit"
                                             :de-CH "Zeitlimit"}
                                :time-left {:en-GB "{minutesLeft, plural,
                                                    =1 {# minute left}
                                                    other {# minutes left}
                                                    }"
                                            :de-CH "{minutesLeft, plural,
                                                    =1 {Noch eine Minute übrig}
                                                    other {Noch # Minuten übrig}
                                                    }"}
                                :time-left-last-minute {:en-GB "Less than one minute left"
                                                        :de-CH "Weniger als eine Minute übrig"}
                                :no-valid-items {:en-GB "No valid items"
                                                 :de-CH "Keine gültigen Gegenstände"}
                                :expired {:en-GB "Expired"
                                          :de-CH "Abgelaufen"}
                                :reset {:en-GB "Reset time limit"
                                        :de-CH "Zeitlimit zurückstellen"}}
                    :delegation {:section-title {:de-CH "Bestellung für" :en-GB "Order for"}}
                    :line {:section-title {:en-GB "Items"
                                           :de-CH "Gegenstände"}
                           :total {:en-GB "Total"
                                   :de-CH "Total"}
                           :total-models {:en-GB "Model(s)"
                                          :de-CH "Modell(e)"}
                           :total-items {:en-GB "Item(s)"
                                         :de-CH "Gegenstand/Gegenstände"}
                           :from {:en-GB "from"
                                  :de-CH "aus"}
                           :first-pickup {:en-GB "First pickup"
                                          :de-CH "Erste Abholung"}
                           :last-return {:en-GB "last return"
                                         :de-CH "letzte Rückgabe"}
                           :invalid-items-warning {:en-GB "{invalidItemsCount, plural,
                                                           =1 {# invalid item}
                                                           other {# invalid items}
                                                           }"
                                                   :de-CH "{invalidItemsCount, plural,
                                                           =1 {# Gegenstand ungültig}
                                                           other {# Gegenstände ungültig}
                                                           }"}
                           :duration-days {:de-CH "{totalDays, plural,
                                                                        =1 {# Tag}
                                                                        other {# Tage}
                                                                      }"
                                           :en-GB "{totalDays, plural,
                                                                        =1 {# day}
                                                                        other {# days}
                                                                      }"}}
                    :edit-dialog {:dialog-title {:en-GB "Edit reservation" :de-CH "Reservation bearbeiten"}
                                  :delete-reservation {:en-GB "Remove reservation" :de-CH "Reservation entfernen"}
                                  :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                  :confirm {:en-GB "Confirm" :de-CH "Bestätigen"}}
                    :confirm-order {:en-GB "Send order"
                                    :de-CH "Bestellung abschicken"}
                    :delete-order {:en-GB "Delete cart"
                                   :de-CH "Warenkorb löschen"}
                    :order-overview {:en-GB "Cart"
                                     :de-CH "Warenkorb"}
                    :empty-order {:en-GB "No items added"
                                  :de-CH "Noch keine Gegenstände hinzugefügt"}
                    :borrow-items {:en-GB "Go to catalog"
                                   :de-CH "Hier geht's zum Katalog"}
                    :confirm-dialog {:dialog-title {:en-GB "Send order" :de-CH "Bestellung abschicken"}
                                     :title {:en-GB "Title" :de-CH "Titel"}
                                     :title-hint {:en-GB "As a reference for you" :de-CH "Als Referenz für dich"}
                                     :purpose {:en-GB "Purpose" :de-CH "Zweck"}
                                     :purpose-hint {:en-GB "For the inventory pool" :de-CH "Für den Inventarpark"}
                                     :contact-details {:en-GB "Contact details" :de-CH "Kontaktdaten"}
                                     :contact-details-hint {:en-GB "The indication of a telephone number is recommended"
                                                            :de-CH "Die Angabe einer Telefonnummer ist empfohlen"}
                                     :lending-terms {:en-GB "Lending terms" :de-CH "Ausleihbedingungen"}
                                     :i-accept {:en-GB "I accept the lending terms"
                                                :de-CH "Ich akzeptiere die Ausleihbedingungen"}
                                     :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                     :confirm {:en-GB "Send" :de-CH "Abschicken"}}
                    :order-success-notification {:title {:en-GB "Order submitted" :de-CH "Bestellung übermittelt"}
                                                 :order-submitted {:en-GB "Order was submitted but still needs to be approved!"
                                                                   :de-CH "Die Bestellung wurde übermittelt, muss aber noch genehmigt werden!"}}
                    :delete-dialog {:dialog-title {:en-GB "Delete cart"
                                                   :de-CH "Warenkorb löschen"}
                                    :really-delete-order {:en-GB "Really remove all reservations?"
                                                          :de-CH "Wirklich alle Reservationen entfernen?"}
                                    :cancel {:en-GB "Cancel"
                                             :de-CH "Abbrechen"}
                                    :confirm {:en-GB "Delete"
                                              :de-CH "Löschen"}}}
    :templates {:index {:title {:de-CH "Vorlagen" :en-GB "Templates"}
                        :no-templates-for-current-profile {:de-CH "Für das aktuelle Profil sind keine Vorlagen verfügbar"
                                                           :en-GB "No templates available for the current profile"}}
                :show {:title {:de-CH "Vorlage" :en-GB "Template"}
                       :items {:de-CH "Gegenstände" :en-GB "Items"}
                       :item-title {:de-CH "{itemCount}× {itemName}" :en-GB "{itemCount}× {itemName}"}
                       :template-not-available {:de-CH "Diese Vorlage ist für das aktuelle Profil nicht verfügbar"
                                                :en-GB "This template is not available for the current profile"}
                       :some-items-not-available {:de-CH "Die Gegenstände in grauer Schrift sind für das aktuelle Profil nicht reservierbar"
                                                  :en-GB "The items shown in grey font are not reservable for the current profile"}
                       :no-items-available {:de-CH "Diese Vorlage enthält keine Gegenstände, welche für das aktuelle Profil reservierbar sind"
                                            :en-GB "This template does not contain any items that can be reserved for the current profile"}
                       :apply-button-label {:de-CH "Gegenstände bestellen"
                                            :en-GB "Order items"}}
                :apply {:dialog {:title {:de-CH "Gegenstände hinzufügen" :en-GB "Add items"}
                                 :info {:de-CH "{count, plural, 
                                                         =1 {Ein Gegenstand wird zum Warenkorb hinzugefügt.}
                                                         other {# Gegenstände werden zum Warenkorb hinzugefügt.}}"
                                        :en-GB "{count, plural, 
                                                         =1 {One item will be added to the cart.}
                                                         other {# items will be added to the cart.}}"}
                                 :error-no-items {:de-CH "Keine Gegenstände gefunden"
                                                  :en-GB "No items found"}
                                 :timespan {:de-CH "Zeitraum" :en-GB "Time span"}
                                 :undefined {:de-CH "Unbestimmt" :en-GB "undefined"}
                                 :from {:de-CH "Von" :en-GB "From"}
                                 :until {:de-CH "Bis" :en-GB "Until"}
                                 :order-for {:de-CH "Bestellung für" :en-GB "Order for"}
                                 :cancel {:de-CH "Abbrechen" :en-GB "Cancel"}
                                 :submit {:de-CH "Hinzufügen" :en-GB "Add"}
                                 :validation {:start-after-end {:de-CH "Enddatum muss nach Beginndatum sein"
                                                                :en-GB "End date must be after start date"}
                                              :start-date-in-past {:de-CH "Datum liegt in der Vergangenheit"
                                                                   :en-GB "Date is in the past"}
                                              :end-date-too-late {:de-CH "Datum darf nicht nach {maxDate, date, small} sein"
                                                                  :en-GB "Date must not be after {maxDate, date, small}"}}}
                        :success-notification {:title {:de-CH "Gegenstände hinzugefügt" :en-GB "Items added"}
                                               :message {:de-CH "{count, plural, 
                                                                          =1 {Ein Gegenstand wurde zum Warenkorb hinzugefügt und kann nun dort überprüft werden.}
                                                                          other {# Gegenstände wurden zum Warenkorb hinzugefügt und können nun dort überprüft werden.}}"
                                                         :en-GB "{count, plural, 
                                                                          =1 {One item was added to the cart and can be reviewed/edited there.}
                                                                          other {# items were added to the cart and can be reviewed/edited there.}}"}
                                               :confirm {:de-CH "Zum Warenkorb" :en-GB "Go to cart"}}}}}})
