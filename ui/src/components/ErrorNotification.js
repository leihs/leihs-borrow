import React from 'react'
import cx from 'classnames'
import ModalDialog from './ModalDialog'

export default function ErrorNotification({
  className,
  children,
  title,
  dismissLabel = 'OK',
  onDismiss = () => {},
  ...restProps
}) {
  return (
    <ModalDialog
      {...restProps}
      className={cx(className)}
      contentClassName="border-danger border-2"
      title={title}
      headerClassname={[{ 'bg-light-shade': false }, 'bg-danger text-color-content-inverse']}
      dismissible
      onDismiss={onDismiss}
    >
      <ModalDialog.Body>{children}</ModalDialog.Body>
      <ModalDialog.Footer>
        <button type="button" onClick={onDismiss} className="btn btn-dark">
          {dismissLabel}
        </button>
      </ModalDialog.Footer>
    </ModalDialog>
  )
}

// TODO: propTypes
