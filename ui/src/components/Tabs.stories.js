import React, { useState } from 'react'
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
        <code>react-bootstrap/Tabs</code>
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
    <PageLayoutMock>
      <h1>Tabs</h1>
      <p className="text-muted">
        In context of page layout above a list card stack. Note that <code>.tab-content</code> is shifted by 1px in
        order to make the top line of the content to work together with the tab lines.
      </p>
      <Tabs defaultActiveKey="a">
        <Tab eventKey="a" title="Lorem">
          <ListCard.Stack>
            <ListCard>Lorem</ListCard>
            <ListCard>Lorem</ListCard>
            <ListCard>Lorem</ListCard>
          </ListCard.Stack>
        </Tab>
        <Tab eventKey="b" title="Ipsum">
          <ListCard.Stack>
            <ListCard>Ipsum</ListCard>
          </ListCard.Stack>
        </Tab>
        <Tab eventKey="c" title="Dolor">
          <ListCard.Stack>
            <ListCard>Dolor</ListCard>
          </ListCard.Stack>
        </Tab>
      </Tabs>
    </PageLayoutMock>
  )
}
tabs.storyName = 'Tabs'

export const responsive = () => {
  const [tab, setTab] = useState('a')
  return (
    <PageLayoutMock>
      <h1>Tabs</h1>
      <p className="text-muted">How to make the tabs responsive - rather a recipe than a story 😎</p>
      <p className="text-muted">
        Add a <code>select</code> with a wrapping <code>div</code> above a <code>Tab</code> and wrap both with
        <code>.responsive-tab-combo</code>.
      </p>
      <p className="text-muted">
        The <code>select</code> must have the classes <code>form-select tab-select</code> for appearance. The wrapping{' '}
        <code>div</code> must have class <code>page-inset-x-inverse</code> so its text aligns with the content.
      </p>

      <div className="responsive-tab-combo">
        <div className="page-inset-x-inverse">
          <select className="form-select tab-select " value={tab} onChange={e => setTab(e.target.value)}>
            <option value="a">Tab Lorem</option>
            <option value="b">Tab Ipsum</option>
            <option value="c">Tab Dolor</option>
          </select>
        </div>
        <Tabs activeKey={tab} onSelect={setTab}>
          <Tab eventKey="a" title="Lorem">
            <ListCard.Stack>
              <ListCard>Lorem</ListCard>
              <ListCard>Lorem</ListCard>
              <ListCard>Lorem</ListCard>
            </ListCard.Stack>
          </Tab>
          <Tab eventKey="b" title="Ipsum">
            <ListCard.Stack>
              <ListCard>Ipsum</ListCard>
            </ListCard.Stack>
          </Tab>
          <Tab eventKey="c" title="Dolor">
            <ListCard.Stack>
              <ListCard>Dolor</ListCard>
            </ListCard.Stack>
          </Tab>
        </Tabs>
      </div>
    </PageLayoutMock>
  )
}
