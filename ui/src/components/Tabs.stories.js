import React from 'react'
import Tabs from 'react-bootstrap/Tabs'
import Tab from 'react-bootstrap/Tab'
import ListCard from './ListCard'
import PageLayoutMock from '../story-utils/PageLayoutMock'

export default {
  title: 'Design Components/Navigation/Tabs',
  component: Tabs,
  parameters: {
    layout: 'fullscreen'
  }
}

export const tabs = () => {
  return (
    <div className="p-3">
      <h1>Tabs</h1>
      <p className="text-muted">
        The <code>react-bootstrap/Tabs</code> is used directly
      </p>
      <Tabs defaultActiveKey="a" className="page-inset-x-inverse">
        <Tab eventKey="a" title="Lorem">
          Lorem
        </Tab>
        <Tab eventKey="b" title="ipsum">
          ipsum
        </Tab>
        <Tab eventKey="c" title="dolor">
          dolor
        </Tab>
      </Tabs>
    </div>
  )
}
tabs.storyName = 'Tabs'

export const inPageLayout = () => {
  return (
    <div>
      <PageLayoutMock>
        <h1>Tabs</h1>
        <p className="text-muted">
          In context of page layout above a list card stack. Note that <code>.tab-content</code> is shifted by 1px in
          order to make the top line of the content to work together with the tab lines.
        </p>
        <Tabs defaultActiveKey="a">
          <Tab eventKey="a" title="Lorem">
            <ListCard.Stack separators="all">
              <div>Lorem</div>
              <div>Lorem</div>
              <div>Lorem</div>
            </ListCard.Stack>
          </Tab>
          <Tab eventKey="b" title="Ipsum">
            Ipsum
          </Tab>
          <Tab eventKey="c" title="Dolor">
            Dolor
          </Tab>
        </Tabs>
      </PageLayoutMock>
    </div>
  )
}
tabs.storyName = 'Tabs'
