import React, { useState } from 'react'
import { action } from '@storybook/addon-actions'
import { de } from 'date-fns/locale'
import { addYears, isAfter, startOfToday, startOfTomorrow } from 'date-fns'

import Section from '../../components/Section'
import InputWithClearButton from '../../components/InputWithClearButton'
import ModalDialog from '../../components/ModalDialog'
import Stack from '../../components/Stack'
import Warning from '../../components/Warning'
import DateRangePicker from '../../components/DateRangePicker'
import MinusPlusControl from '../../components/MinusPlusControl'

export default {
  title: 'Prototypes/Catalog/Filter',
  parameters: {
    layout: 'fullscreen'
  },
  args: {
    onSubmit: action('apply'),
    onDismiss: action('dismiss')
  }
}

export const filter = ({ onSubmit, onDismiss }) => {
  // data
  const pools = [
    { id: '8bd16d45-056d-5590-bc7f-12849f034351', name: 'Ausleihe Toni-Areal' },
    { id: '5863967f-9804-4909-a9b7-92254616d6b2', name: 'FM-Inventar' },
    { id: 'a02b8163-9a16-5066-b48e-9eb74cf8b791', name: 'Fundus-Dok' },
    { id: '5dd25b58-fa56-5095-bd97-2696d92c2fb1', name: 'IT-Zentrum' },
    { id: 'ffaa3aea-2a1f-4d6a-bdfc-f3be93699750', name: 'ITZ Occasions-Shop' },
    { id: '3977012c-ce0e-501f-889b-8715fdb5d83b', name: 'ITZ Software' }
  ]

  const initialTerm = 'Mikrofon'
  const initialRange = { startDate: startOfToday(), endDate: startOfTomorrow() }
  const initialPoolId = '8bd16d45-056d-5590-bc7f-12849f034351'

  // env
  const locale = de

  // state
  const [term, setTerm] = useState(initialTerm)
  const [poolId, setPoolId] = useState(initialPoolId)
  const [onlyAvailable, setOnlyAvailable] = useState(false)
  const [selectedRange, setSelectedRange] = useState(initialRange)
  const [quantity, setQuantity] = useState(1)

  // validation
  const isEndDateBeforeStartDate =
    selectedRange.startDate && selectedRange.endDate && isAfter(selectedRange.startDate, selectedRange.endDate)

  // actions
  function submit(e) {
    e.preventDefault()
    onSubmit({ term, poolId, ...selectedRange, quantity })
  }
  function clear() {
    setTerm('')
    setPoolId('')
    setOnlyAvailable(false)
    setSelectedRange(initialRange)
    setQuantity(1)
  }

  return (
    <ModalDialog title="Filter" shown dismissible onDismiss={onDismiss}>
      <ModalDialog.Body>
        <form onSubmit={submit} noValidate autoComplete="off" id="model-filter-form">
          <Stack space="4">
            <Section title="Stichwort">
              <label htmlFor="term" className="visually-hidden">
                Stichwort
              </label>
              <InputWithClearButton
                name="term"
                id="term"
                placeholder="Suchbegriff eingeben"
                value={term}
                onChange={e => setTerm(e.target.value)}
              />
            </Section>
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
                  <Stack space="3">
                    <DateRangePicker
                      locale={locale}
                      selectedRange={selectedRange}
                      onChange={r => setSelectedRange(r)}
                      minDate={startOfToday()}
                      maxDate={addYears(startOfToday(), 1)}
                    />
                    {isEndDateBeforeStartDate && <Warning>Bis-Datum ist vor Von-Datum</Warning>}
                  </Stack>
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
            <Section title="Inventarparks">
              <div className="d-flex flex-column gap-3">
                <div>
                  <label htmlFor="pool-id" className="visually-hidden">
                    Inventarparks
                  </label>
                  <select
                    className="form-select"
                    name="pool-id"
                    id="pool-id"
                    value={poolId}
                    onChange={e => setPoolId(e.target.value)}
                  >
                    <option value="all" key="all">
                      Alle Inventarparks
                    </option>
                    {pools.map(p => (
                      <option value={p.id} key={p.id}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </Section>
          </Stack>
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
