import React, { useState } from 'react'
import { action } from '@storybook/addon-actions'
import { de } from 'date-fns/locale'
import { addYears, isAfter, startOfToday, startOfTomorrow } from 'date-fns'

import Section from '../../components/Section'
import ModalDialog from '../../components/ModalDialog'
import Warning from '../../components/Warning'
import DateRangePicker from '../../components/DateRangePicker'
import MinusPlusControl from '../../components/MinusPlusControl'

export default {
  title: 'Prototypes/Catalog/AvailabilityFilter',
  parameters: {
    layout: 'fullscreen'
  },
  args: {
    onSubmit: action('apply'),
    onDismiss: action('dismiss')
  }
}

export const availabilityFilter = ({ onSubmit, onDismiss }) => {
  const initialRange = { startDate: startOfToday(), endDate: startOfTomorrow() }

  // env
  const locale = de

  // state
  const [onlyAvailable, setOnlyAvailable] = useState(true)
  const [selectedRange, setSelectedRange] = useState(initialRange)
  const [quantity, setQuantity] = useState(1)

  // validation
  const isEndDateBeforeStartDate =
    selectedRange.startDate && selectedRange.endDate && isAfter(selectedRange.startDate, selectedRange.endDate)

  // actions
  function submit(e) {
    e.preventDefault()
    onSubmit({ ...selectedRange, quantity })
  }
  function clear() {
    setOnlyAvailable(false)
    setSelectedRange(initialRange)
    setQuantity(1)
  }

  return (
    <ModalDialog title="Filter Verfügbarkeit" shown dismissible onDismiss={onDismiss}>
      <ModalDialog.Body>
        <form onSubmit={submit} noValidate autoComplete="off" id="model-filter-form">
          <div className="d-grid gap-4">
            <Section title="Verfügbarkeit">
              <div className="form-check form-switch mb-3">
                <input
                  type="checkbox"
                  className="form-check-input"
                  name="only-available"
                  id="only-available"
                  checked={onlyAvailable}
                  onChange={e => setOnlyAvailable(e.target.checked)}
                />
                <label htmlFor="only-available" className="form-check-label">
                  Datum wählen (von/bis)
                </label>
              </div>
              {onlyAvailable && (
                <fieldset>
                  <legend className="visually-hidden">Zeitraum</legend>
                  <div className="d-grid gap-3">
                    <DateRangePicker
                      locale={locale}
                      selectedRange={selectedRange}
                      onChange={r => setSelectedRange(r)}
                      minDate={startOfToday()}
                      maxDate={addYears(startOfToday(), 1)}
                    />
                    {isEndDateBeforeStartDate && <Warning>Bis-Datum ist vor Von-Datum</Warning>}
                  </div>
                </fieldset>
              )}
            </Section>
            {onlyAvailable && (
              <Section title="Anzahl">
                <MinusPlusControl
                  name="quantity"
                  id="quantity"
                  value={quantity}
                  min={1}
                  onChange={q => setQuantity(q)}
                />
              </Section>
            )}
          </div>
        </form>
      </ModalDialog.Body>
      <ModalDialog.Footer>
        <button type="submit" className="btn btn-primary" form="model-filter-form">
          Anwenden
        </button>
        <button type="button" onClick={clear} className="btn btn-secondary" form="model-filter-form">
          Zurücksetzen
        </button>
      </ModalDialog.Footer>
    </ModalDialog>
  )
}
