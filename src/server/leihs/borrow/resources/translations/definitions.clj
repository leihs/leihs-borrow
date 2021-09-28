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


  ; --- PER FEATURE TRANSLATIONS --- 

    :about-page {:title {:en-GB "About"
                         :de-CH "Übersicht"}
                 :navigation-menu {:en-GB "Navigation Menu"
                                   :de-CH "Navigationsmenü"}}
    :categories {:title {:en-GB "Categories"
                         :de-CH "Kategorien"}}
    :current-user {:title {:en-GB "User account"
                           :de-CH "Benutzerkonto"}
                   :user-data {:en-GB "User data"
                               :de-CH "Nutzerdaten"}
                   :metadata-summary {:en-GB "ID {userId}"
                                      :de-CH "ID {userId}"}}

    :delegations {:title {:en-GB "Delegations"
                          :de-CH "Delegationen"}
                  :responsible {:en-GB "Responsible"
                                :de-CH "Verantwortliche Person"}
                  :members {:en-GB "Members"
                            :de-CH "Mitglieder"}
                  :loading {:en-GB "Loading delegation"
                            :de-CH "Delegation wird geladen"}}
    :favorite-models {:title {:en-GB "Favorites"
                              :de-CH "Favoriten"}}
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
                                     :de-CH "unbestimmt"}}
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
    :model-show {:loading {:en-GB "Loading model"
                           :de-CH "Modell wird geladen"}
                 :compatibles {:en-GB "Compatible Models"
                               :de-CH "Ergänzende Modelle"}
                 :add-item-to-cart {:en-GB "Add item"
                                    :de-CH "Gegenstand hinzufügen"}
                 :add-to-favorites {:en-GB "Add to favorites"
                                    :de-CH "Zu Favoriten hinzufügen"}
                 :remove-from-favorites {:en-GB "Remove from favorites"
                                         :de-CH "Von Favoriten entfernen"}}
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

              :filter {:delegation {:de-CH "Für Delegation" :en-GB "For Delegation"}}

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
                              }"}
               ;
               }

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
                                }"}}
                  ;
                  }

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
                    :line {:total {:en-GB "Total"
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
                                         :de-CH "letzte Rückgabe"}}
                    :confirm-order {:en-GB "Confirm order"
                                    :de-CH "Bestellung bestätigen"}
                    :delete-order {:en-GB "Delete order"
                                   :de-CH "Bestellung löschen"}
                    :order-overview {:en-GB "Order Overview"
                                     :de-CH "Bestellübersicht"}
                    :empty-order {:en-GB "Your order is empty"
                                  :de-CH "Deine Bestellung ist leer"}
                    :borrow-items {:en-GB "Borrow Items"
                                   :de-CH "Gegenstände ausleihen"}
                    :order-title {:en-GB "Order Name"
                                  :de-CH "Name der Bestellung"}
                    :order-title-placeholder {:en-GB "Name Your Order"
                                              :de-CH "Benenne deine Bestellung"}
                    :order-purpose {:en-GB "Order Purpose"
                                    :de-CH "Zweck der Bestellung"}
                    :order-purpose-placeholder {:en-GB "Enter a purpose"
                                                :de-CH "Gebe einen Zweck ein"}}
    :templates {:title {:en-GB "Templates"
                        :de-CH "Vorlagen"}
                :some-not-reservable {:en-GB "Some models are not reservable!"
                                      :de-CH "Some models are not reservable!"}}}})
