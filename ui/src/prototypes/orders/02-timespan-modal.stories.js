import React, { useState } from 'react'
import { action } from '@storybook/addon-actions'
import { de } from 'date-fns/locale'
import { isAfter, parse } from 'date-fns'

import Section from '../../components/Section'
import ModalDialog from '../../components/ModalDialog'
import DatePicker from '../../components/DatePicker'
import Warning from '../../components/Warning'

export default {
  title: 'Prototypes/Orders/Timespan Modal',
  args: {
    onSubmit: action('submit')
  }
}

export const timespanModal = ({ onSubmit }) => {
  const initialStartDate = ''
  const initialEndDate = ''

  // env
  const locale = de

  // state
  const [shown, setShown] = useState(false)
  const [startDate, setStartDate] = useState(initialStartDate) // localized string
  const [endDate, setEndDate] = useState(initialEndDate) // localized string

  // validation
  const parseDate = s => parse(s, 'P', new Date(), { locale: de })
  const isEndDateBeforeStartDate = startDate && endDate && isAfter(parseDate(startDate), parseDate(endDate))

  // actions
  function submit(e) {
    e.preventDefault()
    onSubmit({ startDate, endDate })
    setShown(false)
  }
  function cancel() {
    setShown(false)
  }

  return (
    <>
      <button type="button" className="btn btn-secondary" onClick={() => setShown(true)}>
        Open Modal
      </button>
      <ModalDialog title="Zeitraum" shown={shown} onDismiss={cancel}>
        <ModalDialog.Body>
          <form action="/search" onSubmit={submit} autoComplete="off" id="order-filter-form">
            <div className="d-grid gap-4">
              <Section>
                <fieldset>
                  <legend className="visually-hidden">Zeitraum</legend>
                  <div className="d-flex flex-column gap-3">
                    <DatePicker
                      locale={locale}
                      name="start-date"
                      id="start-date"
                      value={startDate}
                      onChange={e => setStartDate(e.target.value)}
                      placeholder="Unbestimmt"
                      label={<label htmlFor="start-date">Von</label>}
                    />
                    <DatePicker
                      locale={locale}
                      name="end-date"
                      id="end-date"
                      value={endDate}
                      onChange={e => setEndDate(e.target.value)}
                      placeholder="Unbestimmt"
                      label={<label htmlFor="end-date">Bis</label>}
                    />
                    {isEndDateBeforeStartDate && <Warning>Bis-Datum ist vor Von-Datum</Warning>}
                  </div>
                </fieldset>
              </Section>
            </div>
          </form>
        </ModalDialog.Body>
        <ModalDialog.Footer>
          <button type="submit" onClick={submit} className="btn btn-primary" form="order-filter-form">
            Anwenden
          </button>
          <button type="button" onClick={cancel} className="btn btn-secondary" form="order-filter-form">
            Abbrechen
          </button>
        </ModalDialog.Footer>
      </ModalDialog>
    </>
  )
}
