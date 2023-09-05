import React from 'react'
import PropTypes from 'prop-types'
import Icon, { iconMenu, iconMenuClose, iconBag, iconUser, iconArrow } from './Icons'
import cx from 'classnames'

export default function Topnav({
  brandName = 'Leihs',
  brandLinkProps = {},

  cartItemCount,
  invalidCartItemCount = 0,
  cartRemainingMinutes,
  cartExpired,
  cartItemLinkProps = {},

  // main menu mobile
  mainMenuIsOpen = false,
  mainMenuLinkProps = {},

  // main menu desktop
  mainMenuItems = [],

  userMenuIsOpen = false,
  userProfileShort,
  userMenuLinkProps,

  appMenuIsOpen = false,
  appMenuLinkLabel,
  appMenuLinkProps,

  className,
  ...restProps
}) {
  const showCartCounter = !!cartItemCount || cartItemCount === 0
  const cartExpiringSoon = cartRemainingMinutes <= 5

  return (
    <nav className={cx('ui-main-nav topnav', className)} {...restProps}>
      {/* Burger */}
      <a role="button" aria-expanded={mainMenuIsOpen} {...mainMenuLinkProps} className="topnav__burger-link">
        <span className="ui-icon ui-menu-icon">
          <Icon icon={mainMenuIsOpen ? iconMenuClose : iconMenu} />
        </span>
      </a>

      {/* Title ("Brand") */}
      <a className="topnav__brand-link" {...brandLinkProps}>
        {brandName}
      </a>

      {/* Nav (for lg screen) */}
      {mainMenuItems.length > 0 && (
        <div className="topnav__main-menu">
          {mainMenuItems.map(({ href, label, selected, ...restProps }, i) => (
            <a
              key={i}
              href={href}
              className={cx('topnav__main-menu-item-link', { 'topnav__main-menu-item-link--selected': selected })}
              {...restProps}
            >
              {label}
            </a>
          ))}
        </div>
      )}

      {/* Buttons on the right hand side */}
      <div className="topnav__right-buttons">
        {/* Cart */}
        <a
          role="button"
          className={cx('ui-cart-item-link topnav__cart-item-link cart-icon', {
            'cart-icon--expired': cartExpired
          })}
          {...cartItemLinkProps}
        >
          <Icon icon={iconBag} />
          {showCartCounter && (
            <div
              className={cx('cart-icon__badge', {
                'cart-icon__badge--with-conflict': invalidCartItemCount > 0,
                'cart-icon__badge--expiring-soon': cartExpiringSoon
              })}
            >
              <span>
                {cartExpiringSoon
                  ? Math.max(0, cartRemainingMinutes) + '′'
                  : invalidCartItemCount > 0
                  ? '!'
                  : cartItemCount}
              </span>
            </div>
          )}
        </a>

        {/* User and Profile */}
        {userProfileShort && (
          <a
            role="button"
            className={cx('ui-user-profile-button', 'topnav__user-profile-link user-icon', {
              'topnav__user-profile-link--open': userMenuIsOpen
            })}
            aria-expanded={userMenuIsOpen}
            {...userMenuLinkProps}
          >
            <Icon icon={iconUser} />
            <div className="user-icon__badge">{userProfileShort}</div>
          </a>
        )}

        {/* Sub App */}
        {appMenuLinkLabel && (
          <a
            role="button"
            className={cx('ui-app-menu-link', 'topnav__app-menu-link', {
              'topnav__app-menu-link--open': appMenuIsOpen
            })}
            aria-expanded={appMenuIsOpen}
            {...appMenuLinkProps}
          >
            {appMenuLinkLabel}
            <Icon icon={iconArrow} />
          </a>
        )}
      </div>
    </nav>
  )
}
Topnav.propTypes = {
  /** Brand (app) name */
  brandName: PropTypes.node,
  /** Props for the `a` element around the brand name */
  brandLinkProps: PropTypes.shape({}),

  /** Number of items in the cart, shown in a circle beneath the cart icon. Not necessarily a number. Circle will be hidden when value is falsy and not 0. */
  cartItemCount: PropTypes.node,
  /** When a number greater than zero is given, the cart icon is decorated with a warning symbol */
  invalidCartItemCount: PropTypes.number,
  /** When a number 5 or lesser is given, the cart icon will flicker and show the minutes left  */
  cartRemainingMinutes: PropTypes.number,
  /** When true, the cart icon will get red */
  cartExpired: PropTypes.bool,
  /** Props for the `a` element around the cart icon */
  cartItemLinkProps: PropTypes.object,

  /** Is the main menu open? */
  mainMenuIsOpen: PropTypes.bool,
  /** Props for the `a` element of the main menu */
  mainMenuLinkProps: PropTypes.shape({}),

  /** Main menu items (shown within topbar for screens md+) */
  mainMenuItems: PropTypes.arrayOf(
    PropTypes.shape({
      href: PropTypes.string,
      label: PropTypes.string.isRequired,
      selected: PropTypes.any
    })
  ),

  /** Is the user/profile menu open? */
  userMenuIsOpen: PropTypes.bool,
  /** Short name of the current profile */
  userProfileShort: PropTypes.node,
  /** Props for the `a` element around the user/profile button */
  userMenuLinkProps: PropTypes.object,

  /** Is the app switcher menu open? */
  appMenuIsOpen: PropTypes.bool,
  /** Label of the app switcher menu link */
  appMenuLinkLabel: PropTypes.node,
  /** Props for the `a` element around the app switch button */
  appMenuLinkProps: PropTypes.object,

  /** CSS class of the wrapping element */
  className: PropTypes.string
}
