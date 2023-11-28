import React from 'react'
import Topnav from './Topnav'

export default {
  title: 'Design Components/Layout/Topnav',
  component: Topnav
}

export const topnav = () => {
  return (
    <div>
      <h1>Topnav</h1>
      <div className="border px-2">
        <Topnav
          cartItemCount={3}
          appMenuData={{ children: 'Bereiche...' }}
          userProfileShort="AB"
          desktopUserMenuData={{ children: 'Benutzermenu...' }}
          mainMenuItems={[{ label: 'Nav 1', selected: true }, { label: 'Nav 2' }, { label: 'Nav 3' }]}
        />
      </div>
      <p></p>
      <p className="text-muted">The navbar has the following 6 elements:</p>
      <ul className="text-muted">
        <li>App title (&quot;brand&quot;) with home link (mobile: centered; desktop: left) </li>
        <li>Hamburger menu link (desktop: hidden)</li>
        <li>Main menu inline (mobile: hidden)</li>
        <li>App switch button (mobile: hidden)</li>
        <li>User/profile menu button</li>
        <li>Cart icon with link to cart</li>
      </ul>
    </div>
  )
}

export const counterExamples = () => {
  const countVariants = [0, 3, 11, 68, 524, 999999999]
  return (
    <div>
      <h1>Topnav</h1>
      <p className="text-muted">How different counter values are rendered</p>
      {countVariants.map((cartItemCount, i) => {
        const navbarProps = {
          cartItemCount,
          userProfileShort: 'AB',
          appMenuTriggerEl: '[ ^ ]'
        }
        return (
          <div key={i} className="mb-3">
            <Topnav {...navbarProps} />
          </div>
        )
      })}
    </div>
  )
}

export const moreExamples = () => {
  return (
    <div>
      <h1>Topnav</h1>
      <p className="text-muted">Unbound (default props)</p>
      <div className="mb-3">
        <Topnav />
      </div>
      <p className="text-muted">RestProps</p>
      <div className="mb-3">
        <Topnav cartItemCount={3} className="border border-primary" />
      </div>
      <p className="text-muted">Props for the hamburger</p>
      <div className="mb-3">
        <Topnav cartItemCount={3} mainMenuLinkProps={{ className: 'border border-primary' }} />
      </div>
      <p className="text-muted">Props for the a tag surrounding the brand name</p>
      <div className="mb-3">
        <Topnav cartItemCount={3} brandLinkProps={{ className: 'border border-primary' }} />
      </div>
      <p className="text-muted">With cart conflict</p>
      <div className="mb-3">
        <Topnav cartItemCount={3} invalidCartItemCount={1} />
      </div>
      <p className="text-muted">With timeout soon</p>
      <div className="mb-3">
        <Topnav cartItemCount={3} cartRemainingMinutes={5} />
      </div>
      <p className="text-muted">With timeout reached</p>
      <div className="mb-3">
        <Topnav cartItemCount={3} cartRemainingMinutes={-1} cartExpired={true} />
      </div>
    </div>
  )
}
