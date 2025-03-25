import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function CircleBadge({ children, className, inline, variant, ...restProps }) {
  return (
    <div
      className={cx('circle-badge', className, {
        'circle-badge--inline': inline,
        'circle-badge--primary': variant === 'primary',
        'circle-badge--secondary': variant === 'secondary',
        'circle-badge--warning': variant === 'warning',
        'circle-badge--danger': variant === 'danger'
      })}
      {...restProps}
    >
      {children}
    </div>
  )
}

CircleBadge.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string,
  inline: PropTypes.bool,
  variant: PropTypes.oneOf(['primary', 'secondary', 'warning', 'danger'])
}
