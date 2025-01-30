import React from 'react'
import { action } from '@storybook/addon-actions'
import PageLayoutMock from '../story-utils/PageLayoutMock'
import PageLayout from '../components/PageLayout'
import OrderSearchFilter from './OrderSearchFilter'
import { orderSearchFilterProps } from '../story-utils/sample-props'

export default {
  title: 'Feature Components/OrderSearchFilter',
  component: OrderSearchFilter
}

export const empty = () => (
  <PageLayoutMock>
    <PageLayout.Header title="Bestellungen">
      <OrderSearchFilter
        {...orderSearchFilterProps}
        onSubmitTerm={action('onSubmitTerm')}
        onTriggerTimespan={action('onTriggerTimespan')}
        onClearFilter={action('onClearFilter')}
        onChangePool={action('onChangePool')}
      />
    </PageLayout.Header>
  </PageLayoutMock>
)

export const filled = () => (
  <PageLayoutMock>
    <PageLayout.Header title="Bestellungen">
      <OrderSearchFilter
        {...orderSearchFilterProps}
        currentFilters={{
          term: 'MacBook',
          selectedPool: { id: 1, label: 'pool A' },
          status: 'IN_APPROVAL',
          from: '2022-04-21',
          until: '2022-04-24'
        }}
        onSubmitTerm={action('onSubmitTerm')}
        onTriggerTimespan={action('onTriggerTimespan')}
        onClearFilter={action('onClearFilter')}
        onChangePool={action('onChangePool')}
      />
    </PageLayout.Header>
  </PageLayoutMock>
)
