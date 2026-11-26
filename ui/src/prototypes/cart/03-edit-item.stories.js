import React from 'react'

import ModalDialog from '../../components/ModalDialog'
import ActionButtonGroup from '../../components/ActionButtonGroup'
import OrderPanel from '../../features/OrderPanel'
import { getOrderPanelMockData } from '../../story-utils/sample-props'
import { locale, orderPanelTexts } from '../../story-utils/fake-localization'
import { de as dateLocale } from 'date-fns/locale'

export default {
  title: 'Prototypes/Cart/Edit Item',
  parameters: {
    layout: 'fullscreen'
  },
  argTypes: {
    onSubmit: { action: 'submit' },
    onCancel: { action: 'cancel' },
    onRemoveClick: { action: 'remove' }
  }
}

function EditItemDialog({ onSubmit, onCancel, onRemoveClick, inventoryPools }) {
  const { modelData, maxDateLoaded } = getOrderPanelMockData()
  return (
    <ModalDialog title={modelData.name} shown>
      <ModalDialog.Body>
        <div className="d-grid gap-4">
          <OrderPanel
            modelData={modelData}
            maxDateLoaded={maxDateLoaded}
            inventoryPools={inventoryPools}
            onSubmit={onSubmit}
            locale={locale}
            dateLocale={dateLocale}
            txt={orderPanelTexts}
          />
          <ActionButtonGroup>
            <button type="button" className="btn btn-secondary" onClick={onRemoveClick}>
              Gegenstand entfernen
            </button>
          </ActionButtonGroup>
        </div>
      </ModalDialog.Body>
      <ModalDialog.Footer>
        <button type="submit" className="btn btn-primary" form="order-dialog-form">
          Bestätigen
        </button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          Abbrechen
        </button>
      </ModalDialog.Footer>
    </ModalDialog>
  )
}

export const editItem = ({ onSubmit, onCancel, onRemoveClick }) => {
  const { inventoryPools } = getOrderPanelMockData()
  return (
    <EditItemDialog
      onSubmit={onSubmit}
      onCancel={onCancel}
      onRemoveClick={onRemoveClick}
      inventoryPools={inventoryPools.map(pool => ({
        ...pool,
        pickupLocations: []
      }))}
    />
  )
}
editItem.storyName = 'Without pickup locations'

export const editItemWithPickupLocations = ({ onSubmit, onCancel, onRemoveClick }) => {
  const { inventoryPools } = getOrderPanelMockData()
  return (
    <EditItemDialog
      onSubmit={onSubmit}
      onCancel={onCancel}
      onRemoveClick={onRemoveClick}
      inventoryPools={inventoryPools}
    />
  )
}
editItemWithPickupLocations.storyName = 'With pickup locations'
