import React from 'react'
import ModelSearchFilter from '../../features/ModelSearchFilter'
import SquareImageGrid from '../../components/SquareImageGrid'
import Section from '../../components/Section'
import PageLayoutMock from '../../story-utils/PageLayoutMock'
import PageLayout from '../../components/PageLayout'
import { categoryList, modelSearchFilterProps } from '../../story-utils/sample-props'

export default {
  title: 'Prototypes/Catalog/Start',
  parameters: { layout: 'fullscreen' }
}

export const start = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Katalog">
        <ModelSearchFilter {...modelSearchFilterProps} />
      </PageLayout.Header>
      <div className="d-grid gap-4">
        <Section title="Kategorien">
          <SquareImageGrid list={categoryList} />
        </Section>
      </div>
    </PageLayoutMock>
  )
}
