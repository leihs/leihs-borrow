import React from 'react'
import { linkTo } from '@storybook/addon-links'
import ModelSearchFilter from './ModelSearchFilter'

export default {
  title: 'Feature Components/ModelSearchFilter',
  component: ModelSearchFilter
}

const txt = {
  'search-button-label': { 'en-GB': 'Search' },
  'search-input-placeholder': { 'en-GB': 'Search' },
  'pool-select-label': { 'en-GB': 'Inventory pool' },
  'availability-button-label': { 'en-GB': 'Availability' },
  'availability-unrestricted': { 'en-GB': 'Availability unrestricted' },
  'availability-label': { 'en-GB': '{startDate}–{endDate}, qty {quantity}' }
}

const indent = '\u00A0\u00A0'

export const withPickupLocations = () => (
  <ModelSearchFilter
    locale="en-GB"
    txt={txt}
    availableFilters={{
      pools: [
        { id: '', label: 'All inventory pools' },
        { id: 'pool-toni', type: 'pool', label: 'Ausleihe Toni-Areal' },
        {
          id: 'loc-gate-1',
          type: 'pickupLocation',
          poolId: 'pool-toni',
          label: `${indent}Toni-Areal Gate-1`
        },
        {
          id: 'loc-gate-2',
          type: 'pickupLocation',
          poolId: 'pool-toni',
          label: `${indent}Toni-Areal Gate-2`
        },
        {
          id: 'loc-gate-3',
          type: 'pickupLocation',
          poolId: 'pool-toni',
          label: `${indent}Toni-Areal Gate-3`
        },
        { id: 'pool-wsl', type: 'pool', label: 'WSL-Ausleihe' }
      ]
    }}
    currentFilters={{
      term: '',
      selectedPool: { id: '', label: 'All inventory pools' },
      onlyAvailable: false
    }}
    onSubmitTerm={() => {}}
    onTriggerAvailability={() => {}}
    onClearFilter={() => {}}
    onChangePool={() => {}}
  />
)

withPickupLocations.storyName = 'With indented pickup locations'

export const modelSearchFilter = () => (
  <div>
    <p className="text-muted">Siehe</p>
    <button className="btn btn-light btn-sm" onClick={linkTo('Prototypes/Catalog/Search Results')}>
      Prototypes &gt; Katalog &gt; Suchresultate
    </button>
  </div>
)

modelSearchFilter.storyName = 'ModelSearchFilter'
