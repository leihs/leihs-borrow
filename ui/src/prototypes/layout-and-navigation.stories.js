import React, { useState } from 'react'

import Topnav from '../components/Topnav'
import Menu from '../components/Menu'
import PageLayout from '../components/PageLayout'
import { PowerOffIcon, UserIcon } from '../components/Icons'
import ListCard from '../components/ListCard'
import Section from '../components/Section'
import CircleBadge from '../components/CircleBadge'

export default {
  title: 'Prototypes/Layout And Navigation',
  parameters: { layout: 'fullscreen' }
}

function mobileMainNav({ onLinkClick }) {
  return (
    <Menu id="menu">
      <Menu.Group title="Ausleihen">
        <Menu.Link onClick={onLinkClick}>Katalog</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Warenkorb</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Bestellungen</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Favoriten</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Inventarparks</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Hilfe</Menu.Link>
      </Menu.Group>
      <Menu.Group title="Bereich wechseln">
        <Menu.Link onClick={onLinkClick} isSelected>
          Ausleihen
        </Menu.Link>
        <Menu.Link onClick={onLinkClick}>Leihs Admin</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Bedarfsermittlung</Menu.Link>
        <div className="pt-3">Verleih / Inventar</div>
        <Menu.Link onClick={onLinkClick}>Ausleihe Toni-Areal</Menu.Link>
        <Menu.Link onClick={onLinkClick}>AV-Services</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Departement Musik</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Kostüm- und Kleinrequisitenfundus DDK</Menu.Link>
      </Menu.Group>
    </Menu>
  )
}

function mobileUserNav({ onLinkClick }) {
  return (
    <Menu id="user-menu">
      <Menu.Group title="Anna Beispiel">
        <Menu.Link onClick={onLinkClick}>Benutzerdaten</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Abmelden</Menu.Link>
      </Menu.Group>
      <Menu.Group title="Profil">
        <Menu.Link onClick={onLinkClick} isSelected>
          Anna Beispiel (persönlich)
        </Menu.Link>
        <Menu.Link onClick={onLinkClick}>Delegation 1</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Delegation 2</Menu.Link>
      </Menu.Group>
      <Menu.Group title="Sprache">
        <Menu.Link onClick={onLinkClick} isSelected>
          Deutsch
        </Menu.Link>
        <Menu.Link onClick={onLinkClick}>English</Menu.Link>
        <Menu.Link onClick={onLinkClick}>Français</Menu.Link>
      </Menu.Group>
    </Menu>
  )
}

function mainMenuItems() {
  return [
    { href: '#', label: 'Katalog', selected: true },
    {
      href: '#',
      label: (
        <span>
          Bestellungen <CircleBadge inline>7</CircleBadge>
        </span>
      )
    },
    { href: '#', label: 'Favoriten' },
    { href: '#', label: 'Inventarparks' },
    { href: '#', label: 'Hilfe' }
  ]
}

function desktopUserMenuData() {
  return {
    items: [
      {
        href: '#user',
        label: (
          <>
            <UserIcon /> Benutzerkonto
          </>
        )
      },
      {
        as: 'button',
        onClick: () => alert('logout'),
        label: (
          <>
            <PowerOffIcon /> Abmelden
          </>
        )
      }
    ],
    children: (
      <>
        <div className="mt-4">
          <label htmlFor="profile-select" className="form-label">
            Profil wechseln
          </label>
          <select id="profile-select" className="form-select">
            <option>Anna Beispiel (persönlich)</option>
            <option>Delegation 1</option>
            <option>Delegation 2</option>
          </select>
        </div>
        <div className="mt-4 mb-3">
          <label htmlFor="language-select" className="form-label">
            Sprache
          </label>
          <select id="language-select" className="form-select">
            <option>Deutsch</option>
            <option>English</option>
            <option>Français</option>
          </select>
        </div>
      </>
    )
  }
}

function appMenuData() {
  return {
    items: [
      { label: 'Admin' },
      { label: 'Bedarfsermittlung' },
      { isSeparator: true },
      { label: 'Ausleihe Toni-Areal' },
      { label: 'AV-Services' },
      { label: 'Departement Musik' },
      { label: 'Kostüm- und Kleinrequisitenfundus DDK' }
    ]
  }
}

export function layoutAndNavigation() {
  // overlay nav
  const [overlay, setOverlay] = useState('') // '' | 'main' | 'user'
  function onMainMenubuttonClick() {
    setOverlay(x => (x === 'main' ? '' : 'main'))
  }
  function onUserMenuButtonClick() {
    setOverlay(x => (x === 'user' ? '' : 'user'))
  }
  function dismissOverlay() {
    setOverlay('')
  }

  return (
    <PageLayout
      topBar={
        <Topnav
          brandName="Leihs"
          brandLinkProps={{ role: 'button', onClick: dismissOverlay }}
          mainMenuIsOpen={overlay === 'main'}
          mainMenuLinkProps={{ onClick: onMainMenubuttonClick, 'aria-controls': 'menu' }}
          mainMenuItems={mainMenuItems()}
          cartItemCount={0}
          cartItemLinkProps={{ onClick: dismissOverlay, title: 'Warenkorb' }}
          // -- user menu --
          userProfileShort="AB"
          mobileUserMenuIsOpen={overlay === 'user'}
          mobileUserMenuLinkProps={{
            onClick: onUserMenuButtonClick,
            'aria-controls': 'user-menu',
            title: 'Benutzermenu'
          }}
          desktopUserMenuData={desktopUserMenuData()}
          desktopUserMenuTriggerProps={{ title: 'Benutzermenu' }}
          // -- app  menu --
          appMenuData={appMenuData()}
          appMenuTriggerProps={{ title: 'Bereich wechseln' }}
        />
      }
      navOverlay={
        overlay === 'main'
          ? mobileMainNav({ onLinkClick: dismissOverlay })
          : mobileUserNav({ onLinkClick: dismissOverlay })
      }
      navOverlayShown={!!overlay}
      onContentClick={dismissOverlay}
    >
      <PageLayout.ContentContainer>
        <PageLayout.Header title="Page Title"></PageLayout.Header>
        <p className="text-muted d-lg-none">Click the hamburger icon to open the main menu.</p>
        <p className="text-muted d-none d-lg-block">
          Click the button &quot;Bereich&quot; to open the app switcher menu.
        </p>
        <p className="text-muted mb-4">
          Click the button with the user&apos;s initials (&quot;AB&quot;) to open the user menu.
        </p>
        <p className="text-muted mb-4">
          Current breakpoint: <span className="d-sm-none text-primary">xs</span>
          <span className="d-none d-sm-inline d-md-none text-primary">sm</span>
          <span className="d-none d-md-inline d-lg-none text-primary">md</span>
          <span className="d-none d-lg-inline text-primary">lg, xl, xxl</span>
        </p>
        <div className="d-grid gap-4">
          <Section title="Section with text" collapsible>
            <p className="fw-bold">
              Lorem ipsum dolor sit amet consectetur adipisicing elit. Excepturi nostrum ducimus perspiciatis
              voluptatibus molestiae deserunt suscipit nobis temporibus saepe quos. Quos id amet a nam dicta distinctio
              unde minima obcaecati? Laboriosam velit in dicta dignissimos ullam, suscipit dolore? Facilis corrupti
              amet, facere placeat, inventore dolorum nemo molestias repellat consequuntur error iure architecto
              necessitatibus tenetur voluptas fugiat, totam maiores odio illo?
            </p>
          </Section>
          <Section title="Section with a list" collapsible>
            <ListCard.Stack>
              <ListCard>List Card 1</ListCard>
              <ListCard>List Card 2</ListCard>
              <ListCard>List Card 3</ListCard>
            </ListCard.Stack>
          </Section>
        </div>
      </PageLayout.ContentContainer>
    </PageLayout>
  )
}
