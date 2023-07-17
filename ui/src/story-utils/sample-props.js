import { parseISO } from 'date-fns'

export const categoryListPropsV1 = {
  list: [
    {
      id: '78920f6d-57c1-5231-b0c4-f58dcddc64cf',
      href: '#borrow/categories/78920f6d-57c1-5231-b0c4-f58dcddc64cf',
      imgSrc: require('../../static/example-images/categories/5d302b26-5737-48e7-a90d-e65f22d8c065.jpg'),
      caption: 'Audio'
    },
    {
      id: 'b279bb7f-314c-55d1-a407-0de794c2c25e',
      href: '#borrow/categories/b279bb7f-314c-55d1-a407-0de794c2c25e',
      imgSrc: require('../../static/example-images/categories/2b4c8bd3-3d65-5e68-bf7a-3649ec67a1a2.jpg'),
      caption: 'Beleuchtungstechnik'
    },
    {
      id: '298ec3da-d2c8-5d9d-ae1a-55715ccd933c',
      href: '#borrow/categories/298ec3da-d2c8-5d9d-ae1a-55715ccd933c',
      imgSrc: require('../../static/example-images/categories/fdfb06be-9bfc-5377-a82b-5d9fb0cc28fb.jpg'),
      caption: 'Effektgeräte Veranstaltungstechnik'
    },
    {
      id: 'c6a221ec-6df2-5ce7-b9a0-cc5694902353',
      href: '#borrow/categories/c6a221ec-6df2-5ce7-b9a0-cc5694902353',
      imgSrc: null,
      caption: 'Externe Massenspeicher'
    }
  ]
}

export const categoryList = [
  {
    id: '78920f6d-57c1-5231-b0c4-f58dcddc64cf',
    href: '#borrow/categories/78920f6d-57c1-5231-b0c4-f58dcddc64cf',
    imgSrc: require('../../static/example-images/models/62f4cb3c-999d-53ec-9426-298ebacd208a.jpg'),
    caption: 'Audio'
  },
  {
    id: '3f69bb3b-8934-589c-8d96-69f3ce31416e',
    href: '#borrow/categories/3f69bb3b-8934-589c-8d96-69f3ce31416e',
    imgSrc: require('../../static/example-images/categories/286f85ba-e1a1-5c36-b03b-cf443f81d77d.jpg'),
    caption: 'Foto'
  },
  {
    id: '56336471-2ce5-541c-be64-7fdb891f49f5',
    href: '#borrow/categories/56336471-2ce5-541c-be64-7fdb891f49f5',
    imgSrc: require('../../static/example-images/categories/9029dfa3-2691-5b73-94e0-785c5b0019a7.jpg'),
    caption: 'Video'
  },
  {
    id: '82165b56-aa9e-5d6a-b37c-f446cda4004e',
    href: '#borrow/categories/82165b56-aa9e-5d6a-b37c-f446cda4004e',
    imgSrc: require('../../static/example-images/categories/d5a8e40e-7890-5dde-9fb6-8317bac31b72.jpg'),
    caption: 'Musikinstrumente & Zubehör'
  }
]

export const subCategoryListProps = {
  list: [
    {
      id: '78920f6d-57c1-5231-b0c4-f58dcddc6400',
      href: '#borrow/categories/78920f6d-57c1-5231-b0c4-f58dcddc6400',
      caption: 'Kopfhörer'
    },
    {
      id: '78920f6d-57c1-5231-b0c4-f58dcddc6401',
      href: '#borrow/categories/78920f6d-57c1-5231-b0c4-f58dcddc6401',
      caption: 'Lautsprecher'
    },
    {
      id: '78920f6d-57c1-5231-b0c4-f58dcddc6421',
      href: '#borrow/categories/78920f6d-57c1-5231-b0c4-f58dcddc6402',
      caption: 'Mikrophone'
    }
  ]
}

export const modelListProps = {
  list: [
    {
      id: '4f64adb0-6325-4eb1-bf36-8a73a986ed4b',
      href: '#borrow/models/4f64adb0-6325-4eb1-bf36-8a73a986ed4b',
      imgSrc: require('../../static/example-images/models/4f64adb0-6325-4eb1-bf36-8a73a986ed4b.jpg'),
      caption: 'Audio-Mischpult Behringer XENYX Q1204USB'
    },
    {
      id: '720ecc22-ae14-576b-aa23-c09ee71103ad',
      href: '#borrow/models/720ecc22-ae14-576b-aa23-c09ee71103ad',
      caption: 'BluRay-Player Sony BDP-S490 3D',
      subCaption: 'Sony',
      imgSrc: require('../../static/example-images/models/4d446da0-81ae-5dd4-a2b0-f9adae2de0d4.jpg')
    }
  ]
}

export const modelSearchFilterProps = {
  currentFilters: {},
  locale: 'de-CH',
  onSubmit: () => {},
  onChangeSearchTerm: () => {},
  onOpenPanel: () => {},
  onClearFilter: () => {},
  txt: {
    'search-input-label': {
      'de-CH': 'Suchen',
      'en-GB': 'Search'
    },
    'search-input-placeholder': {
      'de-CH': 'Suchbegriff',
      'en-GB': 'Search term'
    },
    'search-filter-label': {
      'de-CH': 'Filter',
      'en-GB': 'Filter'
    },
    'availability-label': {
      'de-CH': '{quantity} Stück verfügbar {startDate, date, narrow} – {endDate, date, narrow}',
      'en-GB':
        '{quantity} {quantity, plural, =1 {item} other {items}} available {startDate, date, narrow} – {endDate, date, narrow}'
    }
  }
}

export function getOrderPanelMockData() {
  const mock = require('../../static/api-examples/features/borrow/calendar.feature/1_1_1_Model_reservation_calendar_.json')
  const spec = mock.spec
  const apiData = mock.result.data
  const modelData = apiData.models.edges.map(edg => edg.node)[0]
  modelData.name = 'Audio-Mischpult Behringer XENYX Q1204USB'

  // FIXME: pools should come from a seperate query,
  //         and availability data should have several pools!
  //         re-use and tranform example data for now…
  const FAKE_SECOND_POOL_ID = '53f78fc0-2b0b-4f67-a207-b08d2a3c47b2'
  modelData.availability.length < 2 &&
    modelData.availability.push({
      inventoryPool: { id: FAKE_SECOND_POOL_ID, name: 'Ein anderer Inventarpark', totalReservableQuantity: 3 },
      dates: modelData.availability[0].dates
    })
  const inventoryPools = modelData.availability.map(x => x.inventoryPool)
  const userDelegations = [
    { id: '2216bad8-36d3-4719-9d1e-a9c26d23045c', name: 'Normin Normalo (persönlich)' },
    { id: '879280bd-3840-48dd-bae4-7fb121ca446a', type: 'delegation', name: 'Movie Production Team' },
    { id: '30d1f5a3-1402-406c-8d36-0b400c5a83f0', type: 'delegation', name: 'Teaching Photography' }
  ]

  const availabilityDates = modelData.availability[0].dates
  return {
    modelData,
    profileName: userDelegations[0],
    inventoryPools,
    initialInventoryPoolId: inventoryPools[0].id,
    minDateLoaded: parseISO(availabilityDates[0].date),
    maxDateLoaded: parseISO(availabilityDates[availabilityDates.length - 1].date),
    spec
  }
}
