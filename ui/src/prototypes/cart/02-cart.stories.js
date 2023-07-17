import React from 'react'

import ActionButtonGroup from '../../components/ActionButtonGroup'
import Badge from '../../components/Badge'
import ListCard from '../../components/ListCard'
import PageLayout from '../../components/PageLayout'
import ProgressInfo from '../../components/ProgressInfo'
import Section from '../../components/Section'
import Stack from '../../components/Stack'
import Warning from '../../components/Warning'
import PageLayoutMock from '../../story-utils/PageLayoutMock'

export default {
  title: 'Prototypes/Cart/Cart',
  parameters: { layout: 'fullscreen' },
  argTypes: {
    onResetTimeLimitClick: { action: 'reset-time-limit' },
    onItemClick: { action: 'item-click' },
    onConfirmClick: { action: 'confirm-click' },
    onDeleteClick: { action: 'delete-click' },
    onDelegationChange: { action: 'delegation-change' }
  },
  args: {
    order: {
      models: [
        {
          reservation: {
            id: '958fb184-fd1a-546f-965d-4852cf997563',
            startDate: '26.5.2021',
            endDate: '6.6.2021',
            durationDays: 13,
            quantity: 1,
            isCompleted: false,
            isInvalid: true
          },
          model: {
            id: '40ca9617-f879-5092-8789-b583f8064f9c',
            name: 'ARRI Alexa DTE-SXS Super 35mm'
          },
          pool: {
            id: '8bd16d45-056d-5590-bc7f-12849f034351',
            name: 'Ausleihe Toni-Areal'
          }
        },
        {
          reservation: {
            id: '958fb184-fd1a-546f-965d-4852cf997563',
            startDate: '27.5.2021',
            endDate: '6.6.2021',
            durationDays: 12,
            quantity: 2,
            isCompleted: false
          },
          model: {
            id: '40ca9617-f879-5092-8789-b583f8064f9c',
            name: 'Glasfilter Tiffen 4x4 Pol'
          },
          pool: {
            id: '8bd16d45-056d-5590-bc7f-12849f034351',
            name: 'Ausleihe Toni-Areal'
          }
        },
        {
          reservation: {
            id: '958fb184-fd1a-546f-965d-4852cf997563',
            startDate: '6.6.2021',
            endDate: '8.6.2021',
            durationDays: 3,
            quantity: 1,
            isCompleted: false
          },
          model: {
            id: '40ca9617-f879-5092-8789-b583f8064f9c',
            name: 'Monitor HD LCD Panasonic'
          },
          pool: {
            id: '8bd16d45-056d-5590-bc7f-12849f034351',
            name: 'Werkstattausleihe'
          }
        }
      ]
    },
    user: {
      id: 'a06ec573-d8da-4999-81fa-63226a8b00b7',
      name: 'Anna Beispiel'
    },
    delegations: [
      { id: '37372089-450b-49ec-8486-fcc3a9e6ae22', name: 'Delegation 1' },
      { id: '3013ff5a-0203-4ec5-bda5-61871ddd5dc7', name: 'Delegation 2' }
    ],
    errorMessage: '1 Gegenstand ungültig'
  }
}

export const cart = ({ order, onResetTimeLimitClick, onItemClick, onConfirmClick, onDeleteClick, errorMessage }) => {
  function confirmClick() {
    onConfirmClick({})
  }

  return (
    <PageLayoutMock>
      <PageLayout.Header title="Warenkorb"></PageLayout.Header>

      <Stack space="5">
        <Section title="Status" collapsible>
          <Stack space="3">
            <ProgressInfo title="Zeitlimit" info="Noch 23 Minuten übrig" totalCount="30" doneCount="23" />
            <ActionButtonGroup>
              <button type="button" className="btn btn-secondary" onClick={onResetTimeLimitClick}>
                Zeitlimit zurückstellen
              </button>
            </ActionButtonGroup>
          </Stack>
        </Section>

        <Section title="Bestellung für" collapsible>
          <div className="fw-bold">Anna Beispiel (persönlich)</div>
        </Section>

        <Section title="Gegenstände" collapsible className="position-relative">
          <ListCard.Stack>
            {order.models.map(({ reservation, model, pool }, i) => (
              <ListCard key={i} onClick={() => onItemClick(reservation.id)}>
                <ListCard.Title>
                  {reservation.quantity}x {model.name}
                </ListCard.Title>
                <ListCard.Body>{pool.name}</ListCard.Body>
                <ListCard.Foot>
                  <Badge colorClassName={reservation.isInvalid ? 'bg-danger' : undefined}>
                    {reservation.durationDays} Tage{' '}
                    {reservation.isCompleted ? `bis ${reservation.endDate}` : `ab ${reservation.startDate}`}
                  </Badge>
                </ListCard.Foot>
              </ListCard>
            ))}
          </ListCard.Stack>
        </Section>

        <Section>
          <ActionButtonGroup>
            {errorMessage && <Warning>{errorMessage}</Warning>}
            <button type="submit" className="btn btn-primary" disabled={errorMessage} onClick={confirmClick}>
              Bestellung abschicken
            </button>
            <button type="button" className="btn btn-secondary" onClick={onDeleteClick}>
              Warenkorb löschen
            </button>
          </ActionButtonGroup>
        </Section>
      </Stack>
    </PageLayoutMock>
  )
}
