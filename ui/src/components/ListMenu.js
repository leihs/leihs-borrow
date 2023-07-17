import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function ListMenu({ children, className, ...restProps }) {
  return (
    <div className={cx('list-menu', className)} {...restProps}>
      <nav>{children}</nav>
    </div>
  )
}

ListMenu.Link = function ListMenuLink(props) {
  return MenuItem({ El: 'a', ...props })
}

ListMenu.Button = function ListMenuButton(props) {
  return MenuItem({ El: 'button', ...props })
}

function MenuItem({ El, children, isBreadcrumb, className, isSelected, hasChildren, ...restProps }) {
  return (
    <El
      className={cx(
        'list-menu__item',
        isBreadcrumb ? 'list-menu__item--breadcrumb' : 'list-menu__item--child',
        isSelected ? 'list-menu__item--selected' : '',
        className
      )}
      {...restProps}
    >
      {hasChildren && <div className="list-menu__dash"> </div>}
      {children}
    </El>
  )
}

ListMenu.CurrentItem = function ListMenuCurrentIcon({ children, className, ...restProps }) {
  return (
    <div className={cx('list-menu__item list-menu__item--current', className)} {...restProps}>
      {children}
    </div>
  )
}

ListMenu.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string
}

ListMenu.Link.propTypes = {
  children: PropTypes.node,
  isBreadcrumb: PropTypes.bool,
  isSelected: PropTypes.bool,
  hasChildren: PropTypes.bool,
  className: PropTypes.string
}

ListMenu.Button.propTypes = {
  children: PropTypes.node,
  isBreadcrumb: PropTypes.bool,
  isSelected: PropTypes.bool,
  hasChildren: PropTypes.bool,
  className: PropTypes.string
}
