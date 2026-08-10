import React from 'react'
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

const sharedFilterProps = {
  locale: 'en-GB',
  txt,
  currentFilters: {
    term: '',
    selectedPool: { id: '', label: 'All inventory pools' },
    onlyAvailable: false
  },
  onSubmitTerm: () => {},
  onTriggerAvailability: () => {},
  onClearFilter: () => {},
  onChangePool: () => {}
}

export const withoutPickupLocations = () => (
  <ModelSearchFilter
    {...sharedFilterProps}
    availableFilters={{
      pools: [
        { id: '', label: 'All inventory pools' },
        { id: 'pool-toni', type: 'pool', label: 'Ausleihe Toni-Areal' },
        { id: 'pool-wsl', type: 'pool', label: 'WSL-Ausleihe' }
      ]
    }}
  />
)

withoutPickupLocations.storyName = 'Without pickup locations'

export const withPickupLocations = () => (
  <ModelSearchFilter
    {...sharedFilterProps}
    availableFilters={{
      pools: [
        { id: '', label: 'All inventory pools' },
        { id: 'pool-toni', type: 'pool', label: 'Ausleihe Toni-Areal' },
        {
          id: 'loc-gate-1',
          type: 'pickupLocation',
          poolId: 'pool-toni',
          label: `${indent}Pickup Location #1`
        },
        {
          id: 'loc-gate-2',
          type: 'pickupLocation',
          poolId: 'pool-toni',
          label: `${indent}Pickup Location #2`
        },
        {
          id: 'loc-gate-3',
          type: 'pickupLocation',
          poolId: 'pool-toni',
          label: `${indent}Pickup Location #3`
        },
        { id: 'pool-wsl', type: 'pool', label: 'WSL-Ausleihe' }
      ]
    }}
  />
)

withPickupLocations.storyName = 'With indented pickup locations'
