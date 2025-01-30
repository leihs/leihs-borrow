import React, { useState } from 'react'
import Section from './Section'

export default {
  title: 'Design Components/Layout/Section',
  component: Section
}

const sampleText =
  'The Alpine ibex (Capra ibex), also known as the steinbock, bouquetin, or simply ibex, is a species of wild goat that lives in the mountains of the European Alps.'

export const section = () => {
  return (
    <div>
      <h1>Section</h1>
      <p className="text-muted">Wraps a primary piece of content, has a title and often is collapsible.</p>
      <Section title="Ibex" collapsible>
        {sampleText}
      </Section>
    </div>
  )
}

export const stateOptions = () => {
  const [collapsed1, setCollapsed1] = useState(false)
  const [collapsed2, setCollapsed2] = useState(false)
  return (
    <div>
      <h1>Section</h1>
      <p className="text-muted">Not collapsible</p>
      <Section title="Ibex" className="mb-4">
        {sampleText}
      </Section>
      <p className="text-muted">Uncontrolled, initially expanded (= default)</p>
      <Section title="Ibex" collapsible className="mb-4">
        {sampleText}
      </Section>
      <p className="text-muted">Uncontrolled, initially collapsed</p>
      <Section title="Ibex" collapsible initialCollapsed className="mb-4">
        {sampleText}
      </Section>
      <p className="text-muted">
        Uncontrolled, with change notification
        <br />
        <span className="small">(current state: {collapsed1 ? 'collapsed' : 'expanded'})</span>
      </p>
      <Section title="Ibex" collapsible onToggleCollapse={setCollapsed1} className="mb-4">
        {sampleText}
      </Section>
      <p className="text-muted">
        Section.Controlled{' '}
        <button className="btn btn-outline-primary btn-sm fw-light ms-2" onClick={() => setCollapsed2(x => !x)}>
          {collapsed2 ? 'uncollapse it!' : 'collapse it!'}
        </button>
      </p>
      <Section.Controlled title="Ibex" collapsible collapsed={collapsed2} onToggleCollapse={setCollapsed2}>
        {sampleText}
      </Section.Controlled>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
    </div>
  )
}

export const restProps = () => {
  return (
    <div>
      <h1>Section</h1>
      <p className="text-muted">Set arbitrary attributes with restProps</p>
      <Section title="Ibex" collapsible className="text-primary">
        {sampleText}
      </Section>
    </div>
  )
}
