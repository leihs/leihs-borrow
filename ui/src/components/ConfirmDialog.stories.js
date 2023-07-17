import React, { useState } from 'react'
import { action } from '@storybook/addon-actions'
import Button from 'react-bootstrap/Button'
import Section from './Section'
import ConfirmDialog from './ConfirmDialog'

export default {
  title: 'Design Components/Layout/ConfirmDialog',
  component: ConfirmDialog,
  parameters: {
    storyshots: { disable: true } // (see https://github.com/leihs/leihs/issues/1125)
  }
}

export const show_confirm_and_alert = () => {
  const INITIALLY_OPEN = false
  const FAKE_DELAY_SECONDS = 2
  const [isDialogShown, setIsDialogShown] = useState(INITIALLY_OPEN)
  const [isConfirmLoading, setIsConfirmLoading] = useState(false)
  const [isAlertShown, setIsAlertShown] = useState(false)

  const confirmFirstStep = () => {
    action('confirmed!')()
    setIsConfirmLoading(true)
    setTimeout(() => {
      setIsConfirmLoading(false)
      setIsDialogShown(false)
      setIsAlertShown(true)
    }, FAKE_DELAY_SECONDS * 1000)
  }
  const cancelFirstStep = () => {
    action('canceled!')()
    setIsDialogShown(false)
  }
  const confirmSecondStep = () => {
    action('confirmed 2!')()
    setIsAlertShown(false)
  }

  return (
    <>
      <p className="mx-3 my-4">
        The <code>ConfirmDialog</code> is a dialog that needs an acknowledgment by the user to be closed. Can be used
        for Alerts, Confirmations, Error Messages, etc. More complicated Modal Dialogs (like <code>OrderPanel</code>{' '}
        should use the <code>ModalDialog</code> component directly.)
      </p>
      <p className="text-center m-4">
        <Button onClick={() => setIsDialogShown(true)}>Do the thing!</Button>
      </p>
      <ConfirmDialog
        title="Confirmation"
        shown={isDialogShown}
        onConfirm={confirmFirstStep}
        onCancel={cancelFirstStep}
        confirmIsLoading={isConfirmLoading}
      >
        <p>Really do the thing?</p>
        <p>(It will take {FAKE_DELAY_SECONDS} seconds)</p>
      </ConfirmDialog>

      <ConfirmDialog title="It worked!" shown={isAlertShown} onConfirm={confirmSecondStep}>
        Did the thing!
      </ConfirmDialog>
    </>
  )
}

export const simple_alert = () => (
  <>
    <ConfirmDialog title="It worked!" shown={true} onConfirm={action('confirm')}>
      I did the thing and it was a great success!
    </ConfirmDialog>
  </>
)

export const dismissible_alert = () => {
  const [shown, setShown] = useState(true)
  function dismiss() {
    setShown(false)
  }
  return (
    <>
      <p className="text-center m-4">
        <Button onClick={() => setShown(true)}>Show notification</Button>
      </p>
      <ConfirmDialog title="Hello" shown={shown} onConfirm={action('confirm')} dismissible onDismiss={dismiss}>
        This message can be closed without clicking the big fat OK button.
      </ConfirmDialog>
    </>
  )
}

export const simple_error_message = () => {
  const FAKE_ERROR_DETAILS = {
    message: 'No authenticated user.',
    extensions: { code: 401, timestamp: '2021-10-12T11:30:58.679Z' }
  }
  return (
    <>
      <ConfirmDialog title="Fehler!" isError={true} shown={true} onConfirm={action('confirm')}>
        <p>Ein schwerer Ausnahmefehler ist aufgetreten!</p>
        <Section collapsible title="Details">
          <pre>{JSON.stringify(FAKE_ERROR_DETAILS, 0, 2)}</pre>
        </Section>
      </ConfirmDialog>
    </>
  )
}

export const simple_confirm = () => (
  <>
    <ConfirmDialog title="Confirmation" shown={true} onConfirm={action('confirm')} onCancel={action('cancel')}>
      Really do the thing?
    </ConfirmDialog>
  </>
)
export const simple_confirm_loading = () => (
  <>
    <ConfirmDialog
      title="Confirmation"
      shown={true}
      onConfirm={action('confirm')}
      onCancel={action('cancel')}
      confirmIsLoading={true}
    >
      Really do the thing?
    </ConfirmDialog>
  </>
)

export const cancel_order_de = () => (
  <>
    <ConfirmDialog
      title="Bestellung stornieren"
      shown={true}
      confirmLabel="Stornieren"
      cancelLabel="Abbrechen"
      onConfirm={action('confirm-cancel-order')}
      onCancel={action('cancel-cancel-order')}
    >
      <p>Video Semesterprojekt</p>
      <p>Material für das Video Semesterprojekt bei Prof. Zimmer.</p>
      <p>24 Tage ab 6.5.2020, 11 Gegenstände</p>
    </ConfirmDialog>
  </>
)
