import React from 'react'
import SquareImageGrid from '../../components/SquareImageGrid'
import Section from '../../components/Section'
import PageLayoutMock from '../../story-utils/PageLayoutMock'
import PageLayout from '../../components/PageLayout'
import Stack from '../../components/Stack'
import ModelSearchFilter from '../../features/ModelSearchFilter'
import CategoryBreadcrumbs from '../../features/CategoryBreadcrumbs'
import { modelListProps, modelSearchFilterProps } from '../../story-utils/sample-props'

export default {
  title: 'Prototypes/Catalog/Sub Category',
  parameters: { layout: 'fullscreen' }
}

export const subCategory = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header
        title="Mischpulte & CD Player"
        preTitle={<CategoryBreadcrumbs ancestorCats={[{ id: '1', name: 'Audio', url: 'cat/1' }]} />}
      >
        <ModelSearchFilter {...modelSearchFilterProps} />
      </PageLayout.Header>
      <Stack space="4">
        <Section title="Gegenstände" collapsible>
          <SquareImageGrid {...modelListProps} />
        </Section>
      </Stack>
    </PageLayoutMock>
  )
}
