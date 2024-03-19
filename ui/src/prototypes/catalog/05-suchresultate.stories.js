import React from 'react'
import { action } from '@storybook/addon-actions'
import SquareImageGrid from '../../components/SquareImageGrid'
import Section from '../../components/Section'
import PageLayoutMock from '../../story-utils/PageLayoutMock'
import PageLayout from '../../components/PageLayout'
import Stack from '../../components/Stack'
import ModelSearchFilter from '../../features/ModelSearchFilter'
import { modelListProps, modelSearchFilterProps } from '../../story-utils/sample-props'

export default {
  title: 'Prototypes/Catalog/Search Results',
  parameters: { layout: 'fullscreen' }
}

export const searchResults = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Suchresultate">
        <ModelSearchFilter
          availableFilters={{
            pools: [
              { id: '', label: 'Alle Inventarparks' },
              { id: 1, label: 'pool A' },
              { id: 2, label: 'pool B' }
            ]
          }}
          currentFilters={{
            term: 'beamer!',
            selectedPool: { id: 1, label: 'pool A' },
            onlyAvailable: true,
            quantity: 3,
            startDate: '2022-04-21',
            endDate: '2022-04-24'
          }}
          onTriggerAvailability={action('onTriggerAvailability')}
          onClearFilter={action('onClearFilter')}
          onSubmitTerm={action('onSubmitTerm')}
          onChangePool={action('onChangePool')}
          locale="de-CH"
          txt={modelSearchFilterProps.txt}
        />
      </PageLayout.Header>

      <Stack space="4">
        <Section title="Gegenstände" collapsible>
          <SquareImageGrid {...modelListProps} />
        </Section>
      </Stack>
    </PageLayoutMock>
  )
}
