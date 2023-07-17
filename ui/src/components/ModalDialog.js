import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames/dedupe'

import Button from 'react-bootstrap/Button'
import Modal from 'react-bootstrap/Modal'

export default function ModalDialog({
  id,
  className,
  title,
  children,
  shown = false,
  dismissible = false,
  onDismiss = () => {},
  headerClassname,
  contentClassName,
  ...restProps
}) {
  const baseId = id ? id : `Modal-${String(Math.random()).slice(2)}`
  // NOTE: we could use `aria-label` instead but it does not work yet <https://github.com/react-bootstrap/react-bootstrap/issues/5953>
  const labelId = baseId + '-Label'

  return (
    <Modal
      show={shown}
      onHide={onDismiss}
      fullscreen={'sm-down'}
      backdrop={dismissible ? true : 'static'}
      keyboard={dismissible}
      dialogClassName={className}
      contentClassName={contentClassName}
      centered
      scrollable
      aria-labelledby={labelId}
      {...restProps}
    >
      <Modal.Header
        closeButton={dismissible}
        className={cx('bg-light-shade border-bottom', { 'dismissible-modal-header': dismissible }, headerClassname)}
      >
        <Modal.Title id={labelId} as="div" className="m-auto fs-2">
          {title}
        </Modal.Title>
      </Modal.Header>

      {children}
    </Modal>
  )
}

ModalDialog.Body = function ModalDialogBody({ children, className, ...restProps }) {
  return (
    <Modal.Body className={className} {...restProps}>
      {children}
    </Modal.Body>
  )
}

ModalDialog.Footer = function ModalDialogFooter({ children, actions, className, ...restProps }) {
  if (children && actions) {
    throw new Error('props `actions` and `actionMenu` can not be given at the same time!')
  } else if (actions) {
    children = makeActionMenuFrom(actions)
  }
  return (
    <Modal.Footer
      className={cx('bg-light-shade border-top', 'd-flex justify-content-between flex-row-reverse', className)}
      {...restProps}
    >
      {children}
    </Modal.Footer>
  )
}

ModalDialog.propTypes = {
  id: PropTypes.string,
  className: PropTypes.string,
  title: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  shown: PropTypes.bool,
  /* When true: display a close button in the header, enable backdrop and ESC key to hide the modal (hiding as such must be implemented by the `onDismiss` handler) */
  dismissible: PropTypes.bool,
  /* Handler which closes the modal when user e.g. clicks on backdrop (details see `dismissible` prop) */
  onDismiss: PropTypes.func
}

ModalDialog.Body.propTypes = {
  className: PropTypes.string,
  children: PropTypes.node
}

ModalDialog.Footer.propTypes = {
  className: PropTypes.string,
  children: PropTypes.node,
  actionMenu: PropTypes.node,
  actions: PropTypes.object
}

function makeActionMenuFrom(actions) {
  const { primary, secondary } = actions
  return (
    <>
      {!!primary && (
        <Button variant="primary" {...primary}>
          {primary.label}
        </Button>
      )}

      {!!secondary && (
        <Button variant="secondary" {...secondary}>
          {secondary.label}
        </Button>
      )}
    </>
  )
}
