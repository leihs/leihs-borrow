import React from 'react'
import PageLayoutMock from '../../story-utils/PageLayoutMock'
import PageLayout from '../../components/PageLayout'
import ListCard from '../../components/ListCard'
import OrderSearchFilter from '../../features/OrderSearchFilter'
import { orderSearchFilterProps } from '../../story-utils/sample-props'
import Tabs from 'react-bootstrap/Tabs'
import Tab from 'react-bootstrap/Tab'
import OrderCard from './OrderCard'
import ReservationCard from './ReservationCard'
import SquareImage from '../../components/SquareImage'
import CircleBadge from '../../components/CircleBadge'

export default {
  title: 'Prototypes/Orders/List',
  parameters: { layout: 'fullscreen' },
  argTypes: {}
}

export const list = ({ ordersByBasicState }) => {
  const openOrders = ordersByBasicState.find(x => x.basicState.key === 'open').orders
  const closedOrders = ordersByBasicState.find(x => x.basicState.key === 'closed').orders
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Bestellungen"></PageLayout.Header>

      <div className="d-grid gap-4">
        <OrderSearchFilter {...orderSearchFilterProps} />

        <div className="responsive-tab-combo">
          <div>
            <select className="form-select tab-select">
              <option>(Responsive Tab Selector - not implemented here)</option>
            </select>
          </div>
          <Tabs defaultActiveKey="current-lendings">
            <Tab
              eventKey="current-lendings"
              title={
                <span>
                  Aktuelle Ausleihen{' '}
                  <CircleBadge variant="secondary" inline>
                    4
                  </CircleBadge>
                </span>
              }
            >
              <ListCard.Stack>
                <ReservationCard
                  img={<SquareImage />}
                  quantity="1"
                  modelName="4K-Videokamera Sony PXW-Z90"
                  inventoryCode="P-AUS476488"
                  poolName="Ausleihe Toni-Areal"
                  startDate="15.10."
                  endDate="20.1.2024"
                  durationDays="6"
                  statusInfo={<span className="text-danger">Rückgabe überfällig</span>}
                  onClick={() => {}}
                />
                <ReservationCard
                  img={<SquareImage />}
                  quantity="1"
                  modelName="4K-Videokamera Sony PXW-Z90"
                  inventoryCode="P-AUS476488"
                  poolName="Ausleihe Toni-Areal"
                  startDate="15.10."
                  endDate="20.1.2024"
                  durationDays="6"
                  delegationName="Delegation TZ-DDE-Cast/Audioviselle Medien"
                  statusInfo={<span className="text-warning">Rückgabe morgen</span>}
                  onClick={() => {}}
                />
                <ReservationCard
                  img={<SquareImage />}
                  quantity="1"
                  modelName="Arri HMI 400w Pocket Par mit Softbox Chimera XS Video Pro 40x55cm (Arri HMI 400w Pocket Par)"
                  inventoryCode="INV61969"
                  poolName="Ausleihe Toni-Areal"
                  startDate="21.10."
                  endDate="23.1.2024"
                  durationDays="3"
                  statusInfo={<span className="text-primary">Abholung morgen</span>}
                  onClick={() => {}}
                />
                <ReservationCard
                  img={<SquareImage />}
                  quantity="1"
                  modelName="Stativ Manfrotto 298 B"
                  inventoryCode="INV39722"
                  poolName="Ausleihe Toni-Areal"
                  startDate="26.10."
                  endDate="29.11.2024"
                  durationDays="3"
                  statusInfo={<span className="">Abholung in 5 Tagen</span>}
                  onClick={() => {}}
                />
              </ListCard.Stack>
            </Tab>
            <Tab
              eventKey="open-orders"
              title={
                <span>
                  Aktive Bestellungen{' '}
                  <CircleBadge variant="secondary" inline>
                    {openOrders.length}
                  </CircleBadge>
                </span>
              }
            >
              <ListCard.Stack>
                {openOrders.map(order => (
                  <OrderCard key={order.id} order={order} orderLink={`/rentals/${order.id}`} />
                ))}
              </ListCard.Stack>
            </Tab>
            <Tab
              eventKey="closed-orders"
              title={
                <span>
                  Abgeschlossene Bestellungen{' '}
                  <CircleBadge variant="secondary" inline>
                    {closedOrders.length}
                  </CircleBadge>
                </span>
              }
            >
              <ListCard.Stack>
                {closedOrders.map(order => (
                  <OrderCard key={order.id} order={order} orderLink={`/rentals/${order.id}`} />
                ))}
              </ListCard.Stack>
            </Tab>
          </Tabs>
        </div>
      </div>
    </PageLayoutMock>
  )
}

const ordersByBasicState = [
  {
    basicState: { key: 'open', label: 'Offen' },
    orders: [
      {
        id: '98510838-a6ec-4752-823e-000000000001',
        title: 'Video Semesterprojekt',
        purpose: 'Material für das Video Semesterprojekt bei Prof. Zimmer',
        startDate: '6.5.2020',
        endDate: '30.6.2020',
        durationDays: 24,
        modelCount: 11,
        isCompleted: false,
        stateGroups: [
          {
            title: 'Genehmigung',
            info: '1 von 3 Inventarparks genehmigt',
            totalCount: 3,
            doneCount: 1
          }
        ]
      },
      {
        id: '98510838-a6ec-4752-823e-000000000002',
        title: 'Werkschau 2021',
        purpose: '',
        startDate: '15.3.2020',
        endDate: '23.3.2020',
        durationDays: 8,
        modelCount: 7,
        isCompleted: false,
        stateGroups: [
          {
            title: 'Abholung',
            info: '4 von 7 Gegenständen abgeholt',
            totalCount: 7,
            doneCount: 4
          }
        ]
      },
      {
        id: '98510838-a6ec-4752-823e-000000000003',
        title: 'Siebdruck Material',
        purpose: '',
        startDate: '18.2.2020',
        endDate: '10.4.2020',
        durationDays: 54,
        modelCount: 4,
        isCompleted: false,
        stateGroups: [
          {
            title: 'Abholung',
            info: '3 von 4 Gegenständen abgeholt',
            totalCount: 4,
            doneCount: 3
          },
          {
            title: 'Rückgabe',
            info: '2 von 4 Gegenständen zurückgebracht',
            totalCount: 4,
            doneCount: 2
          }
        ]
      },
      {
        id: '98510838-a6ec-4752-823e-000000000004',
        title: 'Freitags Experiment',
        purpose: '',
        startDate: '12.2.2020',
        endDate: '10.4.2020',
        durationDays: 2,
        modelCount: 3,
        isCompleted: false,
        stateGroups: [
          {
            title: 'Rückgabe',
            info: '2 von 3 Gegenständen zurückgebracht',
            totalCount: 3,
            doneCount: 2
          }
        ]
      }
    ]
  },
  {
    basicState: { key: 'closed', label: 'Abgeschlossen' },
    orders: [
      {
        id: '98510838-a6ec-4752-823e-000000000005',
        title: 'Diplomausstellung 2019',
        purpose: '',
        startDate: '1.4.2019',
        endDate: '12.6.2019',
        durationDays: 72,
        modelCount: 16,
        isCompleted: true,
        stateGroups: [
          {
            title: 'Alle Gegenstände zurückgebracht'
          }
        ]
      },
      {
        id: '98510838-a6ec-4752-823e-000000000006',
        title: 'Semesterfest',
        purpose: '',
        startDate: '23.2.2019',
        endDate: '24.2.2019',
        durationDays: 2,
        modelCount: 1,
        isCompleted: true,
        stateGroups: [
          {
            title: 'Bestellung wurde abgelehnt'
          }
        ]
      },
      {
        id: '98510838-a6ec-4752-823e-000000000007',
        title: 'Kurs Präsentation',
        purpose: '',
        startDate: '16.7.2018',
        endDate: '17.7.2018',
        durationDays: 2,
        modelCount: 1,
        isCompleted: true,
        stateGroups: [
          {
            title: 'An “Reto Brunner” übertragen'
          }
        ]
      }
    ]
  }
]

list.args = {
  ordersByBasicState
}
