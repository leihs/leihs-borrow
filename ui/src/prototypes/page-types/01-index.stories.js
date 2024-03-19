import React from 'react'
import PageLayout from '../../components/PageLayout'
import Stack from '../../components/Stack'
import Section from '../../components/Section'
import SquareImageGrid from '../../components/SquareImageGrid'
import ListCard from '../../components/ListCard'
import PageLayoutMock from '../../story-utils/PageLayoutMock'
import ModelSearchFilter from '../../features/ModelSearchFilter'
import { modelSearchFilterProps } from '../../story-utils/sample-props'

export default {
  title: 'Prototypes/Page Typology/Index',
  parameters: { layout: 'fullscreen' },
  argTypes: {
    onTriggerAvailability: { action: 'onTriggerAvailability' },
    onSubmit: { action: 'submit' },
    onItemClick: { action: 'item click' }
  }
}

const img1 = require('../../../static/example-images/models/62f4cb3c-999d-53ec-9426-298ebacd208a.jpg')
const img2 = require('../../../static/example-images/categories/286f85ba-e1a1-5c36-b03b-cf443f81d77d.jpg')

export const index = ({ onTriggerAvailability, onSubmitTerm, onItemClick }) => {
  return (
    <PageLayoutMock>
      <PageLayout.Header preTitle="Context" title="Title">
        <ModelSearchFilter
          currentFilters={{}}
          onTriggerAvailability={onTriggerAvailability}
          onSubmitTerm={onSubmitTerm}
          locale="de-CH"
          txt={modelSearchFilterProps.txt}
        />
      </PageLayout.Header>

      <Stack space="5">
        <Section title="Section with image results" collapsible>
          <SquareImageGrid
            list={[
              { id: 1, caption: 'Audio', imgSrc: img1, href: '#' },
              { id: 2, caption: 'Foto', imgSrc: img2, href: '#' }
            ]}
          />
        </Section>
        <Section title="Section with list results" collapsible>
          <ListCard.Stack>
            <ListCard onClick={onItemClick}>
              <ListCard.Title>Audio</ListCard.Title>
              <ListCard.Body>20 articles</ListCard.Body>
            </ListCard>
            <ListCard onClick={onItemClick}>
              <ListCard.Title>Foto</ListCard.Title>
              <ListCard.Body>16 articles</ListCard.Body>
            </ListCard>
          </ListCard.Stack>
        </Section>
      </Stack>
    </PageLayoutMock>
  )
}
