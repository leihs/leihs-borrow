import React from 'react'
import PropTypes from 'prop-types'
import Icon, { iconMenu, iconMenuClose, iconBag, iconUser, iconArrow, iconApps } from './Icons'
import cx from 'classnames'
import * as DropdownMenu from '@radix-ui/react-dropdown-menu'

export default function Topnav({
  brandName = 'Leihs',
  brandLinkProps = {},

  cartItemCount,
  invalidCartItemCount = 0,
  cartRemainingMinutes,
  cartItemLinkProps = {},

  // main menu mobile
  mainMenuIsOpen = false,
  mainMenuLinkProps = {},

  // main menu desktop
  mainMenuItems = [],

  // user menu
  userProfileShort,
  mobileUserMenuIsOpen = false,
  mobileUserMenuLinkProps,
  desktopUserMenuData = {},
  desktopUserMenuTriggerProps,

  // app menu (desktop only - for mobile it is integrated in the main menu)
  appMenuData,
  appMenuTriggerProps,

  className,
  ...restProps
}) {
  const showCartCounter = !!cartItemCount || cartItemCount === 0
  const cartExpiringSoon = cartRemainingMinutes <= 5
  const cartExpired = cartRemainingMinutes <= 0
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
        {/* App menu (dropdown) */}
        {appMenuData && (
          <div className="not-for-burger-mode">
            <DropdownMenu.Root modal={false} className="topnav__radix-dropdown-root">
              <DropdownMenu.Trigger
                className="ui-app-menu-link topnav__radix-dropdown-trigger"
                {...appMenuTriggerProps}
              >
                <Icon icon={iconApps} width="20px" height="20px" style={{ margin: '3px 0 3px 0' }} />
              </DropdownMenu.Trigger>
              {renderDropdownContent(appMenuData)}
            </DropdownMenu.Root>
          </div>
        )}

        {/* User and Profile (for mobile screens, shown in an external overlay) */}
        {userProfileShort && (
          <a
            role="button"
            className={cx('ui-user-profile-button', 'for-burger-mode', 'topnav__user-profile-link user-icon', {
              'topnav__user-profile-link--open': mobileUserMenuIsOpen
            })}
            aria-expanded={mobileUserMenuIsOpen}
            {...mobileUserMenuLinkProps}
          >
            <span className="user-icon">
              <Icon icon={iconUser} />
              <div className="user-icon__badge">{userProfileShort}</div>
            </span>
          </a>
        )}

        {/* User and Profile (for lg screens, dropdown) */}
        {userProfileShort && (
          <div className="not-for-burger-mode">
            <DropdownMenu.Root modal={false} className="topnav__radix-dropdown-root">
              <DropdownMenu.Trigger
                className="ui-user-profile-button topnav__radix-dropdown-trigger"
                {...desktopUserMenuTriggerProps}
              >
                <span className="user-icon" style={{ margin: '-1px 0 1px 0' }}>
                  <Icon icon={iconUser} />
                  <div className="user-icon__badge">{userProfileShort}</div>
                </span>
              </DropdownMenu.Trigger>
              {renderDropdownContent(desktopUserMenuData)}
            </DropdownMenu.Root>
          </div>
        )}

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
      </div>
    </nav>
  )
}

function renderDropdownItem({ isSeparator, label, onClick, href, as, ...restProps }) {
  if (isSeparator) return <DropdownMenu.Separator className="topnav__radix-dropdown-separator" {...restProps} />
  const El = as || 'a'
  return (
    <DropdownMenu.Item asChild>
      <El className="topnav__radix-dropdown-item" onClick={onClick} href={href} {...restProps}>
        {label}
      </El>
    </DropdownMenu.Item>
  )
}

function renderDropdownContent({ items = [], children, ...restProps }) {
  return (
    <DropdownMenu.Content className="ui-topnav-dropdown topnav__radix-dropdown-content" sideOffset={0} {...restProps}>
      {items.map((item, i) => (
        <React.Fragment key={i}>{renderDropdownItem(item)}</React.Fragment>
      ))}
      {children}
    </DropdownMenu.Content>
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
  /** When a number 5 or lesser is given, the cart icon will flicker and show the minutes left. When 0 or lesser, the icon will get red and "0'" will be shown.   */
  cartRemainingMinutes: PropTypes.number,
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

  /** Short name of the current profile (when empty: user menu button will not be shown) */
  userProfileShort: PropTypes.node,
  /** Is the mobile user/profile menu open? */
  mobileUserMenuIsOpen: PropTypes.bool,
  /** Props for the `a` element around the mobile user/profile button */
  mobileUserMenuLinkProps: PropTypes.object,
  /** Data for desktop user/profile menu */
  desktopUserMenuData: PropTypes.shape({
    items: PropTypes.arrayOf(
      PropTypes.shape({
        isSeparator: PropTypes.bool,
        onClick: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
        label: PropTypes.node,
        href: PropTypes.string,
        as: PropTypes.node
      })
    ),
    children: PropTypes.node
  }),
  /** Props for the desktop user/profile menu trigger button */
  desktopUserMenuTriggerProps: PropTypes.shape({}),

  /** Data for the app switcher menu */
  appMenuData: PropTypes.shape({
    items: PropTypes.arrayOf(
      PropTypes.shape({ isSeparator: PropTypes.bool, onClick: PropTypes.func, label: PropTypes.node })
    ),
    children: PropTypes.node
  }),
  /** Props for the app switcher menu trigger button */
  appMenuTriggerProps: PropTypes.shape({}),

  /** CSS class of the wrapping element */
  className: PropTypes.string
}
