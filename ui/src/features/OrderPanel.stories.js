import React, { useState } from 'react'
import { action } from '@storybook/addon-actions'
import { FAKE_STYLEGUIDE_TIME } from '../../.storybook/fake-time'
import { locale as fakeLocale, orderPanelTexts as fakeTxt } from '../story-utils/fake-localization'
import { addYears, endOfMonth } from 'date-fns'
import { de as dateLocale } from 'date-fns/locale'

import { getOrderPanelMockData } from '../story-utils/sample-props'
import OrderPanel from './OrderPanel'
import ModalDialog from '../components/ModalDialog'

export default {
  title: 'Feature Components/OrderPanel',
  component: OrderPanel,
  parameters: {
    layout: 'fullscreen',
    storyshots: { disable: true } // (related to ModalDialog, see https://github.com/leihs/leihs/issues/1125)
  }
}

function OrderPanelStory({ modelDataOverrides = {}, inventoryPoolsOverride, initialPickupLocationId, title }) {
  const now = new Date(FAKE_STYLEGUIDE_TIME)
  const {
    modelData: baseModelData,
    inventoryPools: basePools,
    initialInventoryPoolId,
    maxDateLoaded: initialMaxDateLoaded,
    spec
  } = getOrderPanelMockData()

  const modelData = { ...baseModelData, ...modelDataOverrides }
  const inventoryPools = inventoryPoolsOverride || basePools
  const dialogTitle = title || modelData.name

  const [isValid, setIsValid] = useState()
  const [maxDateLoaded, setMaxDateLoaded] = useState(initialMaxDateLoaded)
  function handleCalendarNavigate({ date }) {
    const newMax = endOfMonth(date)
    if (newMax > maxDateLoaded) {
      setMaxDateLoaded(newMax)
    }
    action('calendar-navigate')
  }
  function handleDatesChange({ startDate }) {
    const newMax = endOfMonth(startDate)
    if (newMax > maxDateLoaded) {
      setMaxDateLoaded(newMax)
    }
    action('dates-change')
  }

  return (
    <ModalDialog title={dialogTitle} className="ui-booking-calendar" shown>
      <ModalDialog.Body>
        <OrderPanel
          modelData={modelData}
          //
          now={now}
          maxDateTotal={addYears(now, 10)}
          maxDateLoaded={maxDateLoaded}
          onCalendarNavigate={handleCalendarNavigate}
          //
          initialStartDate={now}
          initialEndDate={now}
          onDatesChange={handleDatesChange}
          //
          initialQuantity={1}
          onQuantityChange={action('quantity-change')}
          //
          inventoryPools={inventoryPools}
          initialInventoryPoolId={initialInventoryPoolId}
          onInventoryPoolChange={action('pool-change')}
          initialPickupLocationId={initialPickupLocationId}
          onPickupLocationChange={action('pickup-location-change')}
          //
          onSubmit={action('submit')}
          onValidate={setIsValid}
          locale={fakeLocale}
          dateLocale={dateLocale}
          txt={fakeTxt}
        />
        <div className="m-4 text-muted">
          <h4>Debugging Info</h4>
          <details>
            <summary className="code text-monospace">fake timestamp</summary>
            <pre>{JSON.stringify(FAKE_STYLEGUIDE_TIME)}</pre>
          </details>
          <details>
            <summary className="text-monospace">mock data used</summary>
            <pre>{JSON.stringify(modelData, 0, 2)}</pre>
          </details>
          <details>
            <summary className="text-monospace">mock data from spec</summary>
            <pre>{JSON.stringify(spec, 0, 2)}</pre>
          </details>
        </div>
      </ModalDialog.Body>
      <ModalDialog.Footer>
        <button type="submit" className="btn btn-primary" form="order-dialog-form" disabled={!isValid}>
          Hinzufügen
        </button>
        <button type="button" className="btn btn-secondary" onClick={action('cancel')}>
          Abbrechen
        </button>
      </ModalDialog.Footer>
    </ModalDialog>
  )
}

export const orderPanel = () => <OrderPanelStory />
orderPanel.storyName = 'OrderPanel'

export const withPickupLocations = () => <OrderPanelStory />
withPickupLocations.storyName = 'With pickup locations'

export const notTransportable = () => (
  <OrderPanelStory modelDataOverrides={{ transportable: false, name: '4K-Videokamera Sony FDR-AX53' }} />
)
notTransportable.storyName = 'Not transportable'

export const noPickupLocations = () => {
  const { inventoryPools } = getOrderPanelMockData()
  return (
    <OrderPanelStory
      inventoryPoolsOverride={inventoryPools.map(pool => ({
        ...pool,
        pickupLocations: []
      }))}
    />
  )
}
noPickupLocations.storyName = 'Without pickup locations'

export const prefilledPickupLocation = () => <OrderPanelStory initialPickupLocationId="pl-alt-1" />
prefilledPickupLocation.storyName = 'Prefill from catalog filter'

export const withPickupLocationsMoreDetails = () => <OrderPanelStory initialPickupLocationId="pl-alt-1" />
withPickupLocationsMoreDetails.storyName = 'Pickup location with more-details link'

export const pickupLocationsOnly = () => <OrderPanelStory title="Pickup location without inventory pool names" />
pickupLocationsOnly.storyName = 'Pickup location locations only (Hauptlager + alts)'
