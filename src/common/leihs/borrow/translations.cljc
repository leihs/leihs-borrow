(ns leihs.borrow.translations)

(def dict
  {:borrow

  ; --- GENERAL TERMS ---
  ; prefer using those over something like `example-page-section-title-contracts`
  ; but NOT for interpolation / string concating (that assumes to much about the languages)
  ; TODO: implement fallback keys so we can have both: `(t [:example-page-section-title-contracts :terms.contracts])
   {:terms {:contract {:de-CH "Vertrag" :en-GB "Contract" :fr-CH "Contrat"}
            :contracts {:de-CH "Verträge" :en-GB "Contracts" :fr-CH "Contrats"}

            :delegation {:de-CH "Delegation" :en-GB "Delegation" :fr-CH "Délégation"}
            :delegations {:de-CH "Delegationen" :en-GB "Delegations" :fr-CH "Délégations"}

            :weekdays {:monday {:de-CH "Montag" :en-GB "Monday" :fr-CH "Lundi"}
                       :tuesday {:de-CH "Dienstag" :en-GB "Tuesday" :fr-CH "Mardi"}
                       :wednesday {:de-CH "Mittwoch" :en-GB "Wednesday" :fr-CH "Mercredi"}
                       :thursday {:de-CH "Donnerstag" :en-GB "Thursday" :fr-CH "Jeudi"}
                       :friday {:de-CH "Freitag" :en-GB "Friday" :fr-CH "Vendredi"}
                       :saturday {:de-CH "Samstag" :en-GB "Saturday" :fr-CH "Samedi"}
                       :sunday {:de-CH "Sonntag" :en-GB "Sunday" :fr-CH "Dimanche"}}}

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
                }"
      :fr-CH "{itemCount, plural,
                =0 {Aucun objet}
                =1 {# objet}
                other {# objets}
                }"}

     :contract-display-name
     {:de-CH "Vertrag {ID} mit {poolName} vom {date, date, narrow}"
      :en-GB "Contract {ID} with {poolName} from {date, date, narrow}"
      :fr-CH "Contrat {ID} avec {poolName} du {date, date, narrow}"}

     :user-or-delegation-personal-postfix
     {:de-CH " (persönlich)" :en-GB " (personal)" :fr-CH " (personnel)"}}

    :all {:en-GB "All"
          :de-CH "Alle"
          :fr-CH "Tous"}

  ; --- PER COMPONENT TRANSLATIONS (reused between some features) ---

    :order-panel
    {:label {:quantity {:en-GB "Quantity" :de-CH "Anzahl" :fr-CH "Quantité"}
             :minus {:en-GB "Minus" :de-CH "Minus" :fr-CH "Moins"}
             :plus {:en-GB "Plus" :de-CH "Plus" :fr-CH "Plus"}
             :pool {:en-GB "Inventory pool" :de-CH "Inventarpark" :fr-CH "Pool d'inventaire"}
             :pool-max-amount {:en-GB "{pool} (max. {amount, number})" :de-CH "{pool} (max. {amount, number})" :fr-CH "{pool} (max. {amount, number})"}
             :pool-max-amount-info {:en-GB  "Maximum available amount: {amount, number}" :de-CH "Maximal verfügbarer Bestand: {amount, number}" :fr-CH "Quantité maximale disponible : {amount, number}"}
             :user-delegation {:en-GB "Order for" :de-CH "Bestellung für" :fr-CH "Commande pour"}
             :timespan {:en-GB "Time span" :de-CH "Zeitraum" :fr-CH "Période"}
             :from {:en-GB "From" :de-CH "Von" :fr-CH "De"}
             :until {:en-GB "Until" :de-CH "Bis" :fr-CH "À"}
             :undefined {:en-GB "undefined" :de-CH "Unbestimmt" :fr-CH "Indéfini"}
             :show-day-quants {:en-GB "Show availability in calendar" :de-CH "Verfügbarkeit im Kalender anzeigen" :fr-CH "Afficher la disponibilité dans le calendrier"}}
     :validate {:missing-quantity
                {:de-CH "Verfügbarkeit kann nicht geprüft werden, da die Anzahl fehlt"
                 :en-GB "Availability can not be checked because the quantity is missing"
                 :fr-CH "La disponibilité ne peut pas être vérifiée car la quantité est manquante"}

                :invalid-start-date
                {:de-CH "Ungültiges Beginndatum"
                 :en-GB "Invalid start date"
                 :fr-CH "Date de début invalide"}
                :invalid-end-date
                {:de-CH "Ungültiges Enddatum"
                 :en-GB "Invalid end date"
                 :fr-CH "Date de fin invalide"}
                :start-after-end
                {:de-CH "Enddatum muss nach Beginndatum sein"
                 :en-GB "End date must be after start date"
                 :fr-CH "La date de fin doit être postérieure à la date de début"}
                :end-date-too-late
                {:de-CH "Datum darf nicht nach {maxDate, date, small} sein"
                 :en-GB "Date must not be after {maxDate, date, small}"
                 :fr-CH "La date ne doit pas dépasser {maxDate, date, small}"}

                :start-date-in-past
                {:de-CH "Abholdatum liegt in der Vergangenheit"
                 :en-GB "Pickup date is in the past"
                 :fr-CH "La date de retrait se situe dans le passé"}
                :start-date-not-before
                {:de-CH "{days, plural, =1 {Abholung frühestens morgen} other {Abholung frühestens heute in # Arbeitstagen}}"
                 :en-GB "{days, plural, =1 {Earliest pickup date is tomorrow} other {Earliest pickup date in # working days from now}}"
                 :fr-CH "{days, plural,
                          =1 {Retrait possible au plus tôt demain}
                          other {Retrait possible dans # jours ouvrables}}"}
                :quantity-to-large-at-day
                {:de-CH
                 "Gegenstand ist am {startDate, date, small} nicht in der gewünschten Menge verfügbar"
                 :en-GB "Item is not available in the requested quantity on {startDate, date, small}."
                 :fr-CH "L'article n'est pas disponible dans la quantité demandée le {startDate, date, small}."}
                :quantity-to-large-in-range
                {:de-CH "Gegenstand ist in diesem Zeitraum nicht in der gewünschten Menge verfügbar"
                 :en-GB "Item is not available in the desired quantity during this period"
                 :fr-CH "L'article n'est pas disponible dans la quantité souhaitée durant cette période"}

                :pool-closed-at-start-date
                {:de-CH "Abholung am {startDate, date, small} nicht möglich"
                 :en-GB "Pickup not possible on {startDate, date, small}"
                 :fr-CH "Retrait non possible le {startDate, date, small}"}
                :pool-closed-at-end-date
                {:de-CH "Rückgabe am {endDate, date, small} nicht möglich"
                 :en-GB "Return not possible on {endDate, date, small}"
                 :fr-CH "Retour non possible le {endDate, date, small}"}
                :pool-closed-at-start-and-end-date
                {:de-CH "Abholung/Rückgabe am {startDate, date, small} nicht möglich"
                 :en-GB "Pickup/return not possible on {startDate, date, small}"
                 :fr-CH "Retrait/retour non possible le {startDate, date, small}"}
                :pool-closed-max-visits
                {:de-CH " (maximale Besucherzahl erreicht)"
                 :en-GB " (maximum visitor capacity reached)"
                 :fr-CH " (capacité maximale de visiteurs atteinte)"}
                :closed-on-day-of-week
                {:de-CH "{dayName} geschlossen"
                 :en-GB "closed on {dayName}"
                 :fr-CH "fermé le {dayName}"}

                :maximum-reservation-duration
                {:de-CH "Maximale Reservationsdauer ist beschränkt auf {days} Tage"
                 :en-GB "Maximum reservation duration is restricted to {days} days"
                 :fr-CH "La durée maximale de réservation est limitée à {days} jours"}

                :no-pool-access
                {:de-CH "Keine Berechtigung für diesen Inventarpark"
                 :en-GB "No access to this inventory pool"
                 :fr-CH "Aucun accès à ce pool d'inventaire"}
                :pool-suspension
                {:de-CH "Benutzer für diesen Inventarpark gesperrt"
                 :en-GB "User suspended for this inventory pool"
                 :fr-CH "Utilisateur suspendu pour ce pool d'inventaire"}
                :item-not-available-in-pool
                {:de-CH "Gegenstand in diesem Inventarpark nicht verfügbar"
                 :en-GB "Item not available in this inventory pool"
                 :fr-CH "Article non disponible dans ce pool d'inventaire"}
                :unknown-pool
                {:de-CH "Unbekannter Inventarpark"
                 :en-GB "Unknown inventory pool"
                 :fr-CH "Pool d'inventaire inconnu"}}}

  ; --- PER FEATURE TRANSLATIONS ---

    :catalog {:title {:en-GB "Catalog"
                      :de-CH "Katalog"
                      :fr-CH "Catalogue"}
              :categories {:en-GB "Categories"
                           :de-CH "Kategorien"
                           :fr-CH "Catégories"}
              :no-reservable-items {:en-GB "No reservable items found"
                                    :de-CH "Keine reservierbaren Gegenstände gefunden"
                                    :fr-CH "Aucun article réservable trouvé"}
              :check-available-pools {:en-GB "Check available inventory pools"
                                      :de-CH "Verfügbare Inventarparks prüfen"
                                      :fr-CH "Vérifier les pools d'inventaire disponibles"}
              :templates {:en-GB "Templates"
                          :de-CH "Vorlagen"
                          :fr-CH "Modèles"}}
    :categories {:sub-categories {:en-GB "Sub-categories"
                                  :de-CH "Unterkategorien"
                                  :fr-CH "Sous-catégories"}
                 :items {:en-GB "Items"
                         :de-CH "Gegenstände"
                         :fr-CH "Articles"}
                 :category-root {:en-GB "All Categories"
                                 :de-CH "Alle Kategorien"
                                 :fr-CH "Toutes les catégories"}}
    :current-user {:title {:en-GB "User Account"
                           :de-CH "Benutzerkonto"
                           :fr-CH "Compte utilisateur"}
                   :user-data {:en-GB "User data"
                               :de-CH "Nutzerdaten"
                               :fr-CH "Données utilisateur"}
                   :no-contracts {:en-GB "None"
                                  :de-CH "Noch keine"
                                  :fr-CH "Aucun"}
                   :fields {:email {:en-GB "E-mail"
                                    :de-CH "E-Mail"
                                    :fr-CH "E-mail"}
                            :secondary-email {:en-GB "Secondary e-mail"
                                              :de-CH "Zweite E-Mail"
                                              :fr-CH "Deuxième e-mail"}
                            :phone {:en-GB "Phone number"
                                    :de-CH "Telefon"
                                    :fr-CH "Numéro de téléphone"}
                            :org {:en-GB "Organisation"
                                  :de-CH "Organisation"
                                  :fr-CH "Organisation"}
                            :org-id {:en-GB "ID"
                                     :de-CH "ID"
                                     :fr-CH "ID"}
                            :badge-id {:en-GB "Badge ID"
                                       :de-CH "Badge-ID"
                                       :fr-CH "Identifiant du badge"}}}

    :debug-page {:title {:en-GB "Debugging"
                         :de-CH "Debugging"
                         :fr-CH "Débogage"}}

    :errors {:error {:en-GB "Error"
                     :de-CH "Fehler"
                     :fr-CH "Erreur"}
             :processing-error {:en-GB "Error when processing this action"
                                :de-CH "Fehler beim Ausführen dieser Aktion"
                                :fr-CH "Erreur lors du traitement de cette action"}
             :render-error {:en-GB "Error displaying this content"
                            :de-CH "Fehler bei der Anzeige"
                            :fr-CH "Erreur lors de l'affichage de ce contenu"}
             :loading-error {:en-GB "Error loading this content"
                             :de-CH "Fehler beim Laden"
                             :fr-CH "Erreur lors du chargement de ce contenu"}
             ; 401
             :unauthorized {:en-GB "User not logged in"
                            :de-CH "Benutzer ist nicht angemeldet"
                            :fr-CH "Utilisateur non connecté"}
             ; 403
             :forbidden {:en-GB "This Resource is not available for the current profile"
                         :de-CH "Diese Ressource ist für das aktuelle Profil nicht verfügbar"
                         :fr-CH "Cette ressource n'est pas disponible pour le profil actuel"}
             :reload {:en-GB "Reload current page"
                      :de-CH "Seite neu laden"
                      :fr-CH "Recharger la page"}
             :go-to-start {:en-GB "Go to start page"
                           :de-CH "Zur Startseite"
                           :fr-CH "Aller à la page d'accueil"}
             :go-to-login {:en-GB "Go to login"
                           :de-CH "Zum Login"
                           :fr-CH "Aller à la connexion"}}

    :favorite-models {:title {:en-GB "Favorites"
                              :de-CH "Favoriten"
                              :fr-CH "Favoris"}
                      :items {:en-GB "Items"
                              :de-CH "Gegenstände"
                              :fr-CH "Éléments"}
                      :no-favorites {:en-GB "No favorites added yet"
                                     :de-CH "Noch keine Favoriten hinzugefügt"
                                     :fr-CH "Aucun favori ajouté pour le moment"}
                      :go-to-catalog {:en-GB "Go to catalog"
                                      :de-CH "Hier geht's zum Katalog"
                                      :fr-CH "Aller au catalogue"}}
    :filter {:search-input-placeholder {:de-CH "Suchbegriff"
                                        :en-GB "Search term"
                                        :fr-CH "Terme de recherche"}
             :search-button-label {:de-CH "Suchen"
                                   :en-GB "Search"
                                   :fr-CH "Rechercher"}

             :filter {:en-GB "Filter"
                      :de-CH "Filter"
                      :fr-CH "Filtrer"}

             :pool-select-label {:en-GB "Inventory pools" :de-CH "Inventarparks" :fr-CH "Pools d'inventaire"}
             :all-pools-option-label {:en-GB "All inventory pools" :de-CH "Alle Inventarparks" :fr-CH "Tous les pools d'inventaire"}
             :invalid-pool-option-label {:en-GB "Invalid selection" :de-CH "Ungültige Auswahl" :fr-CH "Sélection invalide"}
             :invalid-pool-message {:en-GB "The pre-selected inventory pool is not available for the current profile"
                                    :de-CH "Der vorher gewählte Inventarpark ist für das aktuelle Profil nicht verfügbar"
                                    :fr-CH "Le pool d'inventaire pré-sélectionné n'est pas disponible pour le profil actuel"}
             :pool-suspended-message {:de-CH "Benutzer für diesen Inventarpark gesperrt"
                                      :en-GB "User suspended for this inventory pool"
                                      :fr-CH "Utilisateur suspendu pour ce pool d'inventaire"}

             :availability-button-label {:en-GB "Availability"
                                         :de-CH "Verfügbarkeit"
                                         :fr-CH "Disponibilité"}
             :availability-unrestricted {:en-GB "Availability from/until"
                                         :de-CH "Verfügbarkeit von/bis"
                                         :fr-CH "Disponibilité de/à"}
             :availability-label {:de-CH "{quantity} Stück verfügbar {startDate, date, narrow} – {endDate, date, narrow}",
                                  :en-GB "{quantity} {quantity, plural, =1 {item} other {items}} available {startDate, date, narrow} – {endDate, date, narrow}"
                                  :fr-CH "{quantity} {quantity, plural, =1 {pièce disponible} other {pièces disponibles}} du {startDate, date, narrow} au {endDate, date, narrow}"}

             :availability-modal {:title {:en-GB "Filter Availability"
                                          :de-CH "Filter Verfügbarkeit"
                                          :fr-CH "Filtrer la disponibilité"}
                                  :timespan {:title {:en-GB "Timespan"
                                                     :de-CH "Zeitraum"
                                                     :fr-CH "Période"}
                                             :undefined {:en-GB "undefined"
                                                         :de-CH "Unbestimmt"
                                                         :fr-CH "Indéfini"}
                                             :errors {:start-date-and-end-date-set {:en-GB "Start and end date must be set."
                                                                                    :de-CH "Start- und Enddatum müssen gesetzt sein."
                                                                                    :fr-CH "Les dates de début et de fin doivent être définies."}
                                                      :start-date-equal-or-before-end-date {:en-GB "Start date must be equal to or before end date."
                                                                                            :de-CH "Startdatum muss entweder gleich oder vor dem Enddatum sein."
                                                                                            :fr-CH "La date de début doit être antérieure ou égale à la date de fin."}}}
                                  :from {:en-GB "From"
                                         :de-CH "Von"
                                         :fr-CH "De"}
                                  :until {:en-GB "Until"
                                          :de-CH "Bis"
                                          :fr-CH "À"}
                                  :quantity {:en-GB "Quantity"
                                             :de-CH "Anzahl"
                                             :fr-CH "Quantité"}
                                  :cancel {:en-GB "Cancel" :de-CH "Abbrechen" :fr-CH "Annuler"}
                                  :apply {:en-GB "Apply" :de-CH "Anwenden" :fr-CH "Appliquer"}}}

    :home-page {} ; see catalog
    :logout {:en-GB "Logout"
             :de-CH "Abmelden"
             :fr-CH "Déconnexion"}
    :menu {:borrow {:section-title {:en-GB "Borrow"
                                    :de-CH "Ausleihen"
                                    :fr-CH "Emprunter"}
                    :catalog {:en-GB "Catalog"
                              :de-CH "Katalog"
                              :fr-CH "Catalogue"}
                    :shopping-cart {:en-GB "Cart"
                                    :de-CH "Warenkorb"
                                    :fr-CH "Panier"}
                    :pools {:en-GB "Inventory Pools"
                            :de-CH "Inventarparks"
                            :fr-CH "Pools d'inventaire"}
                    :favorite-models {:en-GB "Favorites"
                                      :de-CH "Favoriten"
                                      :fr-CH "Favoris"}}
           :cart-item {:menu-title {:en-GB "Cart"
                                    :de-CH "Warenkob"
                                    :fr-CH "Panier"}}
           :user {:section-title {:en-GB "User"
                                  :de-CH "Benutzer"
                                  :fr-CH "Utilisateur"}
                  :menu-title {:en-GB "User Menu"
                               :de-CH "Benutzermenu"
                               :fr-CH "Menu utilisateur"}
                  :rentals {:en-GB "Orders"
                            :de-CH "Bestellungen"
                            :fr-CH "Commandes"}
                  :current-user {:en-GB "User Account"
                                 :de-CH "Benutzerkonto"
                                 :fr-CH "Compte utilisateur"}
                  :logout {:en-GB "Logout"
                           :de-CH "Abmelden"
                           :fr-CH "Déconnexion"}}
           :app-switch {:button-label {:en-GB "Borrow"
                                       :de-CH "Ausleihen"
                                       :fr-CH "Emprunter"}
                        :menu-title {:en-GB "Section Menu"
                                     :de-CH "Bereichsmenu"
                                     :fr-CH "Menu de section"}
                        :section-title {:en-GB "Switch Section"
                                        :de-CH "Bereich wechseln"
                                        :fr-CH "Changer de section"}
                        :admin {:en-GB "Admin"
                                :de-CH "Admin"
                                :fr-CH "Admin"}
                        :procure {:en-GB "Procurement"
                                  :de-CH "Bedarfsermittlung"
                                  :fr-CH "Approvisionnement"}
                        :manage {:en-GB "Lending / Inventory"
                                 :de-CH "Verleih / Inventar"
                                 :fr-CH "Prêt / Inventaire"}}
           :documentation {:en-GB "Help"
                           :de-CH "Hilfe"
                           :fr-CH "Aide"}
           :language {:section-title {:en-GB "Language"
                                      :de-CH "Sprache"
                                      :fr-CH "Langue"}}}
    :model-show {:loading {:en-GB "Loading item"
                           :de-CH "Gegenstand wird geladen"
                           :fr-CH "Chargement de l'élément"}
                 :description {:en-GB "Description"
                               :de-CH "Beschreibung"
                               :fr-CH "Description"}
                 :properties {:en-GB "Properties"
                              :de-CH "Eigenschaften"
                              :fr-CH "Propriétés"}
                 :documents {:en-GB "Documents"
                             :de-CH "Dokumente"
                             :fr-CH "Documents"}
                 :compatibles {:en-GB "Compatible Items"
                               :de-CH "Ergänzende Gegenstände"
                               :fr-CH "Éléments compatibles"}
                 :add-item-to-cart {:en-GB "Add item"
                                    :de-CH "Gegenstand hinzufügen"
                                    :fr-CH "Ajouter l'élément"}
                 :add-to-favorites {:en-GB "Add to favorites"
                                    :de-CH "Zu Favoriten hinzufügen"
                                    :fr-CH "Ajouter aux favoris"}
                 :remove-from-favorites {:en-GB "Remove from favorites"
                                         :de-CH "Von Favoriten entfernen"
                                         :fr-CH "Retirer des favoris"}
                 :previous-image {:en-GB "Previous image"
                                  :de-CH "Vorheriges Bild"
                                  :fr-CH "Image précédente"}
                 :next-image {:en-GB "Next image"
                              :de-CH "Nächstes Bild"
                              :fr-CH "Image suivante"}
                 :not-available-for-current-profile {:en-GB "Item not available for current profile"
                                                     :de-CH "Gegenstand für das aktuelle Profil nicht verfügbar"
                                                     :fr-CH "Élément non disponible pour le profil actuel"}
                 :order-dialog {:title {:en-GB "Add item" :de-CH "Gegenstand hinzufügen" :fr-CH "Ajouter l'élément"}
                                :cancel {:en-GB "Cancel" :de-CH "Abbrechen" :fr-CH "Annuler"}
                                :add {:en-GB "Add" :de-CH "Hinzufügen" :fr-CH "Ajouter"}}
                 :order-success-notification {:title {:en-GB "Item added" :de-CH "Gegenstand hinzugefügt" :fr-CH "Élément ajouté"}
                                              :item-was-added {:en-GB "The item was added to the cart" :de-CH "Der Gegenstand wurde zum Warenkorb hinzugefügt" :fr-CH "L'élément a été ajouté au panier"}}}
    :models {:title {:en-GB "Search results" :de-CH "Suchresultate" :fr-CH "Résultats de recherche"}
             :no-items-found {:en-GB "No items found"
                              :de-CH "Keine Gegenstände gefunden"
                              :fr-CH "Aucun élément trouvé"}}
    :pagination {:load-more {:en-GB "Load more"
                             :de-CH "Mehr laden"
                             :fr-CH "Charger plus"}}
    :pools {:title {:en-GB "Inventory Pools"
                    :de-CH "Inventarparks"
                    :fr-CH "Pools d'inventaire"}
            :available-pools {:en-GB "Available inventory pools"
                              :de-CH "Verfügbare Inventarparks"
                              :fr-CH "Pools d'inventaire disponibles"}
            :no-available-pools {:en-GB "No inventory pool available"
                                 :de-CH "Kein Inventarpark verfügbar"
                                 :fr-CH "Aucun pool d'inventaire disponible"}
            :access-suspended {:en-GB "Access suspended"
                               :de-CH "Zugang gesperrt"
                               :fr-CH "Accès suspendu"}
            :no-reservable-models {:en-GB "No reservable items"
                                   :de-CH "Keine reservierbaren Gegenstände"
                                   :fr-CH "Aucun élément réservable"}
            :maximum-reservation-duration {:en-GB "Maximum reservation duration {days} days"
                                           :de-CH "Maximale Reservationsdauer von {days} Tagen"
                                           :fr-CH "Durée maximale de réservation : {days} jours"}}
    :pool-show {:email {:en-GB "E-mail"
                        :de-CH "E-Mail"
                        :fr-CH "E-mail"}
                :description {:en-GB "Description"
                              :de-CH "Beschreibung"
                              :fr-CH "Description"}
                :contact {:title {:en-GB "Contact"
                                  :de-CH "Kontakt"
                                  :fr-CH "Contact"}}
                :opening-times {:title {:en-GB "Opening times"
                                        :de-CH "Öffnungszeiten"
                                        :fr-CH "Heures d'ouverture"}}
                :reservation-constraint {:title {:en-GB "Reservation constraint"
                                                 :de-CH "Reservationseinschränkung"
                                                 :fr-CH "Restriction de réservation"}}
                :holidays {:title {:en-GB "Holidays"
                                   :de-CH "Feiertage"
                                   :fr-CH "Jours fériés"}}
                :show-remaining-holidays {:more {:en-GB "Show remaining"
                                                 :de-CH "Restliche Feiertage anzeigen"
                                                 :fr-CH "Afficher les jours restants"}
                                          :hide {:en-GB "Hide"
                                                 :de-CH "Verbergen"
                                                 :fr-CH "Masquer"}}
                :closed {:en-GB "Closed"
                         :de-CH "Geschlossen"
                         :fr-CH "Fermé"}}

    :profile-menu {:title {:en-GB "Switch Profile"
                           :de-CH "Profil wechseln"
                           :fr-CH "Changer de profil"}}

    :rentals {:title {:en-GB "Orders"
                      :de-CH "Bestellungen"
                      :fr-CH "Commandes"}
              :section-title-current-lendings {:en-GB "Current lendings"
                                               :de-CH "Aktuelle Ausleihen"
                                               :fr-CH "Prêts en cours"}
              :section-title-open-rentals {:en-GB "Active orders"
                                           :de-CH "Aktive Bestellungen"
                                           :fr-CH "Commandes actives"}
              :section-title-closed-rentals {:en-GB "Closed orders"
                                             :de-CH "Abgeschlossene Bestellungen"
                                             :fr-CH "Commandes clôturées"}
              :no-matches-found {:en-GB "No results found for the current search filter"
                                 :de-CH "Keine Treffer zum aktuellen Suchfilter gefunden"
                                 :fr-CH "Aucun résultat pour le filtre de recherche actuel"}
              :no-current-lendings-yet {:en-GB "No current lendings"
                                        :de-CH "Keine aktuellen Ausleihen vorhanden"
                                        :fr-CH "Aucun prêt en cours"}
              :no-active-orders-yet {:en-GB "No active orders"
                                     :de-CH "Keine aktiven Bestellungen vorhanden"
                                     :fr-CH "Aucune commande active"}
              :no-closed-orders-yet {:en-GB "No closed orders yet"
                                     :de-CH "Noch keine abgeschlossene Bestellungen vorhanden"
                                     :fr-CH "Aucune commande clôturée pour l'instant"}
              :info-see-also-delegations {:en-GB "To view orders from a delegation, select the corresponding profile in the user menu"
                                          :de-CH "Zum Anzeigen von Bestellungen einer Delegation das entsprechende Profil im Benutzermenü auswählen"
                                          :fr-CH "Pour voir les commandes d'une délégation, sélectionnez le profil correspondant dans le menu utilisateur"}
              :filter  {:pools {:title {:en-GB "Inventory pools" :de-CH "Inventarparks" :fr-CH "Pools d'inventaire"}
                                :all {:en-GB "All inventory pools" :de-CH "Alle Inventarparks" :fr-CH "Tous les pools d'inventaire"}
                                :invalid-option {:en-GB "Invalid selection" :de-CH "Ungültige Auswahl" :fr-CH "Sélection invalide"}
                                :invalid-option-info {:en-GB "The pre-selected inventory pool is not available for the current profile"
                                                      :de-CH "Der vorher gewählte Inventarpark ist für das aktuelle Profil nicht verfügbar"
                                                      :fr-CH "Le pool d'inventaire pré-sélectionné n'est pas disponible pour le profil actuel"}}

                        :timespan-modal {:title {:en-GB "Timespan" :de-CH "Zeitraum" :fr-CH "Période"}
                                         :from {:en-GB "From" :de-CH "Von" :fr-CH "De"}
                                         :until {:en-GB "Until" :de-CH "Bis" :fr-CH "À"}
                                         :undefined {:en-GB "undefined" :de-CH "unbestimmt" :fr-CH "indéfini"}
                                         :errors {:start-date-equal-or-before-end-date
                                                  {:en-GB "Start date must be equal to or before end date."
                                                   :de-CH "Startdatum muss entweder gleich oder vor dem Enddatum sein."
                                                   :fr-CH "La date de début doit être antérieure ou égale à la date de fin."}}
                                         :cancel {:en-GB "Cancel" :de-CH "Abbrechen" :fr-CH "Annuler"}
                                         :apply {:en-GB "Apply" :de-CH "Anwenden" :fr-CH "Appliquer"}}

                        ;; Note: the JS text resolver does not support nesting
                        :js-component {:search-button-label {:en-GB "Search" :de-CH "Suchen" :fr-CH "Rechercher"}
                                       :search-input-placeholder {:en-GB "Search term" :de-CH "Suchbegriff" :fr-CH "Terme de recherche"}
                                       :filter {:en-GB "Filter" :de-CH "Filter" :fr-CH "Filtrer"}
                                       :status-select-label {:en-GB "Status" :de-CH "Status" :fr-CH "Statut"}
                                       :pool-select-label {:en-GB "Inventory pools" :de-CH "Inventarparks" :fr-CH "Pools d'inventaire"}
                                       :timespan-button-label {:en-GB "Timespan from/until" :de-CH "Zeitraum von/bis" :fr-CH "Période de/à"}
                                       :timespan-label {:en-GB "{startDate, date, narrow} – {endDate, date, narrow}" :de-CH "{startDate, date, narrow} – {endDate, date, narrow}"  :fr-CH "{startDate, date, narrow} – {endDate, date, narrow}"}
                                       :timespan-label-from {:en-GB "{startDate, date, narrow} – " :de-CH "{startDate, date, narrow} – "  :fr-CH "{startDate, date, narrow} – "}
                                       :timespan-label-until {:en-GB " – {endDate, date, narrow}" :de-CH " – {endDate, date, narrow}"  :fr-CH " – {endDate, date, narrow}"}
                                       :timespan-unrestricted {:en-GB "Timespan from/until" :de-CH "Zeitraum von/bis" :fr-CH "Période de/à"}}}

              :fulfillment-state-label {; states flowchart
                                        :IN_APPROVAL {:de-CH "Genehmigung" :en-GB "Approval" :fr-CH "Approbation"}
                                        :TO_PICKUP {:de-CH "Abholung" :en-GB "Pickup" :fr-CH "Retrait"}
                                        :TO_RETURN {:de-CH "Rückgabe" :en-GB "Return" :fr-CH "Retour"}
                                        :RETURNED {:de-CH "Alle Gegenstände zurückgebracht" :en-GB "All items returned" :fr-CH "Tous les éléments ont été retournés"}
                                        :REJECTED {:de-CH "Bestellung wurde abgelehnt" :en-GB "Order was rejected" :fr-CH "La commande a été refusée"}
                                        :EXPIRED-UNAPPROVED {:de-CH "Abgelaufen (nicht genehmigt)" :en-GB "Expired (not approved)" :fr-CH "Expiré (non approuvé)"}
                                        :CANCELED {:de-CH "Bestellung wurde storniert" :en-GB "Order was canceled" :fr-CH "La commande a été annulée"}
                                        :EXPIRED {:de-CH "Abgelaufen (nicht abgeholt)" :en-GB "Expired (not picked up)" :fr-CH "Expiré (non retiré)"}
                                        :OVERDUE {:de-CH "Rückgabe überfällig" :en-GB "Overdue" :fr-CH "Retour en retard"}}

              :x-items {:en-GB "{itemCount, plural, =1 {{itemCount} Gegenstand} other {{itemCount} Gegenstände}}"
                        :de-CH "{itemCount, plural, =1 {{itemCount} Gegenstand} other {{itemCount} Gegenstände}}"
                        :fr-CH "{itemCount, plural,
                                  =1 {{itemCount} élément}
                                  other {{itemCount} éléments}}"}

              :summary-line
              {:open {:de-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {Am {fromDate, date, short}, {itemCount} Gegenstand}
                                  other {Am {fromDate, date, short}, {itemCount} Gegenstände}}}
                                other {{itemCount, plural,
                                  =1 {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstand}
                                  other {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstände}}}}"
                      :en-GB "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {On {fromDate, date, short}, {itemCount} item}
                                  other {On {fromDate, date, short}, {itemCount} items}}}
                                other {{itemCount, plural,
                                  =1 {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} item}
                                  other {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} items}}}}"
                      :fr-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {Le {fromDate, date, short}, {itemCount} élément}
                                  other {Le {fromDate, date, short}, {itemCount} éléments}}}
                                other {{itemCount, plural,
                                  =1 {Entre le {fromDate, date, short} et le {untilDate, date, short}, {itemCount} élément}
                                  other {Entre le {fromDate, date, short} et le {untilDate, date, short}, {itemCount} éléments}}}}"}
               :closed {:de-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {Am {fromDate, date, short}, {itemCount} Gegenstand}
                                  other {Am {fromDate, date, short}, {itemCount} Gegenstände}}}
                                other {{itemCount, plural,
                                  =1 {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstand}
                                  other {Zwischen {fromDate, date, short} und {untilDate, date, short}, {itemCount} Gegenstände}}}}"
                        :en-GB "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {On {fromDate, date, short}, {itemCount} item}
                                  other {On {fromDate, date, short}, {itemCount} items}}}
                                other {{itemCount, plural,
                                  =1 {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} item}
                                  other {Between {fromDate, date, short} and {untilDate, date, short}, {itemCount} items}}}}"
                        :fr-CH "{totalDays, plural,
                                =1 {{itemCount, plural,
                                  =1 {Le {fromDate, date, short}, {itemCount} élément}
                                  other {Le {fromDate, date, short}, {itemCount} éléments}}}
                                other {{itemCount, plural,
                                  =1 {Entre le {fromDate, date, short} et le {untilDate, date, short}, {itemCount} élément}
                                  other {Entre le {fromDate, date, short} et le {untilDate, date, short}, {itemCount} éléments}}}}"}}
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
                        }"
                 :fr-CH "{totalCount, plural,
                          =1 {{doneCount} sur {totalCount} élément approuvé}
                          other {{doneCount} sur {totalCount} éléments approuvés}}"}
                :TO_PICKUP
                {:de-CH "{totalCount, plural,
                          =1 {{doneCount} von {totalCount} Gegenstand abgeholt}
                          other {{doneCount} von {totalCount} Gegenständen abgeholt}
                        }"
                 :en-GB "{totalCount, plural,
                          =1 {{doneCount} of {totalCount} item picked up}
                          other {{doneCount} of {totalCount} items picked up}
                        }"
                 :fr-CH "{totalCount, plural,
                          =1 {{doneCount} sur {totalCount} élément retiré}
                          other {{doneCount} sur {totalCount} éléments retirés}}"}
                :TO_RETURN
                {:de-CH "{totalCount, plural,
                          =1 {{doneCount} von {totalCount} Gegenstand zurückgebracht}
                          other {{doneCount} von {totalCount} Gegenständen zurückgebracht}
                        }"
                 :en-GB "{totalCount, plural,
                          =1 {{doneCount} of {totalCount} item returned}
                          other {{doneCount} of {totalCount} items returned}
                        }"
                 :fr-CH "{totalCount, plural,
                          =1 {{doneCount} sur {totalCount} élément retourné}
                          other {{doneCount} sur {totalCount} éléments retournés}}"}}

               :partial-status {:REJECTED {:de-CH " ({count} abgelehnt)" :en-GB " ({count} rejected)" :fr-CH " ({count} refusé)"}
                                :EXPIRED {:de-CH " ({count} abgelaufen)" :en-GB " ({count} expired)" :fr-CH " ({count} expiré)"}
                                :OVERDUE {:de-CH " ({count} überfällig)" :en-GB " ({count} overdue)" :fr-CH " ({count} en retard)"}}}}

    :rental-show {:page-title {:de-CH "Bestellung" :en-GB "Order" :fr-CH "Commande"}
                  :message-403 {:de-CH "Diese Bestellung ist für das aktuelle Profil nicht sichtbar"
                                :en-GB "This order is not visible for the current profile"
                                :fr-CH "Cette commande n'est pas visible pour le profil actuel"}
                  :state {:de-CH "Status" :en-GB "State" :fr-CH "Statut"}
                  :cancel-action-label {:de-CH "Bestellung stornieren" :en-GB "Cancel order" :fr-CH "Annuler la commande"}
                  :purpose {:de-CH "Zweck" :en-GB "Purpose" :fr-CH "But"}
                  :contact-details {:de-CH "Kontaktdaten" :en-GB "Contact details" :fr-CH "Coordonnées"}
                  :pools-section-title {:de-CH "Inventarparks" :en-GB "Inventory pools" :fr-CH "Pools d'inventaire"}
                  :items-section-title {:de-CH "Gegenstände" :en-GB "Items" :fr-CH "Éléments"}
                  :documents-section-title {:de-CH "Dokumente" :en-GB "Documents" :fr-CH "Documents"}
                  :user-or-delegation-section-title {:de-CH "Bestellung für" :en-GB "Order for" :fr-CH "Commande pour"}

                  :reservation-line
                  {:title {:de-CH "{itemCount}× {itemName}" :en-GB "{itemCount}× {itemName}" :fr-CH "{itemCount}× {itemName}"}
                   :duration-days {:de-CH "{totalDays, plural,
                                             =1 {# Tag}
                                             other {# Tage}
                                           }"
                                   :en-GB "{totalDays, plural,
                                             =1 {# day}
                                             other {# days}
                                           }"
                                   :fr-CH "{totalDays, plural,
                                             =1 {# jour}
                                             other {# jours}}"}
                   :overdue {:de-CH "überfällig" :en-GB "overdue" :fr-CH "en retard"}
                   :option {:de-CH "Option" :en-GB "Option" :fr-CH "Option"}}

                  :reservation-status-label {:DRAFT {:de-CH "DRAFT" :en-GB "DRAFT" :fr-CH "DRAFT"} ; (will never appear in this view)
                                             :UNSUBMITTED {:de-CH "UNSUBMITTED" :en-GB "UNSUBMITTED" :fr-CH "UNSUBMITTED"} ; (will never appear in this view)
                                             :SUBMITTED {:de-CH "In Genehmigung" :en-GB "In approval" :fr-CH "En cours d'approbation"}
                                             :APPROVED {:de-CH "Abholung" :en-GB "Pick up" :fr-CH "Retrait"}
                                             :REJECTED {:de-CH "Abgelehnt" :en-GB "Rejected" :fr-CH "Refusé"}
                                             :SIGNED {:de-CH "Rückgabe" :en-GB "Return" :fr-CH "Retour"}
                                             :CLOSED {:de-CH "Zurückgebracht" :en-GB "Returned" :fr-CH "Retourné"}
                                             :CANCELED {:de-CH "Storniert" :en-GB "Canceled" :fr-CH "Annulé"}
                                             ; Temporal statusses:
                                             :EXPIRED-UNAPPROVED {:de-CH "Abgelaufen (nicht genehmigt)" :en-GB "Expired (not approved)" :fr-CH "Expiré (non approuvé)"}
                                             :EXPIRED {:de-CH "Abgelaufen (nicht abgeholt)" :en-GB "Expired (not picked up)" :fr-CH "Expiré (non retiré)"}}
                  :in-x-days {:de-CH "{days, plural,
                                      =0 {heute}
                                      =1 {morgen}
                                      other {in # Tagen}
                                      }"
                              :en-GB "{days, plural,
                                      =0 {today}
                                      =1 {tomorrow}
                                      other {in # days}
                                      }"
                              :fr-CH "{days, plural,
                                      =0 {aujourd'hui}
                                      =1 {demain}
                                      other {dans # jours}}"}
                  :cancellation-dialog {:title {:de-CH "Bestellung stornieren" :en-GB "Cancel order" :fr-CH "Annuler la commande"}
                                        :confirm {:de-CH "Stornieren" :en-GB "Cancel order" :fr-CH "Annuler la commande"}
                                        :cancel {:de-CH "Abbrechen" :en-GB "Abort" :fr-CH "Abandonner"}}
                  :repeat-order {:repeat-action-label {:de-CH "Bestellung wiederholen" :en-GB "Repeat order" :fr-CH "Répéter la commande"}
                                 :dialog {:title {:de-CH "Gegenstände hinzufügen" :en-GB "Add items" :fr-CH "Ajouter des articles"}
                                          :info {:de-CH "{count, plural,
                                                         =1 {Ein Gegenstand wird zum Warenkorb hinzugefügt.}
                                                         other {# Gegenstände werden zum Warenkorb hinzugefügt.}}"
                                                 :en-GB "{count, plural,
                                                         =1 {One item will be added to the cart.}
                                                         other {# items will be added to the cart.}}"
                                                 :fr-CH "{count, plural,
                                                         =1 {Un article sera ajouté au panier.}
                                                         other {# articles seront ajoutés au panier.}}"}
                                          :error-only-options {:de-CH "Optionen können nur durch die Verleihstelle hinzugefügt werden."
                                                               :en-GB "Options can only be added by the lending desk."
                                                               :fr-CH "Les options ne peuvent être ajoutées que par le guichet de prêt."}
                                          :warning-some-options {:de-CH "{count, plural,
                                                         =1 {Hinweis: Eine Option kann nur durch die Verleihstelle hinzugefügt werden.}
                                                         other {Hinweis: # Optionen können nur durch die Verleihstelle hinzugefügt werden.}}"
                                                                 :en-GB "{count, plural,
                                                         =1 {Please note: One option can only be added by the lending desk.}
                                                         other {Please note: # options can only be added by the lending desk.}}"
                                                                 :fr-CH "{count, plural,
                                                         =1 {Remarque : Une option ne peut être ajoutée que par le guichet de prêt.}
                                                         other {Remarque : # options ne peuvent être ajoutées que par le guichet de prêt.}}"}
                                          :info-multi-pool {:de-CH "Öffnungszeiten werden nicht angezeigt, wenn Gegenstände aus mehreren Inventarparks enthalten sind. Die Verfügbarkeiten können nach dem Hinzufügen im Warenkorb geprüft werden."
                                                            :en-GB "Opening hours are not displayed when items from multiple inventory pools are included. Availability can be checked in the shopping cart after adding the items."
                                                            :fr-CH "Les heures d'ouverture ne sont pas affichées lorsque des articles de plusieurs pools d'inventaire sont inclus. La disponibilité peut être vérifiée dans le panier après l'ajout des articles."}
                                          :timespan {:de-CH "Zeitraum" :en-GB "Time span" :fr-CH "Période"}
                                          :undefined {:de-CH "Unbestimmt" :en-GB "undefined" :fr-CH "Indéfini"}
                                          :from {:de-CH "Von" :en-GB "From" :fr-CH "De"}
                                          :until {:de-CH "Bis" :en-GB "Until" :fr-CH "À"}
                                          :order-for {:de-CH "Bestellung für" :en-GB "Order for" :fr-CH "Commande pour"}
                                          :cancel {:de-CH "Abbrechen" :en-GB "Cancel" :fr-CH "Annuler"}
                                          :submit {:de-CH "Hinzufügen" :en-GB "Add" :fr-CH "Ajouter"}}
                                 :success-notification {:title {:de-CH "Gegenstände hinzugefügt" :en-GB "Items added" :fr-CH "Articles ajoutés"}
                                                        :message {:de-CH "{count, plural,
                                                                          =1 {# Gegenstand wurde zum Warenkorb hinzugefügt und kann nun dort überprüft werden.}
                                                                          other {# Gegenstände wurden zum Warenkorb hinzugefügt und können nun dort überprüft werden.}}"
                                                                  :en-GB "{count, plural,
                                                                          =1 {# item was added to the cart and can be reviewed/edited there.}
                                                                          other {# items were added to the cart and can be reviewed/edited there.}}"
                                                                  :fr-CH "{count, plural,
                                                                          =1 {# article a été ajouté au panier et peut y être révisé/modifié.}
                                                                          other {# articles ont été ajoutés au panier et peuvent y être révisés/modifiés.}}"}
                                                        :confirm {:de-CH "Zum Warenkorb" :en-GB "Go to cart" :fr-CH "Aller au panier"}}}}
                  ;

    :shopping-cart {:title {:en-GB "Cart"
                            :de-CH "Warenkorb"
                            :fr-CH "Panier"}
                    :edit {:en-GB "Edit"
                           :de-CH "Editieren" :fr-CH "Modifier"}
                    :draft {:title {:en-GB "Draft"
                                    :de-CH "Draft" :fr-CH "Brouillon"}
                            :add-to-cart {:en-GB "Add to cart"
                                          :de-CH "Add to cart" :fr-CH "Ajouter au panier"}
                            :delete {:en-GB "Delete draft"
                                     :de-CH "Delete draft" :fr-CH "Supprimer le brouillon"}
                            :empty {:en-GB "Your draft is empty"
                                    :de-CH "Your draft is empty" :fr-CH "Votre brouillon est vide"}}
                    :countdown {:section-title {:en-GB "Status"
                                                :de-CH "Status" :fr-CH "Statut"}
                                :time-limit {:en-GB "Time limit"
                                             :de-CH "Zeitlimit" :fr-CH "Limite de temps"}
                                :time-left {:en-GB "{minutesLeft, plural,
                                                    =1 {# minute left}
                                                    other {# minutes left}
                                                    }"
                                            :de-CH "{minutesLeft, plural,
                                                    =1 {Noch eine Minute übrig}
                                                    other {Noch # Minuten übrig}
                                                    }"
                                            :fr-CH "{minutesLeft, plural,
                                                    =1 {# minute restante}
                                                    other {# minutes restantes}}"}
                                :time-left-last-minute {:en-GB "Less than one minute left"
                                                        :de-CH "Weniger als eine Minute übrig" :fr-CH "Moins d'une minute restante"}
                                :no-valid-items {:en-GB "No valid items"
                                                 :de-CH "Keine gültigen Gegenstände" :fr-CH "Aucun élément valide"}
                                :expired {:en-GB "Expired"
                                          :de-CH "Abgelaufen" :fr-CH "Expiré"}
                                :reset {:en-GB "Reset time limit"
                                        :de-CH "Zeitlimit zurückstellen" :fr-CH "Réinitialiser la limite de temps"}}
                    :delegation {:section-title {:de-CH "Bestellung für" :en-GB "Order for" :fr-CH "Commande pour"}}
                    :line {:section-title {:en-GB "Items"
                                           :de-CH "Gegenstände" :fr-CH "Éléments"}
                           :total {:en-GB "Total"
                                   :de-CH "Total" :fr-CH "Total"}
                           :total-models {:en-GB "Model(s)"
                                          :de-CH "Modell(e)" :fr-CH "Modèle(s)"}
                           :total-items {:en-GB "Item(s)"
                                         :de-CH "Gegenstand/Gegenstände" :fr-CH "Élément(s)"}
                           :from {:en-GB "from"
                                  :de-CH "aus" :fr-CH "de"}
                           :first-pickup {:en-GB "First pickup"
                                          :de-CH "Erste Abholung" :fr-CH "Premier retrait"}
                           :last-return {:en-GB "last return"
                                         :de-CH "letzte Rückgabe" :fr-CH "dernier retour"}
                           :invalid-items-warning {:en-GB "{invalidItemsCount, plural,
                                                           =1 {# invalid item}
                                                           other {# invalid items}
                                                           }"
                                                   :de-CH "{invalidItemsCount, plural,
                                                           =1 {# Gegenstand ungültig}
                                                           other {# Gegenstände ungültig}
                                                           }"
                                                   :fr-CH "{invalidItemsCount, plural,
                                                           =1 {# élément non valide}
                                                           other {# éléments non valides}}"}
                           :duration-days {:de-CH "{totalDays, plural,
                                                                        =1 {# Tag}
                                                                        other {# Tage}
                                                                      }"
                                           :en-GB "{totalDays, plural,
                                                                        =1 {# day}
                                                                        other {# days}
                                                                      }"
                                           :fr-CH "{totalDays, plural,
                                                                        =1 {# jour}
                                                                        other {# jours}}"}}
                    :edit-dialog {:dialog-title {:en-GB "Edit reservation" :de-CH "Reservation bearbeiten" :fr-CH "Modifier la réservation"}
                                  :delete-reservation {:en-GB "Remove reservation" :de-CH "Reservation entfernen" :fr-CH "Supprimer la réservation"}
                                  :cancel {:en-GB "Cancel" :de-CH "Abbrechen" :fr-CH "Annuler"}
                                  :confirm {:en-GB "Confirm" :de-CH "Bestätigen" :fr-CH "Confirmer"}}
                    :confirm-order {:en-GB "Send order"
                                    :de-CH "Bestellung abschicken"
                                    :fr-CH "Envoyer la demande"}
                    :delete-order {:en-GB "Delete cart"
                                   :de-CH "Warenkorb löschen"
                                   :fr-CH "Supprimer le panier"}
                    :order-overview {:en-GB "Cart"
                                     :de-CH "Warenkorb"
                                     :fr-CH "Panier"}
                    :empty-order {:en-GB "No items added"
                                  :de-CH "Noch keine Gegenstände hinzugefügt"
                                  :fr-CH "Aucun article ajouté"}
                    :borrow-items {:en-GB "Go to catalog"
                                   :de-CH "Hier geht's zum Katalog"
                                   :fr-CH "Aller au catalogue"}
                    :confirm-dialog {:dialog-title {:en-GB "Send order" :de-CH "Bestellung abschicken" :fr-CH "Envoyer la commande"}
                                     :title {:en-GB "Title" :de-CH "Titel" :fr-CH "Titre"}
                                     :title-hint {:en-GB "As a reference for you" :de-CH "Als Referenz für dich" :fr-CH "À titre de référence pour vous"}
                                     :purpose {:en-GB "Purpose" :de-CH "Zweck" :fr-CH "Objet ou référence"}
                                     :purpose-hint {:en-GB "For the inventory pool" :de-CH "Für den Inventarpark" :fr-CH "À l'attention du responsable de l'inventaire"}
                                     :contact-details {:en-GB "Contact details" :de-CH "Kontaktdaten" :fr-CH "Coordonnées"}
                                     :contact-details-hint {:en-GB "The indication of a telephone number is recommended"
                                                            :de-CH "Die Angabe einer Telefonnummer ist empfohlen"
                                                            :fr-CH "L'indication d'un numéro de téléphone est recommandée"}
                                     :lending-terms {:en-GB "Lending terms" :de-CH "Ausleihbedingungen" :fr-CH "Conditions de prêt"}
                                     :i-accept {:en-GB "I accept the lending terms"
                                                :de-CH "Ich akzeptiere die Ausleihbedingungen"
                                                :fr-CH "J'accepte les conditions de prêt"}
                                     :cancel {:en-GB "Cancel" :de-CH "Abbrechen" :fr-CH "Annuler"}
                                     :confirm {:en-GB "Send" :de-CH "Abschicken" :fr-CH "Envoyer"}}
                    :order-success-notification {:title {:en-GB "Order submitted" :de-CH "Bestellung übermittelt" :fr-CH "Commande envoyée"}
                                                 :order-submitted {:en-GB "Order was submitted but still needs to be approved!"
                                                                   :de-CH "Die Bestellung wurde übermittelt, muss aber noch genehmigt werden!"
                                                                   :fr-CH "La commande a été envoyée mais doit encore être approuvée."}}
                    :delete-dialog {:dialog-title {:en-GB "Delete cart"
                                                   :de-CH "Warenkorb löschen"
                                                   :fr-CH "Supprimer le panier"}
                                    :really-delete-order {:en-GB "Really remove all reservations?"
                                                          :de-CH "Wirklich alle Reservationen entfernen?"
                                                          :fr-CH "Supprimer vraiment toutes les réservations ?"}
                                    :cancel {:en-GB "Cancel"
                                             :de-CH "Abbrechen"
                                             :fr-CH "Annuler"}
                                    :confirm {:en-GB "Delete"
                                              :de-CH "Löschen"
                                              :fr-CH "Supprimer"}}}
    :templates {:index {:title {:de-CH "Vorlagen" :en-GB "Templates" :fr-CH "Modèles"}
                        :no-templates-for-current-profile {:de-CH "Für das aktuelle Profil sind keine Vorlagen verfügbar"
                                                           :en-GB "No templates available for the current profile" :fr-CH "Aucun modèle disponible pour le profil actuel"}}
                :show {:title {:de-CH "Vorlage" :en-GB "Template" :fr-CH "Modèle"}
                       :items {:de-CH "Gegenstände" :en-GB "Items" :fr-CH "Éléments"}
                       :item-title {:de-CH "{itemCount}× {itemName}" :en-GB "{itemCount}× {itemName}" :fr-CH "{itemCount}× {itemName}"}
                       :template-not-available {:de-CH "Diese Vorlage ist für das aktuelle Profil nicht verfügbar"
                                                :en-GB "This template is not available for the current profile" :fr-CH "Ce modèle n'est pas disponible pour le profil actuel"}
                       :user-suspended {:de-CH "Zugang zu {poolName} gesperrt"
                                        :en-GB "Access to {poolName} is suspended" :fr-CH "L'accès à {poolName} est suspendu"}
                       :some-items-not-available {:de-CH "Die Gegenstände in grauer Schrift sind für das aktuelle Profil nicht reservierbar"
                                                  :en-GB "The items shown in grey font are not reservable for the current profile" :fr-CH "Les éléments affichés en gris ne sont pas réservables pour le profil actuel"}
                       :no-items-available {:de-CH "Diese Vorlage enthält keine Gegenstände, welche für das aktuelle Profil reservierbar sind"
                                            :en-GB "This template does not contain any items that can be reserved for the current profile" :fr-CH "Ce modèle ne contient aucun élément réservable pour le profil actuel"}
                       :apply-button-label {:de-CH "Gegenstände bestellen"
                                            :en-GB "Order items" :fr-CH "Commander les éléments"}}
                :apply {:dialog {:title {:de-CH "Gegenstände hinzufügen" :en-GB "Add items" :fr-CH "Ajouter des éléments"}
                                 :info {:de-CH "{count, plural,
                                                         =1 {Ein Gegenstand wird zum Warenkorb hinzugefügt.}
                                                         other {# Gegenstände werden zum Warenkorb hinzugefügt.}}"
                                        :en-GB "{count, plural,
                                                         =1 {One item will be added to the cart.}
                                                         other {# items will be added to the cart.}}"
                                        :fr-CH "{count, plural,
                                                         =1 {Un élément sera ajouté au panier.}
                                                         other {# éléments seront ajoutés au panier.}}"}
                                 :error-no-items {:de-CH "Keine Gegenstände gefunden"
                                                  :en-GB "No items found"
                                                  :fr-CH "Aucun élément trouvé"}
                                 :timespan {:de-CH "Zeitraum" :en-GB "Time span" :fr-CH "Période"}
                                 :undefined {:de-CH "Unbestimmt" :en-GB "undefined" :fr-CH "Indéfini"}
                                 :from {:de-CH "Von" :en-GB "From" :fr-CH "De"}
                                 :until {:de-CH "Bis" :en-GB "Until" :fr-CH "À"}
                                 :order-for {:de-CH "Bestellung für" :en-GB "Order for" :fr-CH "Commande pour"}
                                 :pool {:de-CH "Inventarpark" :en-GB "Inventory pool" :fr-CH "Pool d'inventaire"}
                                 :cancel {:de-CH "Abbrechen" :en-GB "Cancel" :fr-CH "Annuler"}
                                 :submit {:de-CH "Hinzufügen" :en-GB "Add" :fr-CH "Ajouter"}}
                        :success-notification {:title {:de-CH "Gegenstände hinzugefügt" :en-GB "Items added" :fr-CH "Éléments ajoutés"}
                                               :message {:de-CH "{count, plural,
                                                                          =1 {Ein Gegenstand wurde zum Warenkorb hinzugefügt und kann nun dort überprüft werden.}
                                                                          other {# Gegenstände wurden zum Warenkorb hinzugefügt und können nun dort überprüft werden.}}"
                                                         :en-GB "{count, plural,
                                                                          =1 {One item was added to the cart and can be reviewed/edited there.}
                                                                          other {# items were added to the cart and can be reviewed/edited there.}}"
                                                         :fr-CH "{count, plural,
                                                                          =1 {Un élément a été ajouté au panier et peut y être révisé/modifié.}
                                                                          other {# éléments ont été ajoutés au panier et peuvent y être révisés/modifiés.}}"}
                                               :confirm {:de-CH "Zum Warenkorb" :en-GB "Go to cart" :fr-CH "Aller au panier"}}}}}})
