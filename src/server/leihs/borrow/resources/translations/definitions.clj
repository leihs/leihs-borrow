(ns leihs.borrow.resources.translations.definitions)

(def definitions
  {:borrow

  ; --- GENERAL TERMS --- 
  ; prefer using those over something like `example-page-section-title-contracts`
  ; but NOT for interpolation / string concating (that assumes to much about the languages)
  ; TODO: implement fallback keys so we can have both: `(t [:example-page-section-title-contracts :terms.contracts])
   {:terms {:contract {:de-CH "Vertrag" :en-GB "Contract"}
            :contracts {:de-CH "Verträge" :en-GB "Contracts"}

            :delegation {:de-CH "Delegation" :en-GB "Delegation"}
            :delegations {:de-CH "Delegationen" :en-GB "Delegations"}}

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
      :en-GB "Contract {ID} with {poolName} from {date, date, narrow}"}}

    :all {:en-GB "All"
          :de-CH "Alle"}

  ; --- PER COMPONENT TRANSLATIONS (reused between some features) --- 

    :order-panel
    {:label {:quantity {:en-GB "Quantity" :de-CH "Anzahl"}
             :minus {:en-GB "Minus" :de-CH "Minus"}
             :plus {:en-GB "Plus" :de-CH "Plus"}
             :pool {:en-GB "Inventory Pool" :de-CH "Gerätepark"}
             :pool-max-amount {:en-GB "{pool} (max. {amount, number})" :de-CH "{pool} (max. {amount, number})"}
             :user-delegation {:en-GB "Delegation" :de-CH "Delegation"}
             :timespan {:en-GB "Time span" :de-CH "Zeitraum"}
             :from {:en-GB "From" :de-CH "Von"}
             :until {:en-GB "Until" :de-CH "Bis"}}
     :validate {:start-after-end
                {:de-CH "Enddatum muss nach Beginndatum sein"
                 :en-GB "End date must be after start date"}
                :user-delegation-cant-be-changed-active-cart
                {:en-GB "The delegation can't be changed, as an active cart already exists."
                 :de-CH "Die delegation kann nicht geändert werden, da schon ein aktiver Warenkorb existiert."}
                :missing-quantity
                {:de-CH "Verfügbarkeit kann nicht geprüft werden, da die Anzahl fehlt"
                 :en-GB "Availability can not be checked because the quantity is missing"}
                :quantity-to-large-in-range
                {:de-CH
                 "Gegenstand ist in diesem Zeitraum nicht in der gewünschten Menge verfügbar"
                 :en-GB
                 "Item is not available in the desired quantity during this period"}
                :pool-closed-at-end-date
                {:de-CH "Gerätepark ist am {endDate, date, small} geschlossen"
                 :en-GB "Inventory Pool is closed on {endDate, date, small}"}
                :end-date-to-late
                {:de-CH "Datum darf nicht nach {maxDate, date, small} sein"
                 :en-GB "Date must not be after {maxDate, date, small}"}
                :quantity-to-large-at-day
                {:de-CH
                 "Gegenstand ist am {startDate, date, small} nicht in der gewünschten Menge verfügbar"
                 :en-GB "Item is not available in the requested quantity on {startDate, date, small}."}
                :pool-closed-at-start-date
                {:de-CH "Gerätepark ist am {startDate, date, small} geschlossen"
                 :en-GB "Inventory Pool is closed on {startDate, date, small}"}
                :start-date-to-early
                {:de-CH "Datum darf nicht vor {minDate, date, small} sein"
                 :en-GB "Date must not be before {minDate, date, small}"}
                :invalid-start-date
                {:de-CH "Ungültiges Beginndatum"
                 :en-GB "Invalid start date"}
                :invalid-end-date
                {:de-CH "Ungültiges Enddatum"
                 :en-GB "Invalid end date"}}}

  ; --- PER FEATURE TRANSLATIONS --- 

    :categories {:title {:en-GB "Categories"
                         :de-CH "Kategorien"}
                 :sub-categories {:en-GB "Sub-categories"
                                  :de-CH "Unterkategorien"}
                 :items {:en-GB "Items"
                         :de-CH "Gegenstände"}}
    :current-user {:title {:en-GB "User account"
                           :de-CH "Benutzerkonto"}
                   :user-data {:en-GB "User data"
                               :de-CH "Nutzerdaten"}
                   :metadata-summary {:en-GB "ID {userId}"
                                      :de-CH "ID {userId}"}}

    :debug-page {:title {:en-GB "Debugging"
                         :de-CH "Debugging"}}

    :delegations {:title {:en-GB "Delegations"
                          :de-CH "Delegationen"}
                  :responsible {:en-GB "Responsible"
                                :de-CH "Verantwortliche Person"}
                  :members {:en-GB "Members"
                            :de-CH "Mitglieder"}
                  :loading {:en-GB "Loading delegation"
                            :de-CH "Delegation wird geladen"}}
    :favorite-models {:title {:en-GB "My Favorites"
                              :de-CH "Meine Favoriten"}
                      :items {:en-GB "Items"
                              :de-CH "Gegenstände"}}
    :filter {:search {:title {:en-GB "Search term"
                              :de-CH "Stichwort"}
                      :placeholder {:en-GB "Enter search term"
                                    :de-CH "Suchbegriff eingeben"}}
             :delegations {:en-GB "{n, plural, =1 {Delegation} other {Delegations}}"
                           :de-CH "{n, plural, =1 {Delegation} other {Delegationen}}"}
             :for {:en-GB "For"
                   :de-CH "Für"}
             :time-span {:title {:en-GB "Time Span"
                                 :de-CH "Zeitraum"}
                         :undefined {:en-GB "undefined"
                                     :de-CH "unbestimmt"}
                         :errors {:start-date-and-end-date-set {:en-GB "Start and end date must be set."
                                                                :de-CH "Das Start- und Enddatum müssen gesetzt sein."}
                                  :start-date-equal-or-before-end-date {:en-GB "Start date must be equal to or before end date."
                                                                        :de-CH "Startdatum muss entweder gleich oder vor dem Enddatum sein."}}}
             :show-only-available {:en-GB "Show available only"
                                   :de-CH "Nur Verfügbare anzeigen"}
             :pools {:title {:en-GB "Inventory pools" :de-CH "Geräteparks"}
                     :all {:en-GB "All" :de-CH "Alle"}}
             :from {:en-GB "From"
                    :de-CH "Von"}
             :until {:en-GB "Until"
                     :de-CH "Bis"}
             :quantity {:en-GB "Quantity"
                        :de-CH "Anzahl"}
             :get-results {:en-GB "Get Results"
                           :de-CH "Resultate anzeigen"}
             :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
             :apply {:en-GB "Apply" :de-CH "Anwenden"}
             :reset {:en-GB "Reset" :de-CH "Zurücksetzen"}}
    :home-page {:title {:en-GB "Home"
                        :de-CH "Home"}
                :catalog {:en-GB "Catalog"
                          :de-CH "Katalog"}
                :show-search-and-filter {:en-GB "Show search/filter"
                                         :de-CH "Zeige Suche/Filter"}}
    :logout {:en-GB "Logout"
             :de-CH "Abmelden"}
    :menu {:borrow {:section-title {:en-GB "Borrow"
                                    :de-CH "Ausleihen"}
                    :catalog {:en-GB "Catalog"
                              :de-CH "Katalog"}
                    :shopping-cart {:en-GB "New Rental"
                                    :de-CH "Neue Ausleihe"}}
           :user {:rentals {:en-GB "My Rentals"
                            :de-CH "Meine Ausleihen"}
                  :favorite-models {:en-GB "Favorites"
                                    :de-CH "Favoriten"}
                  :current-user {:en-GB "User Account"
                                 :de-CH "Benutzerkonto"}
                  :logout {:en-GB "Logout"
                           :de-CH "Abmelden"}}
           :help {:section-title {:en-GB "Help"
                                  :de-CH "Hilfe"}
                  :documentation {:en-GB "Documentation"
                                  :de-CH "Dokumentation"}
                  :support {:en-GB "Support"
                            :de-CH "Unterstützung"}}
           :language {:section-title {:en-GB "Language"
                                      :de-CH "Sprache"}}}
    :model-show {:loading {:en-GB "Loading model"
                           :de-CH "Modell wird geladen"}
                 :compatibles {:en-GB "Compatible Models"
                               :de-CH "Ergänzende Modelle"}
                 :add-item-to-cart {:en-GB "Add item"
                                    :de-CH "Gegenstand hinzufügen"}
                 :add-to-favorites {:en-GB "Add to favorites"
                                    :de-CH "Zu Favoriten hinzufügen"}
                 :remove-from-favorites {:en-GB "Remove from favorites"
                                         :de-CH "Von Favoriten entfernen"}
                 :order-dialog {:title {:en-GB "Add item" :de-CH "Gegenstand hinzufügen"}
                                :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                :add {:en-GB "Add" :de-CH "Hinzufügen"}}
                 :order-success-notification {:title {:en-GB "Item added" :de-CH "Gegenstand hinzugefügt"}
                                              :item-was-added {:en-GB "The item was added to the new rental" :de-CH "Der Gegenstand wurde zur Neuen Ausleihe hinzugefügt"}}}
    :models {:title {:en-GB "Search results" :de-CH "Suchresultate"}}
    :pagination {:load-more {:en-GB "Load more"
                             :de-CH "Mehr laden"}
                 :nothing-found {:en-GB "Nothing found"
                                 :de-CH "Nichts gefunden"}}
    :pools {:title {:en-GB "Pools"
                    :de-CH "Geräteparks"}
            :access-suspended {:en-GB "Your access is suspended"
                               :de-CH "Zugang gesperrt"}
            :no-reservable-models {:en-GB "No reservable models"
                                   :de-CH "Keine reservierbaren Modelle"}
            :maximum-reservation-pre {:en-GB "Maximum reservation of "
                                      :de-CH "Maximale Reservierung von "}
            :maximum-reservation-post {:en-GB " days"
                                       :de-CH " Tagen"}}

    :rentals {:title {:en-GB "My Rentals"
                      :de-CH "Meine Ausleihen"}
              :filter-bubble-label {:en-GB "All Rentals"
                                    :de-CH "Alle Ausleihen"}
              :section-title-open-rentals {:en-GB "Open"
                                           :de-CH "Offen"}
              :section-title-closed-rentals {:en-GB "Closed"
                                             :de-CH "Abgeschlossen"}
              :orderless-fallback-title {:de-CH "Direktausleihe"}
              :filter  {:search {:title {:en-GB "Search term"
                                         :de-CH "Stichwort"}
                                 :placeholder {:en-GB "Enter search term"
                                               :de-CH "Suchbegriff eingeben"}}
                        :delegations {:en-GB "{n, plural, =1 {Delegation} other {Delegations}}"
                                      :de-CH "{n, plural, =1 {Delegation} other {Delegationen}}"}
                        :for {:en-GB "For"
                              :de-CH "Für"}
                        :time-span {:title {:en-GB "Time Span"
                                            :de-CH "Zeitraum"}
                                    :undefined {:en-GB "undefined"
                                                :de-CH "unbestimmt"}
                                    :errors {:start-date-equal-or-before-end-date
                                             {:en-GB "Start date must be equal to or before end date."
                                              :de-CH "Startdatum muss entweder gleich oder vor dem Enddatum sein."}}}
                        :pools {:title {:en-GB "Inventory pools" :de-CH "Geräteparks"}
                                :all {:en-GB "All" :de-CH "Alle"}}
                        :states {:title {:en-GB "Status" :de-CH "Status"}
                                 :all {:en-GB "All" :de-CH "Alle"}}
                        :from {:en-GB "From"
                               :de-CH "Von"}
                        :until {:en-GB "Until"
                                :de-CH "Bis"}
                        :get-results {:en-GB "Get Results"
                                      :de-CH "Resultate anzeigen"}
                        :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                        :apply {:en-GB "Apply" :de-CH "Anwenden"}
                        :reset {:en-GB "Reset" :de-CH "Zurücksetzen"}}

              :fulfillment-state-label {; states flowchart: <https://flowchart.fun/c#AoexBsHkCcBMFNoCgAEKCWBnFmCuAjAW3QBcT5YV8BPFAY10xJEMQC4UAKASQDkB9AILBgAJUgA1QQBkAlEiQARdNHh0So+JkQA3AIYl0IAHaoM2OqoMUqtQnuN6A5uy4AVSP2DcAwgGl+AFVgeQBtPiERcSlpAF0UCOExSRkzFxJsPQAHLOgQHQoOTg8vXwDg+TQsFFUAKzVyWCLRAFEAKRafNxbFSvN6Bzp4cEKuH0FeHxbpHrCS738g4Hj5sqWzaqz0OgBrG1wsooAhSFFxAHVZpFCTs8hLxXjbi56N7FUSXGhjUc5Wt0Col4V1C-0BwMeKDBQNeoPanW6kNaHS6sPGk2mPXi6KmM0UQA>
                                        :IN_APPROVAL {:de-CH "Genehmigung" :en-GB "In Approval"}
                                        :TO_PICKUP {:de-CH "Abholung" :en-GB "To pick up"}
                                        :TO_RETURN {:de-CH "Rückgabe" :en-GB "To return"}
                                        :RETURNED {:de-CH "Alle Gegenstände zurückgebracht" :en-GB "All items returned"}
                                        :REJECTED {:de-CH "Ausleihe wurde abgelehnt" :en-GB "Rental was rejeced"}
                                        :CANCELED {:de-CH "Ausleihe wurde storniert" :en-GB "Rental was canceled"}}

              :summary-line
              {:open {:de-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {{totalDays} Tag ab {fromDate, date, short}, {itemCount} Gegenstand}
                                  other {{totalDays} Tag ab {fromDate, date, short}, {itemCount} Gegenstände}
                                }}
                                other {{itemCount, plural,
                                  =1 {{totalDays} Tage ab {fromDate, date, short}, {itemCount} Gegenstand}
                                  other {{totalDays} Tage ab {fromDate, date, short}, {itemCount} Gegenstände}
                                }}
                              }"
                      :en-GB "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {{totalDays} day from {fromDate, date, short}, {itemCount} item}
                                  other {{totalDays} day from {fromDate, date, short}, {itemCount} items}
                                }}
                                other {{itemCount, plural,
                                  =1 {{totalDays} days from {fromDate, date, short}, {itemCount} item}
                                  other {{totalDays} days from {fromDate, date, short}, {itemCount} items}
                                }}
                              }"}
               :closed {:de-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {{totalDays} Tag bis {untilDate, date, short}, {itemCount} Gegenstand}
                                  other {{totalDays} Tag bis {untilDate, date, short}, {itemCount} Gegenstände}
                                }}
                                other {{itemCount, plural,
                                  =1 {{totalDays} Tage bis {untilDate, date, short}, {itemCount} Gegenstand}
                                  other {{totalDays} Tage bis {untilDate, date, short}, {itemCount} Gegenstände}
                                }}
                              }"
                        :en-GB "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {{totalDays} day until {untilDate, date, short}, {itemCount} item}
                                  other {{totalDays} day until {untilDate, date, short}, {itemCount} items}
                                }}
                                other {{itemCount, plural,
                                  =1 {{totalDays} days until {untilDate, date, short}, {itemCount} item}
                                  other {{totalDays} days until {untilDate, date, short}, {itemCount} items}
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
                        }"}}}}

    :rental-show {:page-title {:de-CH "Ausleihe" :en-GB "Rental"}
                  :state {:de-CH "Status" :en-GB "State"}
                  :cancel-action-label {:de-CH "Ausleihe stornieren" :en-GB "Cancel rental"}
                  :purpose {:de-CH "Zweck" :en-GB "Purpose"}
                  :pools-section-title {:de-CH "Geräteparks" :en-GB "Inventory pools"}
                  :items-section-title {:de-CH "Gegenstände" :en-GB "Items"}
                  :documents-section-title {:de-CH "Dokumente" :en-GB "Documents"}
                  :user-or-delegation-section-title {:de-CH "Delegation" :en-GB "Delegation"}
                  :user-or-delegation-personal-postfix {:de-CH " (persönlich)" :en-GB " (personal)"}
                  :metadata-summary {:de-CH "ID {rentalId}" :en-GB "ID {rentalId}"}
                  :reservation-line
                  {:title
                   {:de-CH "{itemCount}× {itemName}"
                    :en-GB "{itemCount}× {itemName}"}
                   :duration {:de-CH "{totalDays, plural,
                                  =1 {# Tag ab {fromDate, date, short}}
                                  other {# Tage ab {fromDate, date, short}}
                                }"
                              :en-GB "{totalDays, plural,
                                  =1 {# day from {fromDate, date, short}}
                                  other {# days from {fromDate, date, short}}
                                }"}}}
                  ;


    :shopping-cart {:title {:en-GB "Cart"
                            :de-CH "Warenkorb"}
                    :edit {:en-GB "Edit"
                           :de-CH "Editieren"}
                    :draft {:title {:en-GB "Draft"
                                    :de-CH "Draft"}
                            :add-to-cart {:en-GB "Add To Cart"
                                          :de-CH "Add To Cart"}
                            :delete {:en-GB "Delete Draft"
                                     :de-CH "Delete Draft"}
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
                                :expired {:en-GB "Expired"
                                          :de-CH "Abgelaufen"}
                                :reset {:en-GB "Reset time limit"
                                        :de-CH "Zeitlimit zurückstellen"}}
                    :delegation {:section-title {:de-CH "Delegation" :en-GB "Delegation"}
                                 :person-postfix  {:de-CH " (persönlich)" :en-GB " (personal)"}}
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
                           :duration {:de-CH "{totalDays, plural,
                                              =1 {# Tag ab {fromDate, date, short}}
                                              other {# Tage ab {fromDate, date, short}}
                                              }"
                                      :en-GB "{totalDays, plural,
                                              =1 {# day from {fromDate, date, short}}
                                              other {# days from {fromDate, date, short}}
                                              }"}}
                    :edit-dialog {:dialog-title {:en-GB "Edit reservation" :de-CH "Reservation bearbeiten"}
                                  :delete-reservation {:en-GB "Remove reservation" :de-CH "Reservation entfernen"}
                                  :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                  :confirm {:en-GB "Confirm" :de-CH "Bestätigen"}}
                    :confirm-order {:en-GB "Confirm rental"
                                    :de-CH "Ausleihe bestätigen"}
                    :delete-order {:en-GB "Delete rental"
                                   :de-CH "Ausleihe löschen"}
                    :order-overview {:en-GB "New rental"
                                     :de-CH "Neue Ausleihe"}
                    :empty-order {:en-GB "No items added"
                                  :de-CH "Noch keine Gegenstände hinzugefügt"}
                    :borrow-items {:en-GB "Go to catalog"
                                   :de-CH "Hier geht's zum Katalog"}
                    :confirm-dialog {:dialog-title {:en-GB "Confirm new rental" :de-CH "Neue Ausleihe bestätigen"}
                                     :title {:en-GB "Title" :de-CH "Titel"}
                                     :purpose {:en-GB "Purpose" :de-CH "Zweck"}
                                     :cancel {:en-GB "Cancel" :de-CH "Abbrechen"}
                                     :confirm {:en-GB "Confirm" :de-CH "Bestätigen"}}
                    :order-success-notification {:title {:en-GB "Order submitted" :de-CH "Bestellung übermittelt"}
                                                 :order-submitted {:en-GB "Order was submitted but still needs to be approved!"
                                                                   :de-CH "Die Bestellung wurde übermittelt, muss aber noch genehmigt werden!"}}
                    :delete-dialog {:dialog-title {:en-GB "Delete new rental"
                                                   :de-CH "Neue Ausleihe löschen"}
                                    :really-delete-order {:en-GB "Really remove all reservations?"
                                                          :de-CH "Wirklich alle Reservationen entfernen?"}
                                    :cancel {:en-GB "Cancel"
                                             :de-CH "Abbrechen"}
                                    :confirm {:en-GB "Delete"
                                              :de-CH "Löschen"}}}
    :templates {:title {:en-GB "Templates"
                        :de-CH "Vorlagen"}
                :some-not-reservable {:en-GB "Some models are not reservable!"
                                      :de-CH "Some models are not reservable!"}}}})
