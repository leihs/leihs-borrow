import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function Badge({ children, as: Elm = 'div', className, colorClassName, style, ...restProps }) {
  const styleAttr = { padding: '9px 20px 9px 20px', ...style }
  return (
    <Elm
      className={cx(
        'badge rounded-pill fw-bold border-0',
        colorClassName ? colorClassName : 'bg-secondary text-dark',
        className,
        'ui-badge'
      )}
      style={styleAttr}
      {...restProps}
    >
      {children}
    </Elm>
  )
}

Badge.propTypes = {
  children: PropTypes.node,
  as: PropTypes.string,
  className: PropTypes.string,
  colorClassName: PropTypes.string,
  style: PropTypes.object
}
