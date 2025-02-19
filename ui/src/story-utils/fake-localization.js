export const locale = 'de-CH'
export const orderPanelTexts = {
  label: {
    quantity: { 'de-CH': 'Anzahl' },
    minus: { 'de-CH': 'Minus' },
    plus: { 'de-CH': 'Plus' },
    pool: { 'de-CH': 'Inventarpark' },
    'pool-max-amount': { 'de-CH': '{pool} (max. {amount, number})' },
    'pool-max-amount-info': { 'de-CH': 'Maximal verfügbarer Bestand: {amount, number}' },
    'user-delegation': { 'de-CH': 'Delegation' },
    timespan: { 'de-CH': 'Zeitraum' },
    from: { 'de-CH': 'Von' },
    until: { 'de-CH': 'Bis' },
    undefined: { 'de-CH': 'Unbestimmt' },
    'show-day-quants': { 'de-CH': 'Verfügbarkeit im Kalender anzeigen' }
  },
  validate: {
    'missing-quantity': { 'de-CH': 'Verfügbarkeit kann nicht geprüft werden, da die Anzahl fehlt' },

    'invalid-start-date': { 'de-CH': 'Ungültiges Beginndatum' },
    'invalid-end-date': { 'de-CH': 'Ungültiges Enddatum' },
    'start-after-end': { 'de-CH': 'Enddatum muss nach Beginndatum sein' },
    'end-date-too-late': { 'de-CH': 'Datum darf nicht nach {maxDate, date, small} sein' },

    'start-date-in-past': { 'de-CH': 'Abholdatum liegt in der Vergangenheit' },
    'start-date-not-before': {
      'de-CH': '{days, plural, =1 {Abholung frühestens morgen} other {Abholung frühestens heute in # Arbeitstagen}}'
    },

    'quantity-to-large-at-day': {
      'de-CH': 'Gegenstand ist am {startDate, date, small} nicht in der gewünschten Menge verfügbar'
    },
    'quantity-to-large-in-range': {
      'de-CH': 'Gegenstand ist in diesem Zeitraum nicht in der gewünschten Menge verfügbar'
    },

    'pool-closed-at-start-date': { 'de-CH': 'Abholung am {startDate, date, small} nicht möglich' },
    'pool-closed-at-end-date': { 'de-CH': 'Rückgabe am {endDate, date, small} nicht möglich' },
    'pool-closed-at-start-and-end-date': { 'de-CH': 'Abholung/Rückgabe am {startDate, date, small} nicht möglich' },
    'pool-closed-max-visits': { 'de-CH': ' (maximale Besucherzahl erreicht)' },

    'maximum-reservation-duration': { 'de-CH': 'Maximale Reservationsdauer ist beschränkt auf {days} Tage' },

    'no-pool-access': { 'de-CH': 'Keine Berechtigung für diesen Inventarpark' },
    'pool-suspension': { 'de-CH': 'Sperre für diesen Inventarpark' },
    'item-not-available-in-pool': { 'de-CH': 'Gegenstand in diesem Inventarpark nicht verfügbar' },
    'unknown-pool': { 'de-CH': 'Unbekannter Inventarpark' }
  }
}
