import React from 'react'
import SquareImageGrid from '../../components/SquareImageGrid'
import Section from '../../components/Section'
import PageLayoutMock from '../../story-utils/PageLayoutMock'
import PageLayout from '../../components/PageLayout'
import Stack from '../../components/Stack'
import ListCard from '../../components/ListCard'
import { modelListProps, modelSearchFilterProps, subCategoryListProps } from '../../story-utils/sample-props'
import ModelSearchFilter from '../../features/ModelSearchFilter'

export default {
  title: 'Prototypes/Catalog/Category',
  parameters: { layout: 'fullscreen' }
}

export const category = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Audio">
        <ModelSearchFilter {...modelSearchFilterProps} />
      </PageLayout.Header>
      <Stack space="4">
        <Section title="Unterkategorien" collapsible initialCollapsed>
          <div className="mb-5">
            <ListCard.Stack>
              {subCategoryListProps.list.map(({ id, href, caption }) => (
                <ListCard key={id} href={href} oneLine>
                  <ListCard.Title>{caption}</ListCard.Title>
                </ListCard>
              ))}
            </ListCard.Stack>
          </div>
        </Section>
        <Section title="Gegenstände" collapsible>
          <SquareImageGrid {...modelListProps} />
        </Section>
      </Stack>
    </PageLayoutMock>
  )
}
