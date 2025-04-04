import React, { useState } from 'react'
import cx from 'classnames'

import ModalDialog from '../../components/ModalDialog'
import Section from '../../components/Section'
import Textarea from '../../components/Textarea'
import InfoMessage from '../../components/InfoMessage'

export default {
  title: 'Prototypes/Cart/Submit Order',
  parameters: {
    layout: 'fullscreen'
  },
  argTypes: {
    onSubmit: { action: 'submit' },
    onCancel: { action: 'cancel' }
  },
  args: {
    delegations: [
      { id: '37372089-450b-49ec-8486-fcc3a9e6ae22', name: 'Delegation 1' },
      { id: '3013ff5a-0203-4ec5-bda5-61871ddd5dc7', name: 'Delegation 2' }
    ],
    initialTitle: 'Videomodul',
    initialPurpose: 'Material für Videomodul im 3. Semester.'
  }
}

export const submitOrder = ({ onSubmit, onCancel, initialTitle, initialPurpose }) => {
  const [title, setTitle] = useState(initialTitle)
  const [purpose, setPurpose] = useState(initialPurpose)
  const [titlePurposeLinked, setTitlePurposeLinked] = useState(true)

  const [formValidated, setFormValidated] = useState()
  const [titleValidated, setTitleValidated] = useState()
  const [summaryValidated, setSummaryValidated] = useState()

  function blurTitle() {
    setTitleValidated(true)
  }
  function blurSummary() {
    setSummaryValidated(true)
  }

  function changeTitle(e) {
    setTitle(e.target.value)
    if (titlePurposeLinked) {
      setPurpose(e.target.value)
    }
  }
  function changePurpose(e) {
    setPurpose(e.target.value)
    setTitlePurposeLinked(false)
  }

  function submit(e) {
    e.preventDefault()
    setFormValidated(true)
    if (e.target.checkValidity()) {
      onSubmit({ title, purpose })
    }
  }
  return (
    <ModalDialog title="Bestellung abschicken" shown>
      <ModalDialog.Body>
        <form
          onSubmit={submit}
          noValidate
          autoComplete="off"
          className={cx({ 'was-validated': formValidated })}
          id="the-form"
        >
          <div className="d-grid gap-4">
            <Section title="Titel" className={cx({ 'was-validated': titleValidated })}>
              <label htmlFor="title" className="visually-hidden">
                Titel
              </label>
              <input
                type="text"
                name="title"
                id="title"
                className="form-control"
                required
                value={title}
                onChange={changeTitle}
                onBlur={blurTitle}
              />
              <InfoMessage className="mt-2">Als Referenz für dich</InfoMessage>
            </Section>
            <Section title="Zweck" className={cx({ 'was-validated': summaryValidated })}>
              <label htmlFor="purpose" className="visually-hidden">
                Zweck
              </label>
              <Textarea
                minRows="3"
                maxRows="15"
                name="purpose"
                id="purpose"
                className="form-control"
                required
                value={purpose}
                onChange={changePurpose}
                onBlur={blurSummary}
              />
              <InfoMessage className="mt-2">Für den Inventarpark</InfoMessage>
            </Section>
          </div>
        </form>
      </ModalDialog.Body>
      <ModalDialog.Footer>
        <button type="submit" className="btn btn-primary" form="the-form">
          Abschicken
        </button>
        <button type="button" className="btn btn-secondary" onClick={onCancel} form="the-form">
          Abbrechen
        </button>
      </ModalDialog.Footer>
    </ModalDialog>
  )
}
