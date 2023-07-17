import React from 'react'
import LabelInside from './LabelInside'

export default {
  title: 'Design Components/Form Controls/LabelInside',
  component: LabelInside
}

export const labelInside = () => {
  return (
    <div>
      <h1>LabelInside</h1>
      <p className="text-muted">
        Wrap around a <code>input.form-control</code> + <code>label</code> to enable inside labels with textual form
        fields:
      </p>
      <div className="mb-3">
        <LabelInside>
          <input
            type="text"
            name="field-1"
            id="field-1"
            defaultValue="Ptarmigan"
            placeholder="Name"
            className="form-control"
          />
          <label htmlFor="field-1">Name</label>
        </LabelInside>
      </div>
      <p className="text-muted small">
        Hint: This approach is a bit similar to Bootstrap&apos;s{' '}
        <a href="https://getbootstrap.com/docs/5.0/forms/floating-labels/">floating label</a>, and could be replaced by
        it without changing the wrapped code (as long as the labels come after the inputs, which is a requirement of
        floating labels). Here an example:
      </p>
      <div>
        <div className="form-floating">
          <input
            type="text"
            name="field-2"
            id="field-2"
            defaultValue="Ptarmigan"
            placeholder="Name"
            className="form-control"
          />
          <label htmlFor="field-2">Name</label>
        </div>
      </div>
    </div>
  )
}
labelInside.storyName = 'LabelInside'

export const labelWidth = () => {
  return (
    <div>
      <h1>LabelInside</h1>
      <p className="text-muted">
        Unfortunately the width of the label is not flexible. The default width is 4rem, with 0.25rem gap to the input
        text (visualized with a line in this story). Exceeding content will be trimmed with{' '}
        <code>overflow: hidden; white-space: nowrap</code>
      </p>
      <div className="mb-3">
        <LabelInside>
          <input type="text" name="field-11" id="field-11" placeholder="Species name" className="form-control" />
          <label htmlFor="field-11" className="border-end">
            Species name
          </label>
        </LabelInside>
      </div>
      <p className="text-muted">This can be changed using CSS applied to input and label.</p>
      <ul className="text-muted">
        <li>Apply a width to the label (e.g. 8rem)</li>
        <li>Apply a left padding to the input (e.g. 8.5rem, which gives a gap of 0.5rem)</li>
      </ul>
      <div>
        <LabelInside>
          <input
            type="text"
            name="field-12"
            id="field-12"
            placeholder="Species name"
            className="form-control"
            style={{ paddingLeft: '8.5rem' }}
          />
          <label htmlFor="field-12" style={{ width: '8rem' }}>
            Species name:
          </label>
        </LabelInside>
      </div>
    </div>
  )
}

export const textarea = () => {
  return (
    <div>
      <h1>LabelInside</h1>
      <p className="text-muted">
        With a <code>textarea.form-control</code>
      </p>
      <div>
        <LabelInside>
          <textarea rows="5" className="form-control" name="field-31" id="field-31" placeholder="Enter your message" />
          <label htmlFor="field-31">You:</label>
        </LabelInside>
      </div>
    </div>
  )
}

export const select = () => {
  return (
    <div>
      <h1>LabelInside</h1>
      <p className="text-muted">
        With a <code>select.form-select</code>
      </p>
      <div>
        <LabelInside>
          <select name="field-21" id="field-21" className="form-select">
            <option value="">Ibex</option>
            <option value="">Marmot</option>
            <option value="">Ptarmigan</option>
          </select>
          <label htmlFor="field-21">Name:</label>
        </LabelInside>
      </div>
    </div>
  )
}
