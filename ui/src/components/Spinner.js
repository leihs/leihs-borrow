import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function Spinner({ className, ...restProps }) {
  const props = {
    className: cx('spinner-border spinner-border-sm', className),
    role: 'status',
    'aria-hidden': true,
    ...restProps
  }
  return <span {...props}></span>
}

Spinner.propTypes = {
  className: PropTypes.string
}
