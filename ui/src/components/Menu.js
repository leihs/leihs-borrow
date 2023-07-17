import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function Menu({ children, className, ...restProps }) {
  return (
    <div className={cx('ui-menu leihs-menu', className)} {...restProps}>
      <nav className="d-flex flex-column justify-content-between gap-5">{children}</nav>
    </div>
  )
}

Menu.Group = function MenuGroup({ children, title, className, ...restProps }) {
  return (
    <div className={cx('ui-menu-group flex-fill text-center', className)} {...restProps}>
      {title && <h2 className="fw-normal">{title}</h2>}
      <div className="d-flex flex-column gap-3">{children}</div>
    </div>
  )
}

Menu.HorizontalGroup = function MenuGroup({ children, title, className, ...restProps }) {
  return (
    <div className={cx('ui-menu-group flex-fill text-center', className)} {...restProps}>
      {title && <h2 className="fw-normal">{title}</h2>}
      <div className="d-flex flex-row gap-2 justify-content-center flex-wrap">{children}</div>
    </div>
  )
}

Menu.Link = function MenuLink(props) {
  return MenuItem({ El: 'a', ...props })
}

Menu.Button = function MenuButton(props) {
  return MenuItem({ El: 'button', ...props })
}

function MenuItem({ El, children, isSelected, className, ...restProps }) {
  return (
    <El
      className={cx(
        'ui-menu-item',
        { 'ui-menu-item-selected leihs-menu-item--selected': isSelected },
        'btn btn-secondary rounded-pill flex-grow-1',
        className
      )}
      {...restProps}
    >
      {children}
    </El>
  )
}

Menu.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string
}

Menu.Group.propTypes = {
  children: PropTypes.node,
  title: PropTypes.node,
  className: PropTypes.string
}

Menu.Link.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string
}

Menu.Button.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string
}
