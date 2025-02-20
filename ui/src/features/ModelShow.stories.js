import React from 'react'
import { action } from '@storybook/addon-actions'
import PageLayoutMock from '../story-utils/PageLayoutMock'
import ModelShow from './ModelShow'

export default {
  title: 'Feature Components/Model Show',
  parameters: { layout: 'fullscreen', storyshots: { disable: true } },
  component: ModelShow
}

export const modelShow = ({ model, onClickFavorite, onOrderClick, orderPanelTmp }) => {
  return (
    <PageLayoutMock>
      <ModelShow
        model={model}
        onOrderClick={onOrderClick}
        onClickFavorite={onClickFavorite}
        orderPanelTmp={orderPanelTmp}
        isAddButtonEnabled={true}
        isFavoriteButtonEnabled={true}
        t={{
          addItemToCart: 'Gegenstand hinzufügen',
          addToFavorites: 'Zu Favoriten hinzufügen',
          removeFromFavorites: 'Von Favoriten entfernen',
          description: 'Beschreibung',
          properties: 'Eigenschaften',
          documents: 'Dokumente',
          compatibles: 'Ergänzende Gegenstände',
          previousImage: 'Vorheriges Bild',
          nextImage: 'Nächstes Bild'
        }}
      />
    </PageLayoutMock>
  )
}

const model = {
  id: '2a893525-9883-4193-9fec-09f070673a2d',
  name: 'Audio-Mischpult Behringer XENYX Q1204USB',
  manufacturer: 'Behringer',
  description:
    'Das XENYX Q1204USB von Behringer ist ein vielseitig einsetzbares Audio Mischpult\n\nLine breaks are preserved. HTML is interpreted, e.g. links: <a href="https://zhdk.ch">zhdk.ch</a>',
  isFavorited: false,
  images: [
    {
      id: '4f64adb0-6325-4eb1-bf36-8a73a986ed4b',
      imageUrl: require('../../static/example-images/models/4f64adb0-6325-4eb1-bf36-8a73a986ed4b.jpg')
    },
    {
      id: 'a7991363-b891-492d-b727-8d026e3a8b78',
      imageUrl: require('../../static/example-images/models/a7991363-b891-492d-b727-8d026e3a8b78.png')
    },
    {
      id: 'fefad5b3-a080-43aa-9cbb-9a544611fc1a',
      imageUrl: require('../../static/example-images/models/fefad5b3-a080-43aa-9cbb-9a544611fc1a.png')
    }
  ],
  attachments: [
    {
      id: '3a05e6be-77ec-47c4-81cf-aa2dc2719c0e',
      filename: 'QX1204USB_Q1204USB_QSG_WW.pdf',
      attachmentUrl: '/borrow/attachments/3a05e6be-77ec-47c4-81cf-aa2dc2719c0e/QX1204USB_Q1204USB_QSG_WW.pdf',
      size: 3379663
    },
    {
      id: '3a05e6be-77ec-47c4-81cf-aa2dc2719c0f',
      filename: 'QX1204USB_Q1204USB_QSG_ZZ.pdf',
      attachmentUrl: '/borrow/attachments/3a05e6be-77ec-47c4-81cf-aa2dc2719c0e/QX1204USB_Q1204USB_QSG_WW.pdf',
      size: 3379663
    }
  ],
  properties: [
    { id: '793d92e1-c12c-4763-ba87-24c8d1637cea', key: 'Kanäle', value: '4 x mic/line, 2x stereo' },
    { id: 'a45ab0bf-8bfa-4121-99ed-a01797232e53', key: 'Gewicht', value: '2,8kg' }
  ],
  recommends: {
    edges: [
      {
        node: {
          id: 'afaa0e9f-4a96-5094-8a57-ea9289df8b95',
          name: 'Aktivlautsprecher-Paar Genelec 8020',
          manufacturer: null,
          images: [
            {
              id: 'c526e47e-020f-5c8b-88f2-a740797b67e0',
              imageUrl: require('../../static/example-images/models/c526e47e-020f-5c8b-88f2-a740797b67e0.jpg')
            }
          ]
        }
      },
      {
        node: {
          id: 'dda9141c-ccd5-5ba7-930c-a503671432e5',
          name: 'Mikrofon Gesang Shure SM58',
          manufacturer: 'Shure',
          images: [
            {
              id: '811f9d50-5915-5f2b-8a66-9ffb9b8a7dfe',
              imageUrl: require('../../static/example-images/models/811f9d50-5915-5f2b-8a66-9ffb9b8a7dfe.jpg')
            }
          ],
          isFavorited: true
        }
      },
      {
        node: {
          id: 'a0f3cdd9-1f1c-5f02-964d-9c175cb46b9a',
          name: 'Mikrofon Shure Beta 57A',
          manufacturer: 'Shure',
          images: []
        }
      },
      {
        node: {
          id: '29072a73-a4c7-529c-8ccd-997b0ea7ab55',
          name: 'Subwoofer Genelec 7050 aktiv',
          manufacturer: 'Genelec',
          images: []
        }
      }
    ]
  },
  totalReservableQuantities: [
    {
      inventoryPool: {
        id: '3977012c-ce0e-501f-889b-8715fdb5d83b'
      },
      quantity: 0
    },
    {
      inventoryPool: {
        id: '4a1ba40c-467e-5efe-8cf1-e8d3dbb59f04'
      },
      quantity: 0
    },
    {
      inventoryPool: {
        id: '5863967f-9804-4909-a9b7-92254616d6b2'
      },
      quantity: 0
    },
    {
      inventoryPool: {
        id: '5dd25b58-fa56-5095-bd97-2696d92c2fb1'
      },
      quantity: 0
    },
    {
      inventoryPool: {
        id: '77e88ec8-9ff6-5435-818e-dc902fc631a6'
      },
      quantity: 0
    },
    {
      inventoryPool: {
        id: '8bd16d45-056d-5590-bc7f-12849f034351'
      },
      quantity: 6
    },
    {
      inventoryPool: {
        id: 'a02b8163-9a16-5066-b48e-9eb74cf8b791'
      },
      quantity: 0
    },
    {
      inventoryPool: {
        id: 'e3dadec7-12a2-52f6-8d6f-475ea5d74ee7'
      },
      quantity: 0
    },
    {
      inventoryPool: {
        id: 'ffaa3aea-2a1f-4d6a-bdfc-f3be93699750'
      },
      quantity: 0
    }
  ],
  availability: [
    {
      inventoryPool: {
        id: '8bd16d45-056d-5590-bc7f-12849f034351',
        name: 'Ausleihe Toni-Areal'
      },
      dates: [
        {
          date: '2021-06-23T00:00:00Z',
          quantity: 4,
          startDateRestrictions: 'BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE',
          endDateRestrictions: null
        },
        {
          date: '2021-06-24T00:00:00Z',
          quantity: 3,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-06-25T00:00:00Z',
          quantity: 3,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-06-26T00:00:00Z',
          quantity: 3,
          startDateRestrictions: ['NON_WORKDAY'],
          endDateRestrictions: ['NON_WORKDAY']
        },
        {
          date: '2021-06-27T00:00:00Z',
          quantity: 3,
          startDateRestrictions: ['NON_WORKDAY'],
          endDateRestrictions: ['NON_WORKDAY']
        },
        {
          date: '2021-06-28T00:00:00Z',
          quantity: 3,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-06-29T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-06-30T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-01T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-02T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-03T00:00:00Z',
          quantity: 4,
          startDateRestrictions: ['NON_WORKDAY'],
          endDateRestrictions: ['NON_WORKDAY']
        },
        {
          date: '2021-07-04T00:00:00Z',
          quantity: 4,
          startDateRestrictions: ['NON_WORKDAY'],
          endDateRestrictions: ['NON_WORKDAY']
        },
        {
          date: '2021-07-05T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-06T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-07T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-08T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-09T00:00:00Z',
          quantity: 4,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-10T00:00:00Z',
          quantity: 6,
          startDateRestrictions: ['NON_WORKDAY'],
          endDateRestrictions: ['NON_WORKDAY']
        },
        {
          date: '2021-07-11T00:00:00Z',
          quantity: 6,
          startDateRestrictions: ['NON_WORKDAY'],
          endDateRestrictions: ['NON_WORKDAY']
        },
        {
          date: '2021-07-12T00:00:00Z',
          quantity: 6,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-13T00:00:00Z',
          quantity: 6,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-14T00:00:00Z',
          quantity: 5,
          startDateRestrictions: null,
          endDateRestrictions: null
        },
        {
          date: '2021-07-15T00:00:00Z',
          quantity: 5,
          startDateRestrictions: null,
          endDateRestrictions: null
        }
      ]
    }
  ],
  fetchedUntilDate: '2021-12-31',
  fetchingUntilDate: undefined
}

modelShow.args = {
  model,
  onClickFavorite: action('click-favorite'),
  onOrderClick: action('order-click'),
  orderPanelTmp: <div className="jumbotron mt-5">FAKE ORDER PANEL</div>
}
