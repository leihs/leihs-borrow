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

export const orderPanel = () => {
  const now = new Date(FAKE_STYLEGUIDE_TIME)
  const {
    modelData,
    inventoryPools,
    initialInventoryPoolId,
    maxDateLoaded: initialMaxDateLoaded,
    spec
  } = getOrderPanelMockData()

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
    <ModalDialog title="Gegenstand hinzufügen" className="ui-booking-calendar" shown>
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

orderPanel.storyName = 'OrderPanel'
