import React from 'react'
import { withLinks } from '@storybook/addon-links'

export default {
  title: 'Theme/Forms',
  decorators: [withLinks]
}

export const elements = () => {
  return (
    <>
      <h1>Forms - Elements</h1>
      <p className="text-muted">Text input field:</p>
      <div className="mb-3">
        <input type="text" name="name" className="form-control" placeholder="Placeholder" />
      </div>
      <p className="text-muted">Textarea:</p>
      <div className="mb-3">
        <textarea className="form-control" rows="3" />
      </div>
      <p className="text-muted">Select field:</p>
      <div className="mb-3">
        <select className="form-select">
          <option></option>
        </select>
      </div>
      <p className="text-muted">Checkbox:</p>
      <div className="mb-3">
        <div className="form-check">
          <input type="checkbox" id="elements-checkbox" className="form-check-input" />
          <label className="form-check-label" htmlFor="elements-checkbox">
            Label
          </label>
        </div>
      </div>
      <p className="text-muted">Switch:</p>
      <div className="mb-3">
        <div className="form-check form-switch">
          <input type="checkbox" id="elements-checkbox" className="form-check-input" />
          <label className="form-check-label" htmlFor="elements-checkbox">
            Label
          </label>
        </div>
      </div>
    </>
  )
}

export function labels() {
  return (
    <>
      <h1>Forms - Labels</h1>
      <p className="text-muted">
        DO wire the labels with <code>for</code> (i.e. <code>htmlFor</code>)
      </p>
      <p className="text-muted">DON&apos;T nest inputs in labels</p>
      <div className="mb-3">
        <label htmlFor="field-1" className="form-label">
          Label
        </label>
        <input type="text" id="field-1" className="form-control" />
      </div>
    </>
  )
}

export function validationFeedback() {
  return (
    <>
      <h1>Forms - Validation feedback</h1>
      <p className="text-muted">
        Note: this theme shows only indicators for invalid fields, not for valid ones (which would have green checkmarks
        by default)
      </p>
      <p className="text-muted">Text input field:</p>
      <div className="mb-3">
        <input type="text" className="form-control is-invalid" />
      </div>
      <p className="text-muted">Textarea:</p>
      <div className="mb-3">
        <textarea className="form-control is-invalid" rows="3" />
      </div>
      <p className="text-muted">Select field:</p>
      <div className="mb-3">
        <select className="form-select is-invalid">
          <option></option>
          <option>a</option>
          <option>b</option>
        </select>
      </div>
      <p className="text-muted">Checkbox:</p>
      <div className="mb-3">
        <div className="form-check">
          <input type="checkbox" id="elements-checkbox" className="form-check-input is-invalid" />
          <label className="form-check-label" htmlFor="elements-checkbox">
            Label
          </label>
        </div>
      </div>
      <p className="text-muted">Switch:</p>
      <div className="mb-3">
        <div className="form-check form-switch">
          <input type="checkbox" id="elements-checkbox" className="form-check-input is-invalid" />
          <label className="form-check-label" htmlFor="elements-checkbox">
            Label
          </label>
        </div>
      </div>
      <p className="text-muted">Field with invalid feedback text:</p>
      <div className="mb-3">
        <input type="text" name="name" className="form-control is-invalid" />
        <div className="invalid-feedback">Please enter your name</div>
      </div>
      <h2>Standalone invalid feedback text</h2>
      <p className="text-muted">
        When the message is not directly related to a form input as in the above example, or not even in a form:{' '}
        <button data-sb-kind="Design Components/Content/Warning" className="btn btn-secondary btn-sm">
          Use the Warning component
        </button>
      </p>
    </>
  )
}
