(ns leihs.borrow.resources.translations.definitions)

(def definitions
  {:borrow
   {:all {:en-GB "All"
          :de-CH "Alle"}
    :about-page {:title {:en-GB "About"
                         :de-CH "Übersicht"}
                 :navigation-menu {:en-GB "Navigation Menu"
                                   :de-CH "Navigationsmenü"}}
    :categories {:title {:en-GB "Categories"
                         :de-CH "Kategorien"}}
    :current-user {:title {:en-GB "Current User"
                           :de-CH "Aktueller Benutzer"}}

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
    :filter {:search {:en-GB "Search"
                      :de-CH "Suche"}
             :for {:en-GB "For"
                   :de-CH "Für"}
             :time-span {:en-GB "Time Span"
                         :de-CH "Zeitraum"}
             :show-only-available {:en-GB "Show available only"
                                   :de-CH "Nur Verfügbare anzeigen"}
             :from {:en-GB "From"
                    :de-CH "Von"}
             :until {:en-GB "Until"
                     :de-CH "Bis"}
             :quantity {:en-GB "Quantity"
                        :de-CH "Anzahl"}
             :get-results {:en-GB "Get Results"
                           :de-CH "Resultate anzeigen"}
             :clear {:en-GB "Clear"
                     :de-CH "Löschen"}
             :pools {:all {:en-GB "All inventory pools"
                           :de-CH "Allen Geräteparks"}}}
    :home-page {:title {:en-GB "Home"
                        :de-CH "Home"}}
    :logout {:en-GB "Logout"
             :de-CH "Abmelden"}
    :model-show {:loading {:en-GB "Loading model"
                           :de-CH "Modell wird geladen"}
                 :compatibles {:en-GB "Compatible Models"
                               :de-CH "Ergänzende Modelle"}}
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

              :refined-state-label {; states flowchart: <https://flowchart.fun/c#AoexBsHkCcBMFNoCgAEKCWBnFmCuAjAW3QBcT5YV8BPFAY10xJEMQC4UAKASQDkB9AILBgAJUgA1QQBkAlEiQARdNHh0So+JkQA3AIYl0IAHaoM2OqoMUqtQnuN6A5uy4AVSP2DcAwgGl+AFVgeQBtPiERcSlpAF0UCOExSRkzFxJsPQAHLOgQHQoOTg8vXwDg+TQsFFUAKzVyWCLRAFEAKRafNxbFSvN6Bzp4cEKuH0FeHxbpHrCS738g4Hj5sqWzaqz0OgBrG1wsooAhSFFxAHVZpFCTs8hLxXjbi56N7FUSXGhjUc5Wt0Col4V1C-0BwMeKDBQNeoPanW6kNaHS6sPGk2mPXi6KmM0UQA>
                                    :IN_APPROVAL {:de-CH "Genehmigung" :en-GB "In Approval"}
                                    :TO_PICKUP {:de-CH "Abholung" :en-GB "To pick up"}
                                    :TO_RETURN {:de-CH "Rückgabe" :en-GB "To return"}
                                    :RETURNED {:de-CH "Alle Gegenstände zurückgebracht" :en-GB "All items returned"}
                                    :REJECTED {:de-CH "Ausleihe wurde abgelehnt" :en-GB "Rental was rejeced"}
                                    :CANCELED {:de-CH "Ausleihe wurde storniert" :en-GB "Rental was canceled"}}

              :summary
              {:open1 {:de-CH "" :en-GB ""}
               :open2 {:de-CH " Tage ab " :en-GB " days from "}
               :open3 {:de-CH ", " :en-GB ", "}
               :open4 {:de-CH " Gegenstand/-stände" :en-GB " item(s)"}
               :closed1 {:de-CH "" :en-GB ""}
               :closed2 {:de-CH " Tage bis " :en-GB " days until "}
               :closed3 {:de-CH ", " :en-GB ", "}
               :closed4 {:de-CH " Gegenstand/-stände" :en-GB " item(s)"}}
              :fulfillment-state
              {:items-approved1 {:de-CH "" :en-GB ""}
               :items-approved2 {:de-CH " von " :en-GB " of "}
               :items-approved3 {:de-CH " " :en-GB ""}
               :items-approved4 {:de-CH " Gegenstand/-ständen genehmigt" :en-GB " item(s) approved"}
               :items-pickedup1 {:de-CH "" :en-GB ""}
               :items-pickedup2 {:de-CH " von " :en-GB " of "}
               :items-pickedup3 {:de-CH " " :en-GB ""}
               :items-pickedup4 {:de-CH " Gegenstand/-ständen abgeholt" :en-GB " item(s) picked up"}
               :items-returned1 {:de-CH "" :en-GB ""}
               :items-returned2 {:de-CH " von " :en-GB " of "}
               :items-returned3 {:de-CH " " :en-GB ""}
               :items-returned4 {:de-CH " Gegenstand/-ständen zurückgebracht" :en-GB " item(s) returned"}}
              #_{:open-singular {:de-CH "{days} ab {date}, {count} Gegenstand"}
                 :open-plural {:de-CH "{days} ab {date}, {count} Gegenstände"}
                 :closed-singular {:de-CH "{days} bis {date}, {count} Gegenstand"}
                 :closed-plural {:de-CH "{days} bis {date}, {count} Gegenstände"}}}

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
                                      :de-CH "Some models are not reservable!"}}
    :visits {:pickups {:title {:en-GB "Pickups"
                               :de-CH "Abholungen"}}
             :returns {:title {:en-GB "Rückgaben"
                               :de-CH "Rückgaben"}}}}})
