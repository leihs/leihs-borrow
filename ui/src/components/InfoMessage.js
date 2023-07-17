import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function InfoMessage({ children, className, ...restProps }) {
  return (
    <div className={cx('info-message', className)} {...restProps}>
      {children}
    </div>
  )
}

InfoMessage.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string
}
