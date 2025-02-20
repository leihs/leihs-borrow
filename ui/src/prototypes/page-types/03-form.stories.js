import React, { useState } from 'react'
import cx from 'classnames'
import ModalDialog from '../../components/ModalDialog'
import Section from '../../components/Section'
import LabelInside from '../../components/LabelInside'
import InputWithClearButton from '../../components/InputWithClearButton'

export default {
  title: 'Prototypes/Page Typology/Form',
  parameters: {
    layout: 'fullscreen',
    storyshots: { disable: true } // (related to ModalDialog, see https://github.com/leihs/leihs/issues/1125)
  },
  argTypes: { onSubmit: { action: 'submit' } }
}

export const form = ({ onSubmit }) => {
  const [wasSubmitted, setWasSubmitted] = useState()
  const [term, setTerm] = useState('')

  function submit(e) {
    e.preventDefault()
    if (e.target.checkValidity()) {
      onSubmit()
    } else {
      setWasSubmitted(true)
    }
  }

  return (
    <ModalDialog title="Form" shown>
      <ModalDialog.Body>
        <form
          onSubmit={submit}
          noValidate
          className={cx({ 'was-validated': wasSubmitted })}
          autoComplete="off"
          id="the-form"
        >
          <div className="d-grid gap-4">
            <Section title="Search term">
              <label htmlFor="field-1" className="visually-hidden">
                Search term
              </label>
              <InputWithClearButton
                type="text"
                id="field-1"
                name="field-1"
                className="form-control"
                required
                value={term}
                onChange={e => setTerm(e.target.value)}
              />
              <div className="invalid-feedback">Required field</div>
            </Section>
            <Section title="Pool">
              <label htmlFor="field-2" className="visually-hidden">
                Pool
              </label>
              <select id="field-2" name="field-2" className="form-select" required>
                <option value="">All</option>
                <option value="value-1">Toni</option>
                <option value="value-2">Werkstatt</option>
                <option value="value-3">ITZ</option>
              </select>
            </Section>
            <Section title="Availability">
              <fieldset>
                <legend className="visually-hidden">Availability</legend>
                <div className="d-flex flex-column gap-3">
                  <LabelInside>
                    <label htmlFor="from">From</label>
                    <input
                      type="text"
                      id="from"
                      name="from"
                      placeholder="Unbestimmt"
                      required
                      className="form-control calendar-indicator"
                    />
                  </LabelInside>
                  <LabelInside>
                    <label htmlFor="to">To</label>
                    <input
                      type="text"
                      id="to"
                      name="to"
                      placeholder="Unbestimmt"
                      required
                      className="form-control calendar-indicator"
                    />
                  </LabelInside>
                </div>
              </fieldset>
            </Section>
          </div>
        </form>
      </ModalDialog.Body>
      <ModalDialog.Footer>
        <button type="submit" className="btn btn-primary" form="the-form">
          Save
        </button>
        <button type="cancel" className="btn btn-secondary" form="the-form">
          Cancel
        </button>
      </ModalDialog.Footer>
    </ModalDialog>
  )
}
