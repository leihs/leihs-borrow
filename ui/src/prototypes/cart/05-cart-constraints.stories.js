import React from 'react'

import { action } from '@storybook/addon-actions'

import OrderPanel from '../../features/OrderPanel'
import { locale as fakeLocale, orderPanelTexts as fakeTxt } from '../../story-utils/fake-localization'
import { de as dateLocale } from 'date-fns/locale'
import { eachDayOfInterval, formatISO, getDay } from 'date-fns'

export default {
  title: 'Prototypes/Cart/Cart Constraints'
}

export const cartConstraints = () => {
  const pool1 = {
    id: 'Pool001',
    name: 'Zoo Zürich',
    totalReservableQuantity: 3,
    reservationAdvanceDays: 1,
    maximumReservationTime: 10
  }
  const pool2 = { id: 'Pool002', name: 'Zoo Basel', totalReservableQuantity: 1 }

  const maxDate = new Date('2020-05-31')

  const modelData = {
    id: 'Model001',
    name: 'Elefant',
    availability: [
      {
        inventoryPool: pool1,
        dates: generateMockAvailabilityDates(maxDate, d =>
          ['2020-04-17', '2020-04-18', '2020-04-19', '2020-04-20'].includes(d) ? 2 : 3
        )
      },
      {
        inventoryPool: pool2,
        dates: generateMockAvailabilityDates(maxDate, () => 1)
      }
    ]
  }
  return (
    <div className="pb-5">
      <h1>Cart Constraints</h1>
      <ul className="text-muted">
        <li>Today is Tuesday, April 7 2020</li>
        <li>Work days Mo-Fr</li>
        <li>April 10-13 Easter holiday</li>
        <li>April 17-20 1 item of 3 inavailable </li>
        <li>April 27 max visits reached</li>
        <li>Advance days = 1 (i.e. earliest pickup tomorrow April 8)</li>
        <li>Maximum reservation time = 10 days</li>
        <li>Max date May 31</li>
      </ul>
      <OrderPanel
        modelData={modelData}
        maxDateTotal={maxDate}
        maxDateLoaded={maxDate}
        inventoryPools={[pool1, pool2]}
        initialQuantity={3}
        onSubmit={action('submit')}
        locale={fakeLocale}
        dateLocale={dateLocale}
        txt={fakeTxt}
      />
    </div>
  )
}

export function invalidPoolSelected() {
  // App must provide all pool properties in `inventoryPools` prop (not in `modelData.availability`)
  // App must guarantee that the currently selected pool is contained in `inventoryPools`
  const pool1 = {
    id: 'Pool001',
    name: 'Valid pool',
    totalReservableQuantity: 3
  }
  const poolWithoutItem = {
    id: 'Pool002',
    name: 'Pool without the item',
    totalReservableQuantity: 0
  }
  const noAccessPool = {
    id: 'Pool003',
    name: 'Pool where user has no access',
    userHasNoAccess: true
  }
  const suspendedPool = {
    id: 'Pool004',
    name: 'Pool where user is suspended',
    userIsSuspended: true
  }
  const maxDate = new Date('2020-05-31')
  const modelData = {
    id: 'Model001',
    name: 'Elefant',
    availability: [
      {
        inventoryPool: pool1,
        dates: generateMockAvailabilityDates(maxDate, d =>
          ['2020-04-17', '2020-04-18', '2020-04-19', '2020-04-20'].includes(d) ? 2 : 3
        )
      }
    ]
  }

  return (
    <div className="pb-5">
      <h1>Cart Constraints - Invalid pool selected</h1>
      <p className="text-muted">
        This is only to expect when a reservation was made before, but then got invalid while being in the cart
        (unsubmitted order)
      </p>
      <p className="text-muted">Use the &quot;Inventarpark&quot; selector to see all cases:</p>
      <ol className="text-muted">
        <li>A valid pool</li>
        <li>Selected pool has no items</li>
        <li>Pool the user can not access</li>
        <li>Pool where the user is suspended</li>
      </ol>
      <p className="text-muted">
        The initially selected pool id is unknown on purpose. The app should always provide the pool details for a
        preselected invalid pool, otherwise the user does not see what the problem is.
      </p>
      <OrderPanel
        modelData={modelData}
        maxDateTotal={maxDate}
        maxDateLoaded={maxDate}
        inventoryPools={[pool1, poolWithoutItem, noAccessPool, suspendedPool]}
        initialInventoryPoolId="BadPoolId"
        initialQuantity={3}
        onSubmit={action('submit')}
        locale={fakeLocale}
        dateLocale={dateLocale}
        txt={fakeTxt}
      />
    </div>
  )
}

function generateMockAvailabilityDates(maxDate, getQuantity) {
  return eachDayOfInterval({ start: new Date('2020-04-01'), end: maxDate }).map(d => {
    const dateISO = formatISO(d)
    const dayOfWeek = getDay(d)
    const dateShort = formatISO(d, { representation: 'date' })

    const noWorkDay = dayOfWeek === 0 || dayOfWeek === 6
    const holiday = dateShort === '2020-04-10' || dateShort === '2020-04-13'
    const maxVisitsReached = dateShort === '2020-04-27'

    const restriction = noWorkDay || holiday ? 'CLOSE_TIME' : maxVisitsReached ? 'VISITS_CAPACITY_REACHED' : undefined

    return {
      date: dateISO,
      quantity: getQuantity(dateShort),
      startDateRestriction: restriction,
      endDateRestriction: restriction
    }
  })
}
