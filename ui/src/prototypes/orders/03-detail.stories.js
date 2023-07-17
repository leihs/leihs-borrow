import React from 'react'
import { action } from '@storybook/addon-actions'
import Stack from '../../components/Stack'
import Section from '../../components/Section'
import ListCard from '../../components/ListCard'
import PageLayout from '../../components/PageLayout'
import ActionButtonGroup from '../../components/ActionButtonGroup'
import Badge from '../../components/Badge'
import ProgressInfo from '../../components/ProgressInfo'
import PageLayoutMock from '../../story-utils/PageLayoutMock'

export default {
  title: 'Prototypes/Orders/Detail',
  parameters: { layout: 'fullscreen' }
}

export const detail = ({ order, onOrderCancelClick, onItemClick }) => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title={order.title} subTitle="24 Tage ab 6.5.2020, 11 Gegenstände"></PageLayout.Header>

      <Stack space="5">
        <Section title="Status" collapsible>
          <Stack space="3">
            <div>
              {order.stateGroups.map((stateGroup, i) => (
                <ProgressInfo key={i} {...stateGroup} />
              ))}
            </div>
            {order.isCancellable && (
              <ActionButtonGroup>
                <button type="button" className="btn btn-secondary" onClick={() => onOrderCancelClick()}>
                  Bestellung stornieren
                </button>
              </ActionButtonGroup>
            )}
          </Stack>
        </Section>

        <Section title="Bestellung für" collapsible>
          <div className="fw-bold">
            {order.delegation.name}
            {order.delegation.isUser && ' (persönlich)'}
          </div>
        </Section>

        <Section title="Zweck" collapsible>
          <div className="fw-bold">{order.purpose}</div>
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
                  <Badge>
                    {reservation.durationDays} Tage{' '}
                    {reservation.isCompleted ? `bis ${reservation.endDate}` : `ab ${reservation.startDate}`}
                  </Badge>
                </ListCard.Foot>
              </ListCard>
            ))}
          </ListCard.Stack>
        </Section>
      </Stack>

      <PageLayout.Metadata>ID {order.id}</PageLayout.Metadata>
    </PageLayoutMock>
  )
}

const sampleOrder = {
  // Primary data (same structure in list entries):
  id: '15874', // TODO: use friendly id if available
  title: 'Video Semesterprojekt',
  purpose: 'Material für das Video Semesterprojekt bei Prof. Zimmer',
  startDate: '6.5.2020',
  endDate: '30.6.2020',
  durationDays: 24,
  modelCount: 11,
  isCompleted: false,
  stateGroups: [
    {
      title: 'In der Genehmigung',
      info: '1 von 3 Inventarparks genehmigt',
      totalCount: 3,
      doneCount: 1
    }
  ],
  // Details:
  isCancellable: true,
  pools: [
    {
      id: '8bd16d45-056d-5590-bc7f-12849f034351',
      name: 'Ausleihe Toni-Areal',
      stateGroups: [
        {
          title: 'Abholung',
          info: '0 von 4 Gegenständen abgeholt',
          totalCount: 4,
          doneCount: 0
        }
      ]
    },
    {
      id: '5dd25b58-fa56-5095-bd97-2696d92c2fb1',
      name: 'Ausleihe Werkstatt',
      stateGroups: [
        {
          title: 'Genehmigung',
          info: '0 von 4 Gegenständen genehmigt',
          totalCount: 4,
          doneCount: 0
        }
      ]
    }
  ],
  models: [
    {
      reservation: {
        id: '958fb184-fd1a-546f-965d-4852cf997563',
        startDate: '26.5.2021',
        endDate: '6.6.2021',
        durationDays: 13,
        quantity: 1,
        isCompleted: false
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
  ],
  delegation: {
    id: 'a06ec573-d8da-4999-81fa-63226a8b00b7',
    name: 'Anna Beispiel',
    isUser: true
  },
  documents: [
    {
      id: '1111c573-d8da-4999-81fa-63226a8b00b7',
      name: 'Werteverzeichnis',
      url: '/borrow/path-to-document'
    }
  ]
}

detail.args = {
  order: sampleOrder,
  onOrderCancelClick: action('order-cancel'),
  onItemClick: action('item-click')
}
