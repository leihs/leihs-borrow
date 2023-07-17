import React, { useState } from 'react'
import { action } from '@storybook/addon-actions'

import FilterButton from './FilterButton'
import PageLayoutMock from '../story-utils/PageLayoutMock'
import PageLayout from './PageLayout'
import ModalDialog from './ModalDialog'
import Button from 'react-bootstrap/Button'

export default {
  title: 'Design Components/Layout/ModalDialog',
  component: ModalDialog,
  parameters: {
    layout: 'fullscreen',
    storyshots: { disable: true } // (see https://github.com/leihs/leihs/issues/1125)
  }
}

export const demo = () => {
  const [modalOpen, setModalOpen] = useState(false)
  const handleClose = () => setModalOpen(false)
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Modal Dialog Demo">
        <FilterButton onClick={() => setModalOpen(!modalOpen)}>Click to open Modal</FilterButton>
        <ModalDialog shown={modalOpen} title="Modal Dialog Title">
          <ModalDialog.Body>
            {new Array(3).fill().map((_, i) => (
              <p key={i}>Modal body text goes here.</p>
            ))}
          </ModalDialog.Body>
          <ModalDialog.Footer
            actions={{
              primary: { label: 'OK', onClick: handleClose },
              secondary: { label: 'Abort', onClick: handleClose }
            }}
          ></ModalDialog.Footer>
        </ModalDialog>
      </PageLayout.Header>
    </PageLayoutMock>
  )
}

export const dismissible = () => {
  const [modalOpen, setModalOpen] = useState(true)
  const handleClose = () => setModalOpen(false)
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Dismissible dialog demo">
        <FilterButton onClick={() => setModalOpen(!modalOpen)}>Click to open Modal</FilterButton>
        <ModalDialog dismissible shown={modalOpen} onDismiss={handleClose} title="Dismissible dialog">
          <ModalDialog.Body>
            <p>A dismissible modal dialog can be closed in the following ways:</p>
            <ul>
              <li>Clicking the close button in the header</li>
              <li>
                Clicking on the backdrop (the area around the modal, which is only visible on larger screens however)
              </li>
              <li>Pressing the ESC key</li>
            </ul>
          </ModalDialog.Body>
          <ModalDialog.Footer
            actions={{
              primary: { label: 'OK', onClick: handleClose },
              secondary: { label: 'Abort', onClick: handleClose }
            }}
          ></ModalDialog.Footer>
        </ModalDialog>
      </PageLayout.Header>
    </PageLayoutMock>
  )
}

export const using_action_menu_config = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Katalog">
        <ModalDialog shown={true} title="Modal Dialog with Custom Action Menu">
          <ModalDialog.Body>
            {new Array(3).fill().map((_, i) => (
              <p key={i}>Modal body text goes here.</p>
            ))}
          </ModalDialog.Body>
          <ModalDialog.Footer>
            <Button variant="success" onClick={action('click one!')}>
              One
            </Button>
            <Button variant="warning" onClick={action('click two!')}>
              Two
            </Button>
            <Button variant="danger" onClick={action('click three!')}>
              Three
            </Button>
          </ModalDialog.Footer>
        </ModalDialog>
      </PageLayout.Header>
    </PageLayoutMock>
  )
}

export const with_long_content = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Katalog">
        <ModalDialog
          shown={true}
          title="This has lots of content in the body so that it will scroll, even the title is very long as to get at least a linebreak in there, man I could tell a whole story in this header wow much text"
        >
          <ModalDialog.Body>
            {new Array(24).fill().map((_, i) => (
              <p key={i}>Modal body text goes here.</p>
            ))}
          </ModalDialog.Body>
          <ModalDialog.Footer
            actions={{
              primary: { label: 'OK', onClick: action('primary click') },
              secondary: { label: 'Abbrechen', onClick: action('secondary click') }
            }}
          ></ModalDialog.Footer>
        </ModalDialog>
      </PageLayout.Header>
    </PageLayoutMock>
  )
}

export const withForm = () => {
  return (
    <PageLayoutMock>
      <ModalDialog shown title="Form">
        <ModalDialog.Body>
          <form id="the-form">
            <p className="text-muted">
              Wire the button to the form using the <code>form</code> attribute.
            </p>
            <div className="form-label">Sample input</div>
            <input type="text" className="form-control" required />
          </form>
        </ModalDialog.Body>
        <ModalDialog.Footer>
          <button type="submit" className="btn btn-primary" form="the-form">
            Submit
          </button>
        </ModalDialog.Footer>
      </ModalDialog>
    </PageLayoutMock>
  )
}
