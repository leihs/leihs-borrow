import React, { useState } from 'react'
import { action } from '@storybook/addon-actions'
import { de } from 'date-fns/locale'
import { isAfter, parse } from 'date-fns'

import Section from '../../components/Section'
import InputWithClearButton from '../../components/InputWithClearButton'
import ModalDialog from '../../components/ModalDialog'
import Stack from '../../components/Stack'
import DatePicker from '../../components/DatePicker'
import Warning from '../../components/Warning'

export default {
  title: 'Prototypes/Orders/Filter',
  parameters: {
    layout: 'fullscreen'
  },
  args: {
    onSubmit: action('submit')
  }
}

export const filter = ({ onSubmit }) => {
  // data
  const pools = [
    { id: '8bd16d45-056d-5590-bc7f-12849f034351', name: 'Ausleihe Toni-Areal' },
    { id: '5863967f-9804-4909-a9b7-92254616d6b2', name: 'FM-Inventar' },
    { id: 'a02b8163-9a16-5066-b48e-9eb74cf8b791', name: 'Fundus-Dok' },
    { id: '5dd25b58-fa56-5095-bd97-2696d92c2fb1', name: 'IT-Zentrum' },
    { id: 'ffaa3aea-2a1f-4d6a-bdfc-f3be93699750', name: 'ITZ Occasions-Shop' },
    { id: '3977012c-ce0e-501f-889b-8715fdb5d83b', name: 'ITZ Software' }
  ]
  const orderStates = [
    { id: 'inApproval', name: 'In der Genehmigung' },
    { id: 'inHandOver', name: 'Bereit zur Abholung' },
    { id: 'lent', name: 'Ausgeliehen' },
    { id: 'inReturn', name: 'Teilweise zurückgebracht' },
    { id: 'allItemsReturned', name: 'Alle Gegenstände zurückgebracht' },
    { id: 'allItemsRejected', name: 'Abgelehnt' },
    { id: 'transferred', name: 'Übertragen' }
  ]
  const user = {
    id: 'a06ec573-d8da-4999-81fa-63226a8b00b7',
    name: 'Anna Beispiel'
  }
  const initialTerm = 'Mikrofon'
  const initialStartDate = ''
  const initialEndDate = ''
  const initialPoolId = '8bd16d45-056d-5590-bc7f-12849f034351'
  const initialDelegationId = 'a06ec573-d8da-4999-81fa-63226a8b00b7'
  const initialOrderState = ''

  // env
  const locale = de

  // state
  const [term, setTerm] = useState(initialTerm)
  const [startDate, setStartDate] = useState(initialStartDate) // localized string
  const [endDate, setEndDate] = useState(initialEndDate) // localized string
  const [poolId, setPoolId] = useState(initialPoolId)
  const [delegationId, setDelegationId] = useState(initialDelegationId)
  const [orderState, setOrderState] = useState(initialOrderState)

  // validation
  const parseDate = s => parse(s, 'P', new Date(), { locale: de })
  const isEndDateBeforeStartDate = startDate && endDate && isAfter(parseDate(startDate), parseDate(endDate))

  // actions
  function submit(e) {
    e.preventDefault()
    onSubmit({ term, startDate, endDate, poolId, delegationId, orderState })
  }
  function clear() {
    setTerm('')
    setStartDate('')
    setEndDate('')
    setPoolId('')
    setDelegationId(user.id)
    setOrderState('')
  }

  return (
    <ModalDialog title="Filter" shown>
      <ModalDialog.Body>
        <form action="/search" onSubmit={submit} autoComplete="off" id="order-filter-form">
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
            <Section title="Status">
              <label htmlFor="order-state" className="visually-hidden">
                Status
              </label>
              <select
                className="form-select"
                name="order-state"
                id="order-state"
                value={orderState}
                onChange={e => setOrderState(e.target.value)}
              >
                <option value="all" key="all">
                  Alle
                </option>
                {orderStates.map(p => (
                  <option value={p.id} key={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </Section>
            <Section title="Zeitraum">
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
            <Section title="Inventarpark">
              <label htmlFor="pool-id" className="visually-hidden">
                Inventarpark
              </label>
              <select
                className="form-select"
                name="pool-id"
                id="pool-id"
                value={poolId}
                onChange={e => setPoolId(e.target.value)}
              >
                <option value="all" key="all">
                  Alle
                </option>
                {pools.map(p => (
                  <option value={p.id} key={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </Section>
          </Stack>
        </form>
      </ModalDialog.Body>
      <ModalDialog.Footer>
        <button type="submit" onClick={submit} className="btn btn-primary" form="order-filter-form">
          Anwenden
        </button>
        <button type="button" onClick={clear} className="btn btn-secondary" form="order-filter-form">
          Zurücksetzen
        </button>
      </ModalDialog.Footer>
    </ModalDialog>
  )
}
